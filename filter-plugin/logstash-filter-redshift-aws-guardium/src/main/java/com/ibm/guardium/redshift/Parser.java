package com.ibm.guardium.redshift;

import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.elastic.logstash.api.Event;

/**
 * Parser class is responsible to parse data of events object and set to guard
 * object.
 *
 * @author Ankita Pawar
 */
public class Parser {

	private final DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
			.append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS[[XXX][X]]"));

	private final DateTimeFormatter DATE_TIME_FORMATTER = dateTimeFormatterBuilder.toFormatter();
	private Logger log = LogManager.getLogger(Parser.class);
	final static String regex = "(\\w+\\s+(?i)External\\s+(?i)table)\\s+['\\\"\\`]?([a-zA-Z_.]+)*|((?i)show\\s+(?i)table)\\s+['\\\"\\`]?([a-zA-Z0-9_.]+)*|((?i)alter\\s+(?i)table)\\s+['\\\"\\`]?([a-zA-Z0-9_.]+)*|((?i)create\\s+(?i)table)\\s+['\\\"\\`]?([a-zA-Z0-9_]+)*|(\\w+\\s+(?i)PROCEDURE)\\s+['\\\"\\`]?([a-zA-Z0-9_.]+)*|(\\w+\\s+(?i)DATASHARE)\\s+['\\\"\\`]?(\\w+)*|(\\w+\\s+(?i)DATASHARES)\\s+['\\\"\\`]?(?i)like\\s+'([a-zA-Z_%-0-9]+)'*|(\\w+\\s+(?i)DATASHARES)|(\\w+\\s+(?i)library)\\s+['\\\"\\`]?([a-zA-Z0-9_.]+)*|(\\w+\\s+(?i)model)\\s+['\\\"\\`]?(\\w+)*|(\\w+\\s+(?i)Identity\\s+(?i)provider)\\s+['\\\"\\`]?([a-zA-Z0-9_.]+)*|(\\w+\\s+(?i)EXTERNAL\\s+(?i)FUNCTION)\\s+['\\\"\\`]?([a-zA-Z0-9_.]+)*|(\\w+\\s+(?i)EXTERNAL\\s+(?i)SCHEMA)\\s+['\\\"\\`]?([a-zA-Z0-9_.]+)*|(\\w+\\s+(?i)view)\\s+['\\\"\\`]?([a-zA-Z0-9_.]+)*|(\\w+\\s+(?i)compression)\\s+['\\\"\\`]?([a-zA-Z0-9_.]+)*|((?i)copy)\\s+['\\\"\\`]?([a-zA-Z0-9_.]+)*|((?i)vacuum)\\s+((?i)sort|(?i)delete)\\s+(?i)only\\s+['\\\"\\`]?(\\w+)|((?i)vacuum)\\s+(?i)reindex\\s+['\\\"\\`]?(\\w+)|((?i)vacuum)\\s*['\\\"\\`]?(\\w+)*|((?i)cancel)|((?i)unload)\\s+\\('([^']+)'\\)\\s+to\\s+'([^']+)'|(\\w+\\s+(?i)database)\\s+['\\\"\\`]?(\\w+)*|(\\w+\\s+(?i)materialized\\s+(?i)view)\\s+((?i)if\\s+(?i)exists\\s+)?['\\\"\\`]?(\\w+)*|(\\w+\\s+(?i)role)\\s+['\\\"\\`]?(\\w+)*|((?i)create\\s+(?i)or\\s+(?i)replace\\s+(?i)function)\\s+['\\\"\\`]?(\\w+)*|(\\w+\\s+(?i)function)\\s+['\\\"\\`]?(\\w+)*";
	final static Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
	List<String> lst = new ArrayList<>();

	/**
	 *
	 * @param event
	 * @return record
	 */
	public Record parseRecord(final Event event) throws ParseException {
		Record record = new Record();
		String dbName = RedShiftTags.UNKNOWN_STRING;
		record.setSessionId(
				event.getField(RedShiftTags.ID) != null ? event.getField(RedShiftTags.ID).toString() : RedShiftTags.NA_STRING);

		// Set dbName if present
		if (event.getField(RedShiftTags.DB) != null && event.getField(RedShiftTags.DBPREFIX) != null) {

			dbName = event.getField(RedShiftTags.DBPREFIX).toString() + ":"
					+ event.getField(RedShiftTags.DB).toString();
			record.setDbName(dbName);
		} else {
			record.setDbName(dbName);
		}

		if (event.getField(RedShiftTags.U_IDENTIFIER) != null) {
			record.setAppUserName(event.getField(RedShiftTags.U_IDENTIFIER).toString());
		} else if (event.getField(RedShiftTags.USER_NAME) != null) {
			record.setAppUserName(event.getField(RedShiftTags.USER_NAME).toString()); // condition for Connection log
		} else {
			record.setAppUserName(RedShiftTags.UNKNOWN_STRING);
		}
		record.setAccessor(parseAccessor(event, record));
		record.setSessionLocator(parseSessionLocator(event));

		if (event.getField(RedShiftTags.STATUS) != null
				&& event.getField(RedShiftTags.STATUS).toString().equalsIgnoreCase(RedShiftTags.UNAUTHORISED)) {
			record.setException(parseException(event));
		} else if (record.getAccessor().getDataType() == "CONSTRUCT") {
			record.setData(parseDataRegex(event));
		} else {
			record.setData(parseData(event));
		}

		if (event.getField("timestamp") != null) {
			record.setTime(getTime(event)); // condition for User_activity log
		} else {
			record.setTime(getConnTime(event)); // condition for Connection log
		}

		return record;
	}

	/**
	 * Populating SessionLocator from event.
	 *
	 * @param event
	 * @return SessionLocator
	 */
	public SessionLocator parseSessionLocator(final Event event) {
		SessionLocator sessionLocator = new SessionLocator();
		sessionLocator.setIpv6(false);
		if (event.getField(RedShiftTags.REMOTEHOST) != null && event.getField(RedShiftTags.REMOTEPORT) != null) {
			if (isIpv6(event.getField(RedShiftTags.REMOTEHOST).toString())) {
				sessionLocator.setClientIpv6(event.getField(RedShiftTags.REMOTEHOST).toString());
				sessionLocator.setIpv6(true);
				sessionLocator.setServerIpv6(RedShiftTags.DEFAULT_IPV6);
			} else {
				sessionLocator.setClientIp(event.getField(RedShiftTags.REMOTEHOST).toString());
				sessionLocator.setServerIp(RedShiftTags.DEFAULT_IP);
			}
			sessionLocator.setClientPort(Integer.parseInt(
					event.getField(RedShiftTags.REMOTEPORT).toString().isEmpty() ? RedShiftTags.DEFAULT_PORT.toString()
							: event.getField(RedShiftTags.REMOTEPORT).toString()));
			sessionLocator.setServerPort(RedShiftTags.DEFAULT_PORT);

		} else {
			sessionLocator.setClientIp(RedShiftTags.DEFAULT_IP);
			sessionLocator.setClientPort(RedShiftTags.DEFAULT_PORT);
			sessionLocator.setServerIp(RedShiftTags.DEFAULT_IP);
			sessionLocator.setServerPort(RedShiftTags.DEFAULT_PORT);
		}
		return sessionLocator;
	}

	/**
	 * Populating Accessor from event.
	 *
	 * @param event
	 * @return Accessor
	 */
	public Accessor parseAccessor(final Event event, final Record record) {
		Accessor accessor = new Accessor();
		if (event.getField(RedShiftTags.U_IDENTIFIER) != null) {
			accessor.setDbUser(event.getField(RedShiftTags.U_IDENTIFIER).toString());
		} else if (event.getField(RedShiftTags.USER_NAME) != null) {
			accessor.setDbUser(event.getField(RedShiftTags.USER_NAME).toString());
		} else {
			accessor.setDbUser(RedShiftTags.NA_STRING);
		}

		if (event.getField(RedShiftTags.OSVERSION) != null) {
			accessor.setOsUser(event.getField(RedShiftTags.OSVERSION).toString());
		} else {
			accessor.setOsUser(RedShiftTags.UNKNOWN_STRING);
		}
		accessor.setDbProtocol(RedShiftTags.DB_PROTOCOL);
		accessor.setServerType(RedShiftTags.REDSHIFT_STRING);
		accessor.setServiceName(record.getDbName());

		accessor.setServerOs(RedShiftTags.UNKNOWN_STRING);
		accessor.setClientOs(RedShiftTags.UNKNOWN_STRING);
		accessor.setClientHostName(RedShiftTags.UNKNOWN_STRING);
		if (event.getField(RedShiftTags.SERVERHOSTNAME_PREFIX) != null
				&& event.getField(RedShiftTags.SERVERHOSTNAME_PREFIX) instanceof String) {
			accessor.setServerHostName(event.getField(RedShiftTags.SERVERHOSTNAME_PREFIX).toString() + ".aws.com");
		}
		accessor.setDbProtocolVersion(RedShiftTags.UNKNOWN_STRING);
		accessor.setSourceProgram(RedShiftTags.UNKNOWN_STRING);
		accessor.setClient_mac(RedShiftTags.UNKNOWN_STRING);
		accessor.setServerDescription(RedShiftTags.UNKNOWN_STRING);
		accessor.setCommProtocol(RedShiftTags.UNKNOWN_STRING);
		lst.clear();
		final Matcher matcher = pattern
				.matcher(regexCustomReplace(event).replaceAll("(?i)if\\s+((?i)not)?(\\s+)?(?i)exists", ""));
		if (matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				if (matcher.group(i) != null) {
					lst.add(matcher.group(i));
				}
			}
		}
		if(!lst.isEmpty()) {
			accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
			accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);
		} else {
			accessor.setLanguage(RedShiftTags.LANGUAGE);
			accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);

		}
		return accessor;
	}

	/**
	 * Populating data object.
	 *
	 * @param event
	 * @return Data
	 */
	public Data parseData(final Event event) {
		Data data = new Data();
		data.setOriginalSqlCommand(regexCustomReplace(event));
		return data;
	}

	/**
	 * Populating data object.
	 *
	 * @param event
	 * @return Data
	 */
	private Data parseDataRegex(final Event event) {
		Data data = new Data();
		data.setConstruct(parseAsConstruct(event));
		return data;
	}

	private Construct parseAsConstruct(final Event event) {
		final Construct construct = new Construct();
		final Sentence sentence = parseSentence(event);
		// String test=regexCustomReplace(event);
		construct.sentences.add(sentence);
		// construct.setFullSql(test);
		construct.setFullSql(event.getField(RedShiftTags.SQLQUERY).toString());
		construct.setRedactedSensitiveDataSql(RedShiftTags.SQLQUERY);
		return construct;

	}

	private Sentence parseSentence(final Event event) {
		Sentence sentence = null;
		sentence = new Sentence(lst.get(0));
		if (lst.size() == 1) {
			sentence.setVerb(lst.get(0));
			return sentence;
		} else {
			sentence.setVerb(lst.get(0));
			sentence.getObjects().add(parseSentenceObject(lst.get(lst.size() - 1)));
		}
		return sentence;
	}

	/**
	 * Using this to perform operation on input, convert String core into
	 * sentenceObject Object and then return the value as response
	 *
	 * @param message
	 * @return sentenceobject
	 *
	 */
	private SentenceObject parseSentenceObject(String message) {
		SentenceObject sentenceObject = null;
		sentenceObject = new SentenceObject(message);
		sentenceObject.setName(message);
		sentenceObject.setType(RedShiftTags.COLLECTION);
		return sentenceObject;
	}

	private String regexCustomReplace(final Event event) {
		String query = RedShiftTags.UNKNOWN_STRING;
		if (event.getField(RedShiftTags.SQLQUERY) != null) {
			query = event.getField(RedShiftTags.SQLQUERY).toString();
			query = query.replace("LOG:", "");
			query = query.replaceAll("\\r|\\n", " ").replaceAll("(?i)select\\s+(?i)top\\s+[0-9]+", "select")
					.replaceAll("((?i)MINUS)", "EXCEPT").trim();
		}
		event.setField(RedShiftTags.SQLQUERY, query);
		return query;
	}

	/**
	 *
	 * @param event
	 * @return Time
	 */
	public Time getConnTime(final Event event) {
		try {
			String day = event.getField("day").toString();
			String md = event.getField("md").toString();
			String month = event.getField("month").toString();
			String year = event.getField("year").toString();
			String rdtime = event.getField("time").toString();

			String concatStr = day + ", " + md + " " + month + " " + year + " " + rdtime;

			SimpleDateFormat formatter =
					new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss:SSS", Locale.ENGLISH);
			Date date = formatter.parse(concatStr);
			long millis = date.getTime();

			return new Time(millis, date.getTimezoneOffset(), 0);

		} catch (Exception ex) {
			log.error("An error occurred during parsing the time for event: {}", event, ex);
			return new Time(0, 0, 0);
		}
	}

	/**
	 *
	 * @param event
	 * @return Time
	 */
	public Time getTime(Event event) {
		String dateString = event.getField(RedShiftTags.TIMESTAMP).toString();
		ZonedDateTime date = ZonedDateTime.parse(dateString);
		long millis = date.toInstant().toEpochMilli();
		int minOffset = date.getOffset().getTotalSeconds() / 60;
		return new Time(millis, minOffset, 0);
	}

	/**
	 *
	 * @param event
	 * @return Exception
	 */
	public ExceptionRecord parseException(final Event event) {
		ExceptionRecord exceptionRecord = new ExceptionRecord();
		exceptionRecord.setExceptionTypeId(RedShiftTags.EXCEPTION_TYPE_AUTHENTICATION_STRING);
		exceptionRecord.setDescription("LOGIN_FAILED");
		exceptionRecord.setSqlString(RedShiftTags.NA_STRING);
		return exceptionRecord;
	}

	public boolean isIpv6(final String address) {
		return address.contains(":");

	}

}
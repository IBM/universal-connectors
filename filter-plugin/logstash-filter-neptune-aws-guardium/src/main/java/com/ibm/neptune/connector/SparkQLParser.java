/*
* Copyright IBM Corp. 2021, 2022 All rights reserved.
*SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.neptune.connector;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.query.algebra.Add;
import org.eclipse.rdf4j.query.algebra.Clear;
import org.eclipse.rdf4j.query.algebra.Copy;
import org.eclipse.rdf4j.query.algebra.Create;
import org.eclipse.rdf4j.query.algebra.DeleteData;
import org.eclipse.rdf4j.query.algebra.DescribeOperator;
import org.eclipse.rdf4j.query.algebra.InsertData;
import org.eclipse.rdf4j.query.algebra.Load;
import org.eclipse.rdf4j.query.algebra.Modify;
import org.eclipse.rdf4j.query.algebra.Move;
import org.eclipse.rdf4j.query.algebra.MultiProjection;
import org.eclipse.rdf4j.query.algebra.Projection;
import org.eclipse.rdf4j.query.algebra.ProjectionElem;
import org.eclipse.rdf4j.query.algebra.Slice;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.UpdateExpr;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedUpdate;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;

import com.ibm.neptune.connector.constant.ApplicationConstants;

/**
 * 
 * A parser to parse the Neptune SparQL queries.
 * 
 */
public class SparkQLParser {

	private static final SPARQLParser SPARQL_PARSER = new SPARQLParser();
	private static final Logger LOG = LogManager.getLogger(SparkQLParser.class);
	private static final Pattern PATTERN = Pattern
			.compile("((?i)graph|(?i)with|(?i)from|(?i)load)\\s*((?i)named|(?i)silent)?(\\s*)?<([\\w\\.\\/:#-]+)>");
	private static final Pattern CLEAR_PATTERN = Pattern.compile("((?i)clear|(?i)drop)");

	public static Map<String, List<Object>> parseUpdate(String sql) throws Exception {

		Map<String, List<Object>> map = new HashMap<>();
		List<Object> objectList = new ArrayList<>();
		List<Object> verbList = new ArrayList<>();
		List<Object> dbNameList = new ArrayList<>();
		final Set<Object> set = new HashSet<>();

		map.put(ApplicationConstants.OBJECT, objectList);
		map.put(ApplicationConstants.VERB, verbList);
		map.put(ApplicationConstants.DBNAME, dbNameList);

		try {
			ParsedUpdate u = SPARQL_PARSER.parseUpdate(sql,
					URLDecoder.decode(ApplicationConstants.BASE_URL, ApplicationConstants.UTF));

//			System.out.println(sql);
			List<UpdateExpr> lst = u.getUpdateExprs();

			lst.forEach(new Consumer<UpdateExpr>() {

				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public void accept(UpdateExpr updExp) {

//					System.out.println(updExp);

					if (updExp instanceof Modify) {

						try {
							dbNameList.add(parseRegex(sql));
						} catch (Exception e) {
							LOG.error("Exception occured in the parseUpdate method while parsing the regex");
						}

						set.clear();

						TupleExpr insertExp = ((Modify) updExp).getInsertExpr();
						TupleExpr deleteExp = ((Modify) updExp).getDeleteExpr();

						if (deleteExp != null) {
							verbList.add(ApplicationConstants.DELETE);
						}

						if (insertExp != null) {
							verbList.add(ApplicationConstants.INSERT);
						}
					}

					else if (updExp instanceof Create) {

						String dbName = ((Create) updExp).getGraph() != null
								? trimDBName(((Create) updExp).getGraph().getValue().toString())
								: ApplicationConstants.UNKNOWN_STRING;
						dbNameList.add(dbName);
						objectList.add(Arrays.asList(dbName));
						verbList.add(
								ApplicationConstants.CREATE + ApplicationConstants.SPACE + ApplicationConstants.TYPE);

					}

					else if (updExp instanceof InsertData) {

						try {
							dbNameList.add(parseRegex(sql));
						} catch (Exception e) {
							LOG.error("Exception occured in the parseUpdate method while parsing the regex");
						}
						objectList.add(Arrays.asList(ApplicationConstants.UNKNOWN_STRING));
						verbList.add(
								ApplicationConstants.INSERT + ApplicationConstants.SPACE + ApplicationConstants.DATA);

					}

					else if (updExp instanceof DeleteData) {

						try {
							dbNameList.add(parseRegex(sql));
						} catch (Exception e) {
							LOG.error("Exception occured in the parseUpdate method while parsing the regex");
						}
						objectList.add(Arrays.asList(ApplicationConstants.UNKNOWN_STRING));
						verbList.add(
								ApplicationConstants.DELETE + ApplicationConstants.SPACE + ApplicationConstants.DATA);

					}

					else if (updExp instanceof Add) {

						String source = ((Add) updExp).getSourceGraph() != null
								? trimDBName(((Add) updExp).getSourceGraph().getValue().toString())
								: ApplicationConstants.UNKNOWN_STRING;

						String destination = ((Add) updExp).getDestinationGraph() != null
								? trimDBName(((Add) updExp).getDestinationGraph().getValue().toString())
								: ApplicationConstants.UNKNOWN_STRING;

						dbNameList.add(destination);
						objectList.add(Arrays.asList(source, destination));
						verbList.add(ApplicationConstants.ADD + ApplicationConstants.SPACE + ApplicationConstants.TYPE);

					}

					else if (updExp instanceof Copy) {

						String source = ((Copy) updExp).getSourceGraph() != null
								? trimDBName(((Copy) updExp).getSourceGraph().getValue().toString())
								: ApplicationConstants.UNKNOWN_STRING;

						String destination = ((Copy) updExp).getDestinationGraph() != null
								? trimDBName(((Copy) updExp).getDestinationGraph().getValue().toString())
								: ApplicationConstants.UNKNOWN_STRING;

						dbNameList.add(destination);
						objectList.add(Arrays.asList(source, destination));

						verbList.add(
								ApplicationConstants.COPY + ApplicationConstants.SPACE + ApplicationConstants.TYPE);

					}

					else if (updExp instanceof Move) {

						String source = ((Move) updExp).getSourceGraph() != null
								? trimDBName(((Move) updExp).getSourceGraph().getValue().toString())
								: ApplicationConstants.UNKNOWN_STRING;

						String destination = ((Move) updExp).getDestinationGraph() != null
								? trimDBName(((Move) updExp).getDestinationGraph().getValue().toString())
								: ApplicationConstants.UNKNOWN_STRING;

						dbNameList.add(destination);
						objectList.add(Arrays.asList(source, destination));
						verbList.add(
								ApplicationConstants.MOVE + ApplicationConstants.SPACE + ApplicationConstants.TYPE);

					}

					else if (updExp instanceof Clear) {

						String dbName = ((Clear) updExp).getGraph() != null
								? trimDBName(((Clear) updExp).getGraph().getValue().toString())
								: ApplicationConstants.UNKNOWN_STRING;

						dbNameList.add(dbName);
						objectList.add(Arrays.asList(dbName));
						final Matcher matcher = CLEAR_PATTERN.matcher(sql);
						verbList.add(((matcher.find() ? matcher.group(1) : ApplicationConstants.UNKNOWN_STRING)
								.equalsIgnoreCase(ApplicationConstants.DROP) ? ApplicationConstants.DROP
										: ApplicationConstants.CLEAR)
								+ ApplicationConstants.SPACE + ApplicationConstants.TYPE);

					}

					else if (updExp instanceof Load) {

						String source = ApplicationConstants.UNKNOWN_STRING;
						try {
							source = parseRegex(sql);
						} catch (Exception e) {
							LOG.error("Exception occured in the parseUpdate method while parsing the regex");

						}
						String destination = ((Load) updExp).getGraph() != null
								? trimDBName(((Load) updExp).getGraph().getValue().toString())
								: ApplicationConstants.UNKNOWN_STRING;

						dbNameList.add(destination);

						objectList.add(Arrays.asList(source, destination));
						verbList.add(
								ApplicationConstants.LOAD + ApplicationConstants.SPACE + ApplicationConstants.TYPE);

					}

					try {
						updExp.visit(new AbstractQueryModelVisitor() {

							@Override
							public void meet(StatementPattern node) throws Exception {

								String dbName = node.getContextVar() != null
										? trimDBName(node.getContextVar().getValue().toString())
										: ApplicationConstants.UNKNOWN_STRING;
								String object = node.getSubjectVar() != null ? node.getSubjectVar().getName()
										: ApplicationConstants.UNKNOWN_STRING;
								set.add(dbName.isEmpty() ? object : (dbName + "." + object));
								super.meet(node);
							}

						});

						if (updExp instanceof Modify) {
							objectList.add(new ArrayList<>(set));
						}

					} catch (Exception e) {
						LOG.error("Exception occured in parseUpdate method");
					}
				}
			});

			if (objectList.size() < verbList.size()) {

				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) objectList.get(objectList.size() - 1);
				objectList.add(Arrays.asList(list.get(list.size() - 1)));

			}
//			System.out.println(
//					"ObjectList: " + objectList + "\n" + "VerbList: " + verbList + "\n" + "DbNameList:" + dbNameList);
//			System.out.println(
//					"--------------------------------------------------------------------------------------------");

			return map;

		} catch (Exception e) {
			LOG.error("Exception occured in the parseUpdate method of sparql parser ");
			throw e;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, List<Object>> parseQuery(String sql) throws Exception {

		Map<String, List<Object>> map = new HashMap<>();
		List<Object> objectList = new ArrayList<>();
		List<Object> verbList = new ArrayList<>();
		List<Object> dbNameList = new ArrayList<>();
		List<Object> list = new ArrayList<>();
		map.put(ApplicationConstants.OBJECT, objectList);
		map.put(ApplicationConstants.VERB, verbList);
		map.put(ApplicationConstants.DBNAME, dbNameList);

		try {
			ParsedQuery q = SPARQL_PARSER.parseQuery(sql,
					URLDecoder.decode(ApplicationConstants.BASE_URL, ApplicationConstants.UTF));

			TupleExpr te = q.getTupleExpr();

//			System.out.println(sql);
//			System.out.println(te);

			dbNameList.add(parseRegex(sql));

			te.visit(new AbstractQueryModelVisitor() {

				@Override
				public void meet(Projection node) throws Exception {
					if (verbList.isEmpty()) {

						verbList.add(te instanceof DescribeOperator ? ApplicationConstants.DESCRIBE
								: ApplicationConstants.SELECT);
					}
					super.meet(node);
				}

				@Override
				public void meet(MultiProjection node) throws Exception {
					verbList.add(ApplicationConstants.SELECT);
					super.meet(node);
				}

				@Override
				public void meet(Slice node) throws Exception {
					if (verbList.isEmpty()) {
						verbList.add(ApplicationConstants.SELECT);
					}
					super.meet(node);
				}

				@Override
				public void meet(StatementPattern node) throws Exception {
					if (!(te instanceof DescribeOperator)
							&& !(node.getSubjectVar().toString().contains(ApplicationConstants.CONST)
									|| node.getSubjectVar().toString().contains(ApplicationConstants.ANON))) {
						list.add(node.getSubjectVar().getName());
					}
					super.meet(node);
				}

				@Override
				public void meet(ValueConstant node) throws Exception {
					if (te instanceof DescribeOperator) {
						String value = trimDBName(node.getValue().toString());
						dbNameList.remove(dbNameList.size() - 1);
						dbNameList.add(value);
						list.add(value);
					}
					super.meet(node);
				}

				@Override
				public void meet(ProjectionElem node) throws Exception {
					if (te instanceof DescribeOperator && !node.getName().contains(ApplicationConstants.DESCRB)) {
						list.add(node.getName());
					}
					super.meet(node);
				}
			});

			objectList.add(list);
//			System.out.println(objectList + " : " + verbList + ":" + dbNameList);
//			System.out.println(
//					"ObjectList: " + objectList + "\n" + "VerbList: " + verbList + "\n" + "DbNameList:" + dbNameList);
//			System.out.println(
//					"--------------------------------------------------------------------------------------------");

			return map;
		} catch (Exception e) {
			LOG.error("Exception occured in parseQuery method of sparql parser");
			throw e;
		}
	}

	private static String parseRegex(final String query) throws Exception {

		final Matcher matcher = PATTERN.matcher(query);
		String dbName = ApplicationConstants.UNKNOWN_STRING;

		if (matcher.find()) {
			dbName = trimDBName(matcher.group(4));
		}
		return dbName;
	}

	private static String trimDBName(String dbName) {

		return dbName != null && dbName.contains(ApplicationConstants.FORWARDSLASH)
				? dbName.split("\\/")[dbName.split("\\/").length - 1]
				: dbName;

	}
}

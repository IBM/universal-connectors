package com.ibm.guardium.universalconnector.commons.custom_parsing.parsers;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Map;

public class LeefParserTest {
        private static final String SYSLOG_HEADER = "<13>Jan 18 11:07:53 192.168.1.1 ";
        private static final String LEEF_SAMPLE = "LEEF:LEEFVersion|Vendor|Product|Version|EventID|"
                        + "cat=123\tdevTime=2019 Jun 05 12:12:12\tdevTimeFormat=yyyy MMM dd HH:mm:ss\tproto=UDP\t"
                        + "sev=9\tsrc=12.12.12.12\tdst=13.13.13.13\tsrcPort=81\tdstPort=82\tsrcPreNAT=12.12.12.13\t"
                        + "dstPreNAT=12.12.12.14\tsrcPostNAT=12.12.12.15\tdstPostNAT=12.12.12.16\tusrName=Jack\t"
                        + "srcMAC=11:22:33:AA:BB:CC\tdstMAC=AA:BB:CC:11:22:33\tsrcPreNATPort=100\tdstPreNATPort=101\t"
                        + "srcPostNATPort=102\tdstPostNATPort=103\tidentSrc=14.14.14.14\tidentHostName=testhost\t"
                        + "identNetBios=testnb\tidentGrpName=testgroup\tidentMAC=AA:11:BB:22:CC:33";
        private static final String LEEF_SAMPLE_V1 = "LEEF:1.0|Microsoft|MSExchange|4.0 SP1|15345|"
                        + "src=192.0.2.0\tdst=172.50.123.1\tsev=5\tcat=anomaly\tsrcPort=81\tdstPort=21\tusrName=joe.black\t"
                        + "testField1=value1\ttestField2=value2";
        private static final String LEEF_SAMPLE_V2 = "LEEF:2.0|Lancope|StealthWatch|1.0|41|^|"
                        + "src=192.0.2.0^dst=172.50.123.1^sev=5^cat=anomaly^srcPort=81^dstPort=21^usrName=joe.black^"
                        + "testField1=value1^testField2=value2";
        private static final String LEEF_SAMPLE_V2_2 = "LEEF:2.0|Microsoft|MSExchange|4.0 SP1|15345|"
                        + "src=192.0.2.0\tdst=172.50.123.1\tsev=5\tcat=anomaly\tsrcPort=81\tdstPort=21\tusrName=joe.black\t"
                        + "testField1=value1\ttestField2=value2";

        private static final String HEX_DELIMITER_1 = "LEEF:2.0|Lancope|StealthWatch|1.0|41|x5E|"
                        + "src=192.0.2.0^dst=172.50.123.1^sev=5^cat=anomaly^srcPort=81^dstPort=21^usrName=joe.black^"
                        + "testField1=value1^testField2=value2";
        private static final String HEX_DELIMITER_2 = "LEEF:2.0|Lancope|StealthWatch|1.0|41|xa6|"
                        + "src=192.0.2.0¦dst=172.50.123.1¦sev=5¦cat=anomaly¦srcPort=81¦dstPort=21¦usrName=joe.black¦"
                        + "testField1=value1¦testField2=value2";
        private static final String HEX_DELIMITER_3 = "LEEF:2.0|Lancope|StealthWatch|1.0|41|0xa6|"
                        + "src=192.0.2.0¦dst=172.50.123.1¦sev=5¦cat=anomaly¦srcPort=81¦dstPort=21¦usrName=joe.black¦"
                        + "testField1=value1¦testField2=value2";

        private static final String INVALID_DELIMITER_1 = "LEEF:2.0|Microsoft|MSExchange|4.0 SP1|15345|adhashds|"
                        + "src=192.0.2.0\tdst=172.50.123.1\tsev=5\tcat=anomaly\tsrcPort=81\tdstPort=21\tusrName=joe.black\t"
                        + "testField1=value1\ttestField2=value2";

        private static final String INVALID_DELIMITER_2 = "LEEF:2.0|Microsoft|MSExchange|4.0 SP1|15345||"
                        + "src=192.0.2.0\tdst=172.50.123.1\tsev=5\tcat=anomaly\tsrcPort=81\tdstPort=21\tusrName=joe.black\t"
                        + "testField1=value1\ttestField2=value2";

        private static final String BAR_DELIMITER = "LEEF:2.0|Microsoft|MSExchange|4.0 SP1|15345|||"
                        + "src=192.0.2.0|dst=172.50.123.1|sev=5|cat=anomaly|srcPort=81|dstPort=21|usrName=joe.black|"
                        + "testField1=value1|testField2=value2";

        private static final LeefParser lp = new LeefParser();

        @Test
        public void testLeefTemplate() {
                // Test a few cases that should be invalid
                assertTrue(lp.parsePayload("").isEmpty());
                assertTrue(lp.parsePayload(SYSLOG_HEADER + " random-event-text eventid=0").isEmpty());

                // Valid LEEF, test a random sample of fields and overall size
                Map<String, String> map = lp.parsePayload(LEEF_SAMPLE);
                assertEquals(30, map.size());
                assertEquals("testhost", map.get("identHostName"));
                assertEquals("AA:11:BB:22:CC:33", map.get("identMAC"));
                assertEquals("123", map.get("cat"));
                // Now test the header fields are populated
                assertEquals("LEEFVersion", map.get("$leefversion$"));
                assertEquals("Vendor", map.get("$vendor$"));
                assertEquals("Product", map.get("$product$"));
                assertEquals("Version", map.get("$version$"));
                assertEquals("EventID", map.get("$eventid$"));

                // Test again with a Syslog header
                map = lp.parsePayload(SYSLOG_HEADER + LEEF_SAMPLE);
                assertEquals(30, map.size());
                assertEquals("testhost", map.get("identHostName"));
                assertEquals("AA:11:BB:22:CC:33", map.get("identMAC"));
                assertEquals("123", map.get("cat"));
                // Now test the header fields are populated
                assertEquals("LEEFVersion", map.get("$leefversion$"));
                assertEquals("Vendor", map.get("$vendor$"));
                assertEquals("Product", map.get("$product$"));
                assertEquals("Version", map.get("$version$"));
                assertEquals("EventID", map.get("$eventid$"));
        }

        @Test
        public void testLeefV1() {
                // Valid LEEF 1.0, test a random sample of fields and overall size
                Map<String, String> map = lp.parsePayload(LEEF_SAMPLE_V1);
                assertEquals(14, map.size());
                assertEquals("192.0.2.0", map.get("src"));
                assertEquals("joe.black", map.get("usrName"));
                assertEquals("anomaly", map.get("cat"));
                assertEquals("value2", map.get("testField2"));
                // Now test the header fields are populated
                assertEquals("1.0", map.get("$leefversion$"));
                assertEquals("Microsoft", map.get("$vendor$"));
                assertEquals("MSExchange", map.get("$product$"));
                assertEquals("4.0 SP1", map.get("$version$"));
                assertEquals("15345", map.get("$eventid$"));

                // Test again with a Syslog header
                map = lp.parsePayload(SYSLOG_HEADER + LEEF_SAMPLE_V1);
                assertEquals(14, map.size());
                assertEquals("192.0.2.0", map.get("src"));
                assertEquals("joe.black", map.get("usrName"));
                assertEquals("anomaly", map.get("cat"));
                assertEquals("value2", map.get("testField2"));
                // Now test the header fields are populated
                assertEquals("1.0", map.get("$leefversion$"));
                assertEquals("Microsoft", map.get("$vendor$"));
                assertEquals("MSExchange", map.get("$product$"));
                assertEquals("4.0 SP1", map.get("$version$"));
                assertEquals("15345", map.get("$eventid$"));
        }

        @Test
        public void testLeefV2() {
                // Valid LEEF 2.0, test a random sample of fields and overall size
                Map<String, String> map = lp.parsePayload(LEEF_SAMPLE_V2);
                assertEquals(14, map.size());
                assertEquals("192.0.2.0", map.get("src"));
                assertEquals("joe.black", map.get("usrName"));
                assertEquals("anomaly", map.get("cat"));
                assertEquals("value2", map.get("testField2"));
                // Now test the header fields are populated
                assertEquals("2.0", map.get("$leefversion$"));
                assertEquals("Lancope", map.get("$vendor$"));
                assertEquals("StealthWatch", map.get("$product$"));
                assertEquals("1.0", map.get("$version$"));
                assertEquals("41", map.get("$eventid$"));

                // Test a with V2 payload that doesn't specify a custom-delimiter, as that is
                // optional
                map = lp.parsePayload(LEEF_SAMPLE_V2_2);
                assertEquals(14, map.size());
                assertEquals("192.0.2.0", map.get("src"));
                assertEquals("joe.black", map.get("usrName"));
                assertEquals("anomaly", map.get("cat"));
                assertEquals("value2", map.get("testField2"));
                // Now test the header fields are populated
                assertEquals("2.0", map.get("$leefversion$"));
                assertEquals("Microsoft", map.get("$vendor$"));
                assertEquals("MSExchange", map.get("$product$"));
                assertEquals("4.0 SP1", map.get("$version$"));
                assertEquals("15345", map.get("$eventid$"));

                // Test again with a Syslog header
                map = lp.parsePayload(SYSLOG_HEADER + LEEF_SAMPLE_V2);
                assertEquals(14, map.size());
                assertEquals("192.0.2.0", map.get("src"));
                assertEquals("joe.black", map.get("usrName"));
                assertEquals("anomaly", map.get("cat"));
                assertEquals("value2", map.get("testField2"));
                // Now test the header fields are populated
                assertEquals("2.0", map.get("$leefversion$"));
                assertEquals("Lancope", map.get("$vendor$"));
                assertEquals("StealthWatch", map.get("$product$"));
                assertEquals("1.0", map.get("$version$"));
                assertEquals("41", map.get("$eventid$"));
        }

        @Test
        public void testHexLeefV2Delimiters() {
                // This sample uses 'x5E' as the delimiter, which is the hex value of '^'
                Map<String, String> map = lp.parsePayload(HEX_DELIMITER_1);
                assertEquals(14, map.size());
                assertEquals("192.0.2.0", map.get("src"));
                assertEquals("joe.black", map.get("usrName"));
                assertEquals("anomaly", map.get("cat"));
                assertEquals("value2", map.get("testField2"));
                // Now test the header fields are populated
                assertEquals("2.0", map.get("$leefversion$"));
                assertEquals("Lancope", map.get("$vendor$"));
                assertEquals("StealthWatch", map.get("$product$"));
                assertEquals("1.0", map.get("$version$"));
                assertEquals("41", map.get("$eventid$"));

                // This sample uses 'xa6' as the delimiter, which is the hex value of '¦'
                map = lp.parsePayload(HEX_DELIMITER_2);
                assertEquals(14, map.size());
                assertEquals("192.0.2.0", map.get("src"));
                assertEquals("joe.black", map.get("usrName"));
                assertEquals("anomaly", map.get("cat"));
                assertEquals("value2", map.get("testField2"));
                // Now test the header fields are populated
                assertEquals("2.0", map.get("$leefversion$"));
                assertEquals("Lancope", map.get("$vendor$"));
                assertEquals("StealthWatch", map.get("$product$"));
                assertEquals("1.0", map.get("$version$"));
                assertEquals("41", map.get("$eventid$"));

                // This sample uses '0xa6' as the delimiter, which is the hex value of '¦'
                map = lp.parsePayload(HEX_DELIMITER_3);
                assertEquals(14, map.size());
                assertEquals("192.0.2.0", map.get("src"));
                assertEquals("joe.black", map.get("usrName"));
                assertEquals("anomaly", map.get("cat"));
                assertEquals("value2", map.get("testField2"));
                // Now test the header fields are populated
                assertEquals("2.0", map.get("$leefversion$"));
                assertEquals("Lancope", map.get("$vendor$"));
                assertEquals("StealthWatch", map.get("$product$"));
                assertEquals("1.0", map.get("$version$"));
                assertEquals("41", map.get("$eventid$"));
        }

        @Test
        public void testLeefV2InvalidDelimiters() {
                // This payload has a garbage 'delimiter' field, it should fall back to the
                // default
                // delimiter which is \t, and that should allow the event to parse anyhow.
                Map<String, String> map = lp.parsePayload(INVALID_DELIMITER_1);
                assertEquals(14, map.size());
                assertEquals("192.0.2.0", map.get("src"));
                assertEquals("joe.black", map.get("usrName"));
                assertEquals("anomaly", map.get("cat"));
                assertEquals("value2", map.get("testField2"));
                // Now test the header fields are populated
                assertEquals("2.0", map.get("$leefversion$"));
                assertEquals("Microsoft", map.get("$vendor$"));
                assertEquals("MSExchange", map.get("$product$"));
                assertEquals("4.0 SP1", map.get("$version$"));
                assertEquals("15345", map.get("$eventid$"));

                // This payload has nothing in the delimiter field of the header, ie just the
                // empty-string
                // Should also fallback to parse the attributes with a \t delimiter
                map = lp.parsePayload(INVALID_DELIMITER_2);
                assertEquals(14, map.size());
                assertEquals("192.0.2.0", map.get("src"));
                assertEquals("joe.black", map.get("usrName"));
                assertEquals("anomaly", map.get("cat"));
                assertEquals("value2", map.get("testField2"));
                // Now test the header fields are populated
                assertEquals("2.0", map.get("$leefversion$"));
                assertEquals("Microsoft", map.get("$vendor$"));
                assertEquals("MSExchange", map.get("$product$"));
                assertEquals("4.0 SP1", map.get("$version$"));
                assertEquals("15345", map.get("$eventid$"));
        }

        @Test
        public void testLeefV2BarDelimiter() {
                // This payload has a custom attribute delimeter of '|', this is a special case
                // within the
                // library because '|' is the delimiter that the header itself uses. Still
                // should work though.
                Map<String, String> map = lp.parsePayload(BAR_DELIMITER);
                assertEquals(14, map.size());
                assertEquals("192.0.2.0", map.get("src"));
                assertEquals("joe.black", map.get("usrName"));
                assertEquals("anomaly", map.get("cat"));
                assertEquals("value2", map.get("testField2"));
                // Now test the header fields are populated
                assertEquals("2.0", map.get("$leefversion$"));
                assertEquals("Microsoft", map.get("$vendor$"));
                assertEquals("MSExchange", map.get("$product$"));
                assertEquals("4.0 SP1", map.get("$version$"));
                assertEquals("15345", map.get("$eventid$"));
        }

}
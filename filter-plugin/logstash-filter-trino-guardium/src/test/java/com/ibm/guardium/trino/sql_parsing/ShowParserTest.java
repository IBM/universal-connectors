package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShowParserTest {

    @Test
    void parseStatement() throws Exception {
        Data data = new Data();
        String show;
        ShowParser parser;
        int i = 0;

    show = "SHOW FUNCTIONS LIKE 't*'";
    parser = new ShowParser(data, show, new LinkedHashMap<>());
    parser.parse();
    assertEquals("SHOW FUNCTIONS", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("'t*'", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("FUNCTION", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

        show = "SHOW COLUMNS FROM hive.default.customers;";
    parser = new ShowParser(data, show, new LinkedHashMap<>());
        parser.parse();
        assertEquals("SHOW COLUMNS", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "hive.default.customers",
        data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

        show = "SHOW TABLES FROM hive.default;";
        data = new Data();
    parser = new ShowParser(data, show, new LinkedHashMap<>());
        parser.parse();
        assertEquals("SHOW TABLES", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "hive.default", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("DATABASE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

        show = "SHOW SCHEMAS FROM hive";
        data = new Data();
    parser = new ShowParser(data, show, new LinkedHashMap<>());
        parser.parse();
        assertEquals("SHOW SCHEMAS", data.getConstruct().getSentences().get(0).getVerb());
        assertEquals("hive", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("DATABASE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

        show = "SHOW SCHEMAS FROM hive";
        data = new Data();
    parser = new ShowParser(data, show, new LinkedHashMap<>());
        parser.parse();
        assertEquals("SHOW SCHEMAS", data.getConstruct().getSentences().get(0).getVerb());
        assertEquals("hive", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("DATABASE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

        show = "SHOW CREATE TABLE my_table;";
        data = new Data();
    parser = new ShowParser(data, show, new LinkedHashMap<>());
        parser.parse();
        assertEquals("SHOW CREATE TABLE", data.getConstruct().getSentences().get(0).getVerb());
        assertEquals("my_table", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

        show = "SHOW GRANTS FOR user1;";
        data = new Data();
    parser = new ShowParser(data, show, new LinkedHashMap<>());
        parser.parse();
        assertEquals("SHOW GRANTS", data.getConstruct().getSentences().get(0).getVerb());
        assertEquals("user1", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals(
        "ROLE OR USER", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

        show = "SHOW COLUMNS FROM my_catalog.my_schema.my_table LIKE 'id%';";
        data = new Data();
    parser = new ShowParser(data, show, new LinkedHashMap<>());
        parser.parse();
        assertEquals("SHOW COLUMNS", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "my_catalog.my_schema.my_table",
        data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    show = "SHOW TBLPROPERTIES delta./mnt/data/sales_data;";
    data = new Data();
    parser = new ShowParser(data, show, new LinkedHashMap<>());
    parser.parse();
    assertEquals("SHOW TBLPROPERTIES", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("delta", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    show = "SHOW TBLPROPERTIES hive.default.customers;";
    data = new Data();
    parser = new ShowParser(data, show, new LinkedHashMap<>());
    parser.parse();
    assertEquals("SHOW TBLPROPERTIES", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "hive.default.customers",
        data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
    show = "show grant on DATABASE `hive_metastore`.`default`";
    data = new Data();
    parser = new ShowParser(data, show, new LinkedHashMap<>());
    parser.parse();
    assertEquals("SHOW GRANT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "`hive_metastore`.`default`",
        data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("DATABASE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
    }
}

/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.azure.cosmos;

import static org.junit.Assert.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Test;

public class ParserTest {
	
    @Test
	public void testparseGetTime() throws Exception {
	final String CosmoString="{\"time\":\"2023-02-21T11:37:17.0556567Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMOS\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/DEV-COSMO\",\"category\":\"DataPlaneRequests\",\"operationName\":\"Create\",\"properties\":{\"activityId\":\"880d1d92-159c-4134-ae35-9c4e48807fd3\",\"requestResourceType\":\"DatabaseFeed\",\"requestResourceId\":\"\\/dbs\",\"collectionRid\":\"\",\"databaseRid\":\"\",\"statusCode\":\"201\",\"duration\":\"444.264900\",\"userAgent\":\"Microsoft.Azure.Documents.Common\\/2.14.0\",\"clientIpAddress\":\"20.193.136.102\",\"requestCharge\":\"1.000000\",\"requestLength\":\"25\",\"responseLength\":\"175\",\"resourceTokenPermissionId\":\"\",\"resourceTokenPermissionMode\":\"\",\"resourceTokenUserRid\":\"\",\"region\":\"Central India\",\"partitionId\":\"\",\"aadAppliedRoleAssignmentId\":\"\",\"aadPrincipalId\":\"\",\"authTokenType\":\"PrimaryMasterKey\",\"keyType\":\"PrimaryMasterKey\",\"connectionMode\":\"Gateway\",\"subscriptionId\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"databaseName\":\"\",\"collectionName\":\"\"}}";
	final JsonObject CosmosJson = JsonParser.parseString(CosmoString).getAsJsonObject();
	Record record = Parser.parseRecord(CosmosJson);
	assertEquals(1676979437055L, record.getTime().getTimstamp());
	assertNotNull(record);
    }
    

    @Test
	public void testparseClientip() throws Exception {
    final String CosmoString="{\"time\":\"2023-02-21T11:37:17.0556567Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMOS\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/DEV-COSMO\",\"category\":\"DataPlaneRequests\",\"operationName\":\"Create\",\"properties\":{\"activityId\":\"880d1d92-159c-4134-ae35-9c4e48807fd3\",\"requestResourceType\":\"DatabaseFeed\",\"requestResourceId\":\"\\/dbs\",\"collectionRid\":\"\",\"databaseRid\":\"\",\"statusCode\":\"201\",\"duration\":\"444.264900\",\"userAgent\":\"Microsoft.Azure.Documents.Common\\/2.14.0\",\"clientIpAddress\":\"20.193.136.102\",\"requestCharge\":\"1.000000\",\"requestLength\":\"25\",\"responseLength\":\"175\",\"resourceTokenPermissionId\":\"\",\"resourceTokenPermissionMode\":\"\",\"resourceTokenUserRid\":\"\",\"region\":\"Central India\",\"partitionId\":\"\",\"aadAppliedRoleAssignmentId\":\"\",\"aadPrincipalId\":\"\",\"authTokenType\":\"PrimaryMasterKey\",\"keyType\":\"PrimaryMasterKey\",\"connectionMode\":\"Gateway\",\"subscriptionId\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"databaseName\":\"\",\"collectionName\":\"\"}}";
    final JsonObject CosmosJson = JsonParser.parseString(CosmoString).getAsJsonObject();
	Record record = Parser.parseRecord(CosmosJson);
	assertEquals("20.193.136.102",record.getSessionLocator().getClientIp());
    }
    
    
    @Test
    public void testQuery_select() throws Exception {
    final String CosmoString="{\"time\":\"2023-02-21T06:03:28.9493677Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083FE1FC-CD4D-5B6C-895B-2B5AF1D082F4\\/RESOURCEGROUPS\\/AZURE-COSMOS\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/DEV-COSMO\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"f1810797-e32e-406d-af59-2280fea8001e\",\"databasename\":\"FamiliesDatabase\",\"collectionname\":\"Families\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083fe1fc-cd4d-5b6c-895b-2b5af1d082f4\",\"resourcegroupname\":\"Azure-cosmos\",\"partialipaddress\":\"52.140.110.66\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"2\",\"signature\":\"-1661408583400352360\",\"shapesignature\":\"8235320023077058687\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT f._rid, [{\\\\\\\"item\\\\\\\": f.address.city}] AS orderByItems, {\\\\\\\"givenName\\\\\\\": c.givenName} AS payload\\\\nFROM Families AS f\\\\nJOIN c IN f.children\\\\nWHERE ((f.id = \\\\\\\"WakefieldFamily\\\\\\\") AND (true))\\\\nORDER BY f.address.city ASC\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select", record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("f.children",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseINKeyword() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT *   FROM Families WHERE Families.id IN ('AndersenFamily', 'WakefieldFamily')\\\" ,\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("Families",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());
    }
    
    @Test
    public void testparseWHERE() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT c.givenName FROM Families f JOIN c IN f.children WHERE f.id = 'WakefieldFamily' ORDER BY f.address.city ASC\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("Families",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseCOUNT() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT TOP 5 f.id, (SELECT VALUE Count(1) FROM n IN f.nutrients WHERE n.units = 'mg') AS count_mg FROM food f\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("food",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("f.nutrients",record.getData().getConstruct().getSentences().get(0).getDescendants().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseCONCAT() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-29T13:15:47.5292582Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"ac76039d-7de4-49c1-884c-9de2e073a088\",\"databasename\":\"cosmoDB\",\"collectionname\":\"Families\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"0\",\"signature\":\"7836615225522989813\",\"shapesignature\":\"-1520510819878011583\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT TOP 5 (SELECT VALUE Concat('id_', f.id)) AS id FROM food f\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("food",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseENDSWITH() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-30T06:36:25.1036312Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"1b93a44b-ffbc-4c0f-9466-37934e81b5c9\",\"databasename\":\"cosmoDB\",\"collectionname\":\"Families\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"2\",\"signature\":\"3330002102473822776\",\"shapesignature\":\"-1026639395066501040\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT ENDSWITH(\\\\\\\"abc\\\\\\\", \\\\\\\"b\\\\\\\", false) AS e1, ENDSWITH(\\\\\\\"abc\\\\\\\", \\\\\\\"bC\\\\\\\", false) AS e2, ENDSWITH(\\\\\\\"abc\\\\\\\", \\\\\\\"bC\\\\\\\", true) AS e3 from myhome\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("myhome",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseaddress() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT * FROM Families.address.state\\\",\\\"parameters\\\":[]}\"}}";    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("Families.address.state",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8:COSMOS-DATABASE:cosmoDB",record.getDbName());
    }
    
    @Test
    public void testparsepayload() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-29T12:55:56.2461944Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"0b76d57d-d22d-4cb2-b912-7726e24b69ba\",\"databasename\":\"cosmoDB\",\"collectionname\":\"Families\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"2363625393322487707\",\"shapesignature\":\"7572324026122958197\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT {\\\\\\\"Count\\\\\\\": {\\\\\\\"item\\\\\\\": Count(1)}} AS payload\\\\nFROM c\\\\nJOIN t IN c.tags\\\\nJOIN n IN c.nutrients\\\\nJOIN s IN c.servings\\\\nWHERE (((t.name = \\\\\\\"infant formula\\\\\\\") AND ((n.nutritionValue > 0) AND (n.nutritionValue < 10))) AND (s.amount > 1))\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("c",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseItems() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-29T12:51:33.8058662Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"2ff43f42-bea8-441d-b46e-cc53219355b4\",\"databasename\":\"cosmoDB\",\"collectionname\":\"Families\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"0\",\"signature\":\"-1661408583400352360\",\"shapesignature\":\"8235320023077058687\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT f._rid, [{\\\\\\\"item\\\\\\\": f.address.city}] AS orderByItems, {\\\\\\\"givenName\\\\\\\": c.givenName} AS payload\\\\nFROM Families AS f\\\\nJOIN c IN f.children\\\\nWHERE ((f.id = \\\\\\\"WakefieldFamily\\\\\\\") AND (true))\\\\nORDER BY f.address.city ASC\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("f.children",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparsepersonId() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceIds\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT p.FirstName, p.LastName, a.City, cd.Detail FROM Person p JOIN ContactDetail cd ON cd.PersonId = p.Id JOIN ContactDetailType cdt ON cdt.Id = cd.TypeId JOIN Address a ON a.PersonId = p.Id\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("ContactDetail",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    @Test
    public void testparseControlPlane() throws Exception {
    final String CosmoString="{ \"time\": \"2023-03-13T09:56:41.5889464Z\", \"resourceId\": \"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8/RESOURCEGROUPS/AZURE-COSMO/PROVIDERS/MICROSOFT.DOCUMENTDB/DATABASEACCOUNTS/COSMOS-DATABASE\", \"subscriptionId\": \"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\", \"operationName\": \"SqlContainersDelete\", \"category\": \"ControlPlaneRequests\", \"properties\": {\"activityId\": \"5520ee10-c185-11ed-8bc3-12f484077d92\",\"httpstatusCode\": \"204\",\"result\": \"OK\",\"httpMethod\": \"DELETE\",\"apiKind\": \"Sql\",\"apiKindResourceType\": \"Containers\",\"operationType\": \"Delete\",\"resourceUri\": \"sqlDatabases/MyDatabase/containers/products\",\"resourceDetails\": \"\"}}";
    final JsonObject CosmosJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record = Parser.parseRecord(CosmosJson);
    assertEquals("SqlContainersDelete",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("sqlDatabases/MyDatabase/containers/products",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("0.0.0.0",record.getSessionLocator().getClientIp());
    }

    @Test
    public void testparseControlPlaneResourceUri() throws Exception {
    final String CosmoString="{ \"time\": \"2023-03-13T09:56:41.5889464Z\", \"resourceId\": \"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8/RESOURCEGROUPS/AZURE-COSMO/PROVIDERS/MICROSOFT.DOCUMENTDB/DATABASEACCOUNTS/COSMOS-DATABASE\", \"subscriptionId\": \"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\", \"operationName\": \"SqlContainersDelete\", \"category\": \"ControlPlaneRequests\", \"properties\": {\"activityId\": \"5520ee10-c185-11ed-8bc3-12f484077d92\",\"httpstatusCode\": \"204\",\"result\": \"OK\",\"httpMethod\": \"DELETE\",\"apiKind\": \"Sql\",\"apiKindResourceType\": \"Containers\",\"operationType\": \"Delete\",\"resourceUri\": \"sqlDatabases/MyDatabase/containers/products\",\"resourceDetails\": \"\"}}";
    final JsonObject CosmosJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record = Parser.parseRecord(CosmosJson);
    assertEquals("SqlContainersDelete",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("sqlDatabases/MyDatabase/containers/products",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("0.0.0.0",record.getSessionLocator().getClientIp());
    }
   @Test
   public void testparseDataPlane() throws Exception {
   final String CosmoString="{\"time\":\"2023-02-21T11:37:17.0556567Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMOS\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/DEV-COSMO\",\"category\":\"DataPlaneRequests\",\"operationName\":\"Create\",\"properties\":{\"activityId\":\"880d1d92-159c-4134-ae35-9c4e48807fd3\",\"requestResourceType\":\"DatabaseFeed\",\"requestResourceId\":\"\\/dbs\",\"collectionRid\":\"\",\"databaseRid\":\"\",\"statusCode\":\"201\",\"duration\":\"444.264900\",\"userAgent\":\"Microsoft.Azure.Documents.Common\\/2.14.0\",\"clientIpAddress\":\"20.193.136.102\",\"requestCharge\":\"1.000000\",\"requestLength\":\"25\",\"responseLength\":\"175\",\"resourceTokenPermissionId\":\"\",\"resourceTokenPermissionMode\":\"\",\"resourceTokenUserRid\":\"\",\"region\":\"Central India\",\"partitionId\":\"\",\"aadAppliedRoleAssignmentId\":\"\",\"aadPrincipalId\":\"\",\"authTokenType\":\"PrimaryMasterKey\",\"keyType\":\"PrimaryMasterKey\",\"connectionMode\":\"Gateway\",\"subscriptionId\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"databaseName\":\"\",\"collectionName\":\"\"}}";
   final JsonObject CosmosJson = JsonParser.parseString(CosmoString).getAsJsonObject();
   Record record = Parser.parseRecord(CosmosJson);
   assertEquals("Create",record.getData().getConstruct().getSentences().get(0).getVerb());
   assertEquals("/dbs",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
   assertEquals("20.193.136.102",record.getSessionLocator().getClientIp());
   }
    @Test
    public void testparseNestedQuery() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT COUNT(UniqueLastNames)FROM (SELECT AVG(film.age) FROM film GROUP BY film.lastName) AS UniqueLastNames\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    }
    
    @Test
    public void testparseGroupBy() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT COUNT(1)FROM (SELECT AVG(f.age)FROM f GROUP BY f.lastName)\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    }
    
    @Test
    public void testparseExists() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT TOP 5 f.id, f.tagsFROM food f WHERE EXISTS(SELECT VALUE t FROM t IN f.tags WHERE t.name = 'orange')\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    }
    
    @Test
    public void testparseStartsWith() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT * FROM products p WHERE STARTSWITH(p.tags[0].slug, \'color-group-\')\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("products",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseSpatialfunc() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT f.id FROM Families f WHERE ST_DISTANCE(f.location, {'type': 'Point', 'coordinates':[31.9, -4.8]}) < 30000\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("Families",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseSpatialfunc1() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\" SELECT a.id FROM Areas a WHERE ST_INTERSECTS(a.location, {  'type':'Polygon','coordinates': [[[31.8, -5], [32, -5], [32, -4.7], [31.8, -4.7], [31.8, -5]]]  })\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("Areas",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    

    @Test
    public void testparseBitwiseOperator() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"  SELECT (100 >> 2) AS rightShift,(100 << 2) AS leftShift,(100 >>> 0) AS zeroFillRightShift,(100 & 1000) AS logicalAnd,(100 | 1000) AS logicalOr,(100 ^ 1000) AS logicalExclusiveOr\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    }
    
    @Test
    public void testparseOffsetLimit() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT f.id, f.address.city FROM Families f ORDER BY f.address.city OFFSET 1 LIMIT 1\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("Families",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    
    @Test
    public void testparseMathfunc() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT ABS(-1) AS abs1, ABS(0) AS abs2, ABS(1) AS abs3\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    }
    
    @Test
    public void testparseNormalQuery() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"select * from ABC\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("ABC",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseLikekeyword() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT * FROM c WHERE c.description LIKE \\\'%fruit%\\\'\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("c",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseNotLikekeyword() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT * FROM c WHERE c.description NOT LIKE \\\'%fruit%\\\'\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("c",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseEscapeClause() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT * FROM c WHERE c.description LIKE '%20-30!%%' ESCAPE '!'\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("c",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    
    @Test
    public void testparseUDF() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT Families.id, Families.address.city FROM Families WHERE udf.REGEX_MATCH(Families.address.city, \'.*eattle\')\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("Families",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseBetween() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT (c.grade BETWEEN 0 AND 10)FROM Families.children[0] c\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("Families.children[0]",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseAggregate() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT AVG(c.propertyA)FROM c\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("c",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseMin() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT MIN(cities.propertyA)FROM cities\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("cities",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseMax() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT MAX(c.propertyA)FROM c\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("c",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    
    @Test
    public void testparseArrayQuery() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT c.InvoiceNumber,c.Customer.Name FROM   c WHERE  ARRAY_CONTAINS(c.CustomerComments, \'Superb\')\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("c",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name); 
    }
    
    @Test
    public void testparseParamQuery() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\" SELECT * FROM Families f WHERE f.lastName = @lastName AND f.address.state = @addressState\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("Families",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name); 
    }
    
    @Test
    public void testparseValueQuery() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT VALUE child FROM child IN Parents.children\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("Parents.children",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name); 
    }
    
    @Test
	public void testparseQueryRuntimeStatics() throws Exception {
	final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT *\\\\r\\\\n    FROM cosmosDB collection\\\\r\\\\n    WHERE collection.id = 'AndersenFamily'\\\",\\\"parameters\\\":[]}\"}}";
	final JsonObject CosmosJson = JsonParser.parseString(CosmoString).getAsJsonObject();
	Record record = Parser.parseRecord(CosmosJson);
	assertEquals("083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8:COSMOS-DATABASE:cosmoDB", record.getDbName());
	}
    @Test
    public void testparseGrade() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT * FROM Families f WHERE ({grade: f.children[0].grade}.grade > 3)\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("Families",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name); 
    }
    
    @Test
    public void testparseValueGrade() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\" SELECT VALUE {\'name\':f.children[0].familyName,\'grade\': f.children[0].grade + 3 } FROM Families123 f\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("Families123",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name); 
    }
    
    @Test
    public void testparseNutrition() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT TOP 1000 c.id, m.MaxNutritionValue FROM cJOIN (SELECT udf.GetMaxNutritionValue(c.nutrients) AS MaxNutritionValue) m WHERE m.MaxNutritionValue > 100\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("cJOIN",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name); 
    }
    
    @Test
    public void testparse3obj() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT Count(1) AS Count FROM c JOIN t IN c.tags JOIN n IN c.nutrients JOIN s IN c.servings WHERE t.name = 'infant formula' AND (n.nutritionValue > 0 AND n.nutritionValue < 10) AND s.amount > 1\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("c.nutrients",record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name); 
    }
    
    @Test
    public void testparseDescsobj() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT a.Name,{\'Make\': a.Make, \'Model\': a.Model, \'SellingPrice\': a.SellingPrice} AS InvoiceDetails FROM (SELECT  c.InvoiceNumber,c.Customer.Name,cx.LineItem,cx.Make,cx.Model,cx.SellingPrice FROM   c JOIN   cx IN c.Salesdetails) a\\\",\\\"parameters\\\":[]}\"}}";	
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("c",record.getData().getConstruct().getSentences().get(0).getDescendants().get(0).getObjects().get(0).name); 	
    }
    
    @Test
    public void testparseCurrentTime() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT GetCurrentDateTime() AS currentUtcDateTime\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    }
    
    @Test
    public void testparseIndexQuery() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT INDEX_OF(\'abc\', \'ab\') AS index_of_prefix,INDEX_OF(\'abc\', \'b\') AS index_of_middle,INDEX_OF(\'abc\', \'c\') AS index_of_last,INDEX_OF(\'abc\', \'d\') AS index_of_missing\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    }
    
    @Test
    public void testparseReplaceQuery() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT REPLACE(\'This is a Test\', \'Test\', \'desk\') AS replace\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8:COSMOS-DATABASE:cosmoDB",record.getDbName());
    }
    
    @Test
    public void testparseReverseQuery() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT REVERSE(\'Abc\') AS reverse\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8:COSMOS-DATABASE:cosmoDB",record.getDbName());
    assertEquals(1680004089768L,record.getTime().getTimstamp());
    }
    
    @Test
    public void testparseRTRIM() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\" SELECT RTRIM(\'   abc\') AS t1, RTRIM(\'   abc   \') AS t2, RTRIM(\'abc   \') AS t3, RTRIM(\'abc\') AS t4,RTRIM(\'abc\', \'bc\') AS t5,RTRIM(\'abc\', \'abc\') AS t6\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    }
    
    @Test
    public void testparseLeftQuery() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT LEFT(\'abc\', 1) AS l1, LEFT(\'abc\', 2) AS l2\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    }
    
    @Test
    public void testparseStringToBoolean() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT StringToBoolean(\'true\') AS b1, StringToBoolean(\'    false\') AS b2,StringToBoolean(\'false    \') AS b3\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8:COSMOS-DATABASE:cosmoDB",record.getDbName());
    }
    
    @Test
    public void testparseStringToNull() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT StringToNull(\'null\') AS n1, StringToNull(\'  null \') AS n2, IS_NULL(StringToNull(\'null   \')) AS n3\\\",\\\"parameters\\\":[]}\"}}";	
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());	
    }
    
    @Test
    public void testparseStringToNumber() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT StringToNumber(\'1.000000\') AS num1, StringToNumber(\'3.14\') AS num2,StringToNumber(\'   60   \') AS num3, StringToNumber(\'-1.79769e+308\') AS num4\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());	
    }
    
    @Test
    public void testparseSubString() throws Exception {
    final String CosmoString="{\"time\":\"2023-03-28T11:48:09.7683235Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\",\"databasename\":\"cosmoDB\",\"collectionname\":\"collection\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"-4576541951102199744\",\"shapesignature\":\"-8612658657970479756\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT SUBSTRING(\'abc\', 1, 1) AS substring\\\",\\\"parameters\\\":[]}\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("select",record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8-COSMOS-DATABASE.azure.com",record.getAccessor().getServerHostName());
    }
    
    @Test
    public void testparseIpv6() throws Exception {
    final String CosmoString="{\"time\":\"2023-02-21T11:37:17.0556567Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMOS\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/DEV-COSMO\",\"category\":\"DataPlaneRequests\",\"operationName\":\"Create\",\"properties\":{\"activityId\":\"880d1d92-159c-4134-ae35-9c4e48807fd3\",\"requestResourceType\":\"DatabaseFeed\",\"requestResourceId\":\"\\/dbs\",\"collectionRid\":\"\",\"databaseRid\":\"\",\"statusCode\":\"201\",\"duration\":\"444.264900\",\"userAgent\":\"Microsoft.Azure.Documents.Common\\/2.14.0\",\"clientIpAddress\":\"2001:0db8:85a3:0000:0000:8a2e:0370:7334\",\"requestCharge\":\"1.000000\",\"requestLength\":\"25\",\"responseLength\":\"175\",\"resourceTokenPermissionId\":\"\",\"resourceTokenPermissionMode\":\"\",\"resourceTokenUserRid\":\"\",\"region\":\"Central India\",\"partitionId\":\"\",\"aadAppliedRoleAssignmentId\":\"\",\"aadPrincipalId\":\"\",\"authTokenType\":\"PrimaryMasterKey\",\"keyType\":\"PrimaryMasterKey\",\"connectionMode\":\"Gateway\",\"subscriptionId\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"databaseName\":\"\",\"collectionName\":\"\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("0000:0000:0000:0000:0000:FFFF:0000:0000",record.getSessionLocator().getServerIpv6());
    assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334",record.getSessionLocator().getClientIpv6());
    }

    @Test
    public void testparseServerHostName() throws Exception {
    final String CosmoString="{\"time\":\"2023-02-21T11:37:17.0556567Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/\\/RESOURCEGROUPS\\/AZURE-COSMOS\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/DEV-COSMO\",\"category\":\"DataPlaneRequests\",\"operationName\":\"Create\",\"properties\":{\"activityId\":\"880d1d92-159c-4134-ae35-9c4e48807fd3\",\"requestResourceType\":\"DatabaseFeed\",\"requestResourceId\":\"\\/dbs\",\"collectionRid\":\"\",\"databaseRid\":\"\",\"statusCode\":\"201\",\"duration\":\"444.264900\",\"userAgent\":\"Microsoft.Azure.Documents.Common\\/2.14.0\",\"clientIpAddress\":\"20.193.136.102\",\"requestCharge\":\"1.000000\",\"requestLength\":\"25\",\"responseLength\":\"175\",\"resourceTokenPermissionId\":\"\",\"resourceTokenPermissionMode\":\"\",\"resourceTokenUserRid\":\"\",\"region\":\"Central India\",\"partitionId\":\"\",\"aadAppliedRoleAssignmentId\":\"\",\"aadPrincipalId\":\"\",\"authTokenType\":\"PrimaryMasterKey\",\"keyType\":\"PrimaryMasterKey\",\"connectionMode\":\"Gateway\",\"subscriptionId\":\"\",\"databaseName\":\"\",\"collectionName\":\"\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("cosmos.azure.com",record.getAccessor().getServerHostName());
    }
    
    @Test
    public void testparseException() throws Exception {
	final String CosmoString="{\"time\":\"2023-02-21T11:37:17.0556567Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMOS\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/DEV-COSMO\",\"category\":\"DataPlaneRequests\",\"operationName\":\"Create\",\"properties\":{\"activityId\":\"880d1d92-159c-4134-ae35-9c4e48807fd3\",\"requestResourceType\":\"DatabaseFeed\",\"requestResourceId\":\"\\/dbs\",\"collectionRid\":\"\",\"databaseRid\":\"\",\"statusCode\":\"400\",\"duration\":\"444.264900\",\"userAgent\":\"Microsoft.Azure.Documents.Common\\/2.14.0\",\"clientIpAddress\":\"20.193.136.102\",\"requestCharge\":\"1.000000\",\"requestLength\":\"25\",\"responseLength\":\"175\",\"resourceTokenPermissionId\":\"\",\"resourceTokenPermissionMode\":\"\",\"resourceTokenUserRid\":\"\",\"region\":\"Central India\",\"partitionId\":\"\",\"aadAppliedRoleAssignmentId\":\"\",\"aadPrincipalId\":\"\",\"authTokenType\":\"PrimaryMasterKey\",\"keyType\":\"PrimaryMasterKey\",\"connectionMode\":\"Gateway\",\"subscriptionId\":\"083de1fb-cd2d-4b7c-895a-2b5af1d09e8\",\"databaseName\":\"\",\"collectionName\":\"\"}}";
    final JsonObject CosmoJson = JsonParser.parseString(CosmoString).getAsJsonObject();
    Record record=Parser.parseRecord(CosmoJson);
    assertEquals("Error (400)",record.getException().getDescription());
    assertEquals("SQL_ERROR",record.getException().getExceptionTypeId());

    
    }
}
    
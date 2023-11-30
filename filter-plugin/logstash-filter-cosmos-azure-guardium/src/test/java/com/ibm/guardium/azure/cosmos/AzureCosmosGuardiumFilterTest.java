/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.azure.cosmos;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.universalconnector.commons.GuardConstants;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class AzureCosmosGuardiumFilterTest {
	
	final static Context context = new ContextImpl(null, null);
	final static  AzureCosmosGuardiumFilter filter = new AzureCosmosGuardiumFilter("test-id", null, context);

	@Test
	public void testControlPlaneRequest() {
		final String CosmoString="{ \"time\": \"2023-03-13T09:56:41.5889464Z\", \"resourceId\": \"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8/RESOURCEGROUPS/AZURE-COSMO/PROVIDERS/MICROSOFT.DOCUMENTDB/DATABASEACCOUNTS/COSMOS-DATABASE\", \"subscriptionId\": \"083de1fb-cd2d-4b7c-895a-2b5af1d091e8\", \"operationName\": \"SqlContainersDelete\", \"category\": \"ControlPlaneRequests\", \"properties\": {\"activityId\": \"5520ee10-c185-11ed-8bc3-12f484077d92\",\"httpstatusCode\": \"204\",\"result\": \"OK\",\"httpMethod\": \"DELETE\",\"apiKind\": \"Sql\",\"apiKindResourceType\": \"Containers\",\"operationType\": \"Delete\",\"resourceUri\": \"sqlDatabases/MyDatabase/containers/products\",\"resourceDetails\": \"\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testDataplaneRequest() {
		final String CosmoString="{\"operationName\":\"Update\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/AZURE-COSMO/PROVIDERS/MICROSOFT.DOCUMENTDB/DATABASEACCOUNTS/COSMOS-DATABASE\",\"time\":\"2023-04-17T09:22:27.6222250Z\",\"category\":\"DataPlaneRequests\",\"properties\":{\"collectionRid\":\"\",\"databaseName\":\"testdatabase\",\"resourceTokenPermissionMode\":\"\",\"responseLength\":\"256\",\"requestLength\":\"248\",\"requestCharge\":\"10.290000\",\"collectionName\":\"testcontainer\",\"duration\":\"15.914100\",\"resourceTokenPermissionId\":\"\",\"activityId\":\"94d17bef-1970-468c-8ec4-a12bc02f5a99\",\"requestResourceType\":\"Document\",\"connectionMode\":\"Gateway\",\"keyType\":\"PrimaryMasterKey\",\"resourceTokenUserRid\":\"\",\"requestResourceId\":\"/dbs/testdatabase/colls/testcontainer/docs/testQA\",\"clientIpAddress\":\"49.35.174.206\",\"partitionId\":\"\",\"authTokenType\":\"PrimaryMasterKey\",\"aadPrincipalId\":\"2d2a524f-a589-4577-bf29-d88c19da732g\",\"userAgent\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36 Edg/112.0.1722.48\",\"aadAppliedRoleAssignmentId\":\"\",\"databaseRid\":\"\",\"region\":\"Central India\",\"subscriptionId\":\"083de1fb-cd2d-4b7c-895a-2b5af1d091e8\",\"statusCode\":\"200\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testCreateItem() {
		final String CosmoString="{\"time\":\"2023-02-21T07:12:28.4915560Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083FE1FC-CD4D-5B6C-895B-2B5AF1D082F4\\/RESOURCEGROUPS\\/AZURE-COSMOS\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/DEV-COSMO\",\"category\":\"DataPlaneRequests\",\"operationName\":\"Create\",\"properties\":{\"activityId\":\"8085f7db-b625-4cb9-8a42-1dc2ad023855\",\"requestResourceType\":\"DocumentFeed\",\"requestResourceId\":\"\\/dbs\\/FamiliesDatabase\\/colls\\/Families\\/docs\",\"collectionRid\":\"\",\"databaseRid\":\"\",\"statusCode\":\"201\",\"duration\":\"4.398400\",\"userAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/110.0.0.0 Safari\\/537.36 Edg\\/110.0.1587.50\",\"clientIpAddress\":\"122.161.50.240\",\"requestCharge\":\"14.290000\",\"requestLength\":\"441\",\"responseLength\":\"653\",\"resourceTokenPermissionId\":\"\",\"resourceTokenPermissionMode\":\"\",\"resourceTokenUserRid\":\"\",\"region\":\"Central India\",\"partitionId\":\"\",\"aadAppliedRoleAssignmentId\":\"\",\"aadPrincipalId\":\"\",\"authTokenType\":\"PrimaryMasterKey\",\"keyType\":\"PrimaryMasterKey\",\"connectionMode\":\"Gateway\",\"subscriptionId\":\"083fe1fc-cd4d-5b6c-895b-2b5af1d082f4\",\"databaseName\":\"FamiliesDatabase\",\"collectionName\":\"Families\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testRoleDefinition() {
		final String CosmoString="{ \"time\": \"2023-03-07T07:20:22.3934738Z\", \"resourceId\": \"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/AZURE_COSMOS_RG/PROVIDERS/MICROSOFT.DOCUMENTDB/DATABASEACCOUNTS/AZURECOSMODATABASE\", \"subscriptionId\": \"083de1fb-cd2d-4b7c-895a-2b5af1d091e8\", \"operationName\": \"SqlRoleDefinitionCreateComplete\", \"category\": \"ControlPlaneRequests\", \"properties\": {\"activityId\": \"7f9f46a4-bcb8-11ed-9cfc-6ecba8553b82\",\"httpstatusCode\": \"200\",\"httpMethod\": \"PUT\",\"duration\": \"5665\",\"result\": \"OK\",\"roleDefinitionId\": \"be79875a-2cc4-40d5-8958-566017875b39\",\"roleDefinitionName\": \"My Read Only Role\",\"roleDefinitionType\": \"CustomRole\",\"roleDefinitionAssignableScopes\": \"/dbs/Families/colls/family\",\"roleDefinitionPermissions\": \"{dataActions:[Microsoft.DocumentDB/databaseAccounts/readMetadata,Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers/items/read,Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers/executeQuery,Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers/readChangeFeed],notDataActions:[]}\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testRoleAssignment() {
		final String CosmoString="{\"time\":\"2023-06-05T10:04:43.1673116Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8\\/RESOURCEGROUPS\\/COSMOS-RESOURCE\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DB-DATABASE\",\"subscriptionId\":\"083de1fb-cd2d-4b7c-895a-2b5af1d091e8\",\"operationName\":\"SqlRoleAssignmentCreateComplete\",\"category\":\"ControlPlaneRequests\",\"properties\":{\"activityId\":\"59e162e6-0388-11ee-9734-06fbd7d78a1a\",\"httpstatusCode\":\"200\",\"httpMethod\":\"PUT\",\"duration\":\"11856\",\"result\":\"OK\",\"roleAssignmentId\":\"cb8ed2d7-2371-4e3c-bd31-6cc1560e84f8\",\"associatedRoleDefinitionId\":\"00000000-0000-0000-0000-000000000002\",\"roleAssignmentPrincipalId\":\"2d2a524f-a589-4577-bf29-d88c19da732f\",\"roleAssignmentPrincipalType\":\"AadUser\",\"roleAssignmentScope\":\"\\/dbs\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testQueryRuntimeStatics() {
		final String CosmoString="{\"time\":\"2023-02-21T06:03:28.9493677Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083FE1FC-CD4D-5B6C-895B-2B5AF1D082F4\\/RESOURCEGROUPS\\/AZURE-COSMOS\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/DEV-COSMO\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"f1810797-e32e-406d-af59-2280fea8001e\",\"databasename\":\"FamiliesDatabase\",\"collectionname\":\"Families\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083fe1fc-cd4d-5b6c-895b-2b5af1d082f4\",\"resourcegroupname\":\"Azure-cosmos\",\"partialipaddress\":\"52.140.110.66\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"2\",\"signature\":\"-1661408583400352360\",\"shapesignature\":\"8235320023077058687\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT f._rid, [{\\\\\\\"item\\\\\\\": f.address.city}] AS orderByItems, {\\\\\\\"givenName\\\\\\\": c.givenName} AS payload\\\\nFROM Families AS f\\\\nJOIN c IN f.children \\\\n WHERE ((f.id = \\\\\\\"WakefieldFamily\\\\\\\") AND (true))\\\\nORDER BY f.address.city ASC\\\",\\\"parameters\\\":[]}\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testNestedQuery() {
		final String CosmoString="{\"time\":\"2023-02-21T06:03:28.9493677Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083FE1FC-CD4D-5B6C-895B-2B5AF1D082F4\\/RESOURCEGROUPS\\/AZURE-COSMOS\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/DEV-COSMO\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"f1810797-e32e-406d-af59-2280fea8001e\",\"databasename\":\"FamiliesDatabase\",\"collectionname\":\"Families\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083fe1fc-cd4d-5b6c-895b-2b5af1d082f4\",\"resourcegroupname\":\"Azure-cosmos\",\"partialipaddress\":\"52.140.110.66\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"2\",\"signature\":\"-1661408583400352360\",\"shapesignature\":\"8235320023077058687\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT TOP 5 f.id, (SELECT VALUE Count(1) FROM n IN f.nutrients WHERE n.units = 'mg') AS count_mg FROM food f\\\",\\\"parameters\\\":[]}\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testJoinQuery() {
		final String CosmoString="{ \"time\": \"2023-03-28T11:48:09.7683235Z\", \"resourceId\": \"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8/RESOURCEGROUPS/AZURE-COSMO/PROVIDERS/MICROSOFT.DOCUMENTDB/DATABASEACCOUNTS/COSMOS-DATABASE\", \"category\": \"QueryRuntimeStatistics\", \"properties\": { \"activityId\": \"a4081ca7-70b8-4d73-aca9-4e92044cbe4d\", \"databasename\": \"cosmoDB\", \"collectionname\": \"collection\", \"partitionkeyrangeid\": \"0\", \"useragent\": \"Microsoft.Azure.Documents.Common/2.14.0RoutingGateway\", \"subscriptionid\": \"083de1fb-cd2d-4b7c-895a-2b5af1d091e8\", \"resourcegroupname\": \"azure-cosmo\", \"partialipaddress\": \"40.80.50.2\", \"regionname\": \"Central India\", \"authtype\": \"1\", \"numberofrowsreturned\": \"1\", \"signature\": \"-4576541951102199744\", \"shapesignature\": \"-8612658657970479756\", \"queryexecutionstatus\": \"Finished\", \"querytext\": \"{\\\"query\\\":\\\"SELECT c.givenName FROM Families f JOIN c IN f.children WHERE f.id = 'WakefieldFamily' ORDER BY f.address.city ASC\\\",\\\"parameters\\\":[]}\" } } ";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testCountQuery() {
		final String CosmoString="{\"time\":\"2023-03-29T12:55:56.2461944Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"QueryRuntimeStatistics\",\"properties\":{\"activityId\":\"0b76d57d-d22d-4cb2-b912-7726e24b69ba\",\"databasename\":\"cosmoDB\",\"collectionname\":\"Families\",\"partitionkeyrangeid\":\"0\",\"useragent\":\"Microsoft.Azure.Documents.Common\\/2.14.0RoutingGateway\",\"subscriptionid\":\"083de1fb-cd2d-4b7c-895a-2b5af1d091e8\",\"resourcegroupname\":\"azure-cosmo\",\"partialipaddress\":\"40.80.50.2\",\"regionname\":\"Central India\",\"authtype\":\"1\",\"numberofrowsreturned\":\"1\",\"signature\":\"2363625393322487707\",\"shapesignature\":\"7572324026122958197\",\"queryexecutionstatus\":\"Finished\",\"querytext\":\"{\\\"query\\\":\\\"SELECT {\\\\\\\"Count\\\\\\\": {\\\\\\\"item\\\\\\\": Count(1)}} AS payload\\\\nFROM c\\\\nJOIN t IN c.tags\\\\nJOIN n IN c.nutrients\\\\nJOIN s IN c.servings\\\\nWHERE (((t.name = \\\\\\\"infant formula\\\\\\\") AND ((n.nutritionValue > 0) AND (n.nutritionValue < 10))) AND (s.amount > 1))\\\",\\\"parameters\\\":[]}\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testWhereQuery() {
		final String CosmoString="{ \"time\": \"2023-03-30T07:11:58.6788768Z\", \"resourceId\": \"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8/RESOURCEGROUPS/AZURE-COSMO/PROVIDERS/MICROSOFT.DOCUMENTDB/DATABASEACCOUNTS/COSMOS-DATABASE\", \"category\": \"QueryRuntimeStatistics\", \"properties\": {\"activityId\": \"5003af5f-34c4-4bcc-a46f-e1e5b38159f9\", \"databasename\": \"cosmoDB\", \"collectionname\": \"Families\", \"partitionkeyrangeid\": \"0\", \"useragent\": \"Microsoft.Azure.Documents.Common/2.14.0RoutingGateway\", \"subscriptionid\": \"083de1fb-cd2d-4b7c-895a-2b5af1d091e8\", \"resourcegroupname\": \"azure-cosmo\", \"partialipaddress\": \"40.80.50.2\", \"regionname\": \"Central India\", \"authtype\": \"1\", \"numberofrowsreturned\": \"1\", \"signature\": \"4036375829063283830\", \"shapesignature\": \"-8612658657970479756\", \"queryexecutionstatus\": \"Finished\", \"querytext\": \"{\\\"query\\\":\\\"SELECT *\\\\r\\\\n    FROM Families f\\\\r\\\\n    WHERE f.id = 'AndersenFamily'\\\",\\\"parameters\\\":[]}\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testGetRequest() {
		final String CosmoString="{\"time\":\"2023-04-05T04:28:57.2134014Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMO\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/COSMOS-DATABASE\",\"category\":\"DataPlaneRequests\",\"operationName\":\"Read\",\"properties\":{\"activityId\":\"ade18404-f074-426f-91a9-2247143c4bb6\",\"requestResourceType\":\"DatabaseFeed\",\"requestResourceId\":\"\\/dbs\",\"collectionRid\":\"\",\"databaseRid\":\"\",\"statusCode\":\"200\",\"duration\":\"71.166500\",\"userAgent\":\"PostmanRuntime\\/7.31.3\",\"clientIpAddress\":\"157.34.229.88\",\"requestCharge\":\"2.000000\",\"requestLength\":\"0\",\"responseLength\":\"203\",\"resourceTokenPermissionId\":\"\",\"resourceTokenPermissionMode\":\"\",\"resourceTokenUserRid\":\"\",\"region\":\"Central India\",\"partitionId\":\"\",\"aadAppliedRoleAssignmentId\":\"\",\"aadPrincipalId\":\"\",\"authTokenType\":\"PrimaryMasterKey\",\"keyType\":\"PrimaryMasterKey\",\"connectionMode\":\"Gateway\",\"subscriptionId\":\"083de1fb-cd2d-4b7c-895a-2b5af1d091e8\",\"databaseName\":\"\",\"collectionName\":\"\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testSQLError() {
		final String CosmoString="{\"time\":\"2023-02-21T11:37:17.0556567Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMOS\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/DEV-COSMO\",\"category\":\"DataPlaneRequests\",\"operationName\":\"Create\",\"properties\":{\"activityId\":\"880d1d92-159c-4134-ae35-9c4e48807fd3\",\"requestResourceType\":\"DatabaseFeed\",\"requestResourceId\":\"\\/dbs\",\"collectionRid\":\"\",\"databaseRid\":\"\",\"statusCode\":\"400\",\"duration\":\"444.264900\",\"userAgent\":\"Microsoft.Azure.Documents.Common\\/2.14.0\",\"clientIpAddress\":\"20.193.136.102\",\"requestCharge\":\"1.000000\",\"requestLength\":\"25\",\"responseLength\":\"175\",\"resourceTokenPermissionId\":\"\",\"resourceTokenPermissionMode\":\"\",\"resourceTokenUserRid\":\"\",\"region\":\"Central India\",\"partitionId\":\"\",\"aadAppliedRoleAssignmentId\":\"\",\"aadPrincipalId\":\"\",\"authTokenType\":\"PrimaryMasterKey\",\"keyType\":\"PrimaryMasterKey\",\"connectionMode\":\"Gateway\",\"subscriptionId\":\"083de1fb-cd2d-4b7c-895a-2b5af1d091e8\",\"databaseName\":\"\",\"collectionName\":\"\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testEmpty() {
		final String CosmoString="";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}
	@Test
	public void testMessage() {
		final String CosmoString="{\"time\":\"2023-02-21T11:37:17.0556567Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMOS\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/DEV-COSMO\",\"category\":\"DataPlaneRequests\",\"operationName\":\"Create\",\"properties\":{\"activityId\":\"880d1d92-159c-4134-ae35-9c4e48807fd3\",\"requestResourceType\":\"DatabaseFeed\",\"requestResourceId\":\"\\/dbs\",\"collectionRid\":\"\",\"databaseRid\":\"\",\"statusCode\":\"201\",\"duration\":\"444.264900\",\"userAgent\":\"Microsoft.Azure.Documents.Common\\/2.14.0\",\"clientIpAddress\":\"20.193.136.102\",\"requestCharge\":\"1.000000\",\"requestLength\":\"25\",\"responseLength\":\"175\",\"resourceTokenPermissionId\":\"\",\"resourceTokenPermissionMode\":\"\",\"resourceTokenUserRid\":\"\",\"region\":\"Central India\",\"partitionId\":\"\",\"aadAppliedRoleAssignmentId\":\"\",\"aadPrincipalId\":\"\",\"authTokenType\":\"PrimaryMasterKey\",\"keyType\":\"PrimaryMasterKey\",\"connectionMode\":\"Gateway\",\"subscriptionId\":\"083de1fb-cd2d-4b7c-895a-2b5af1d091e8\",\"databaseName\":\"\",\"collectionName\":\"\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testCategory() {
		final String CosmoString="{\"time\":\"2023-02-21T11:37:17.0556567Z\",\"resourceId\":\"\\/SUBSCRIPTIONS\\/083DE1FB-CD2D-4B7C-895A-2B5AF1D09E8\\/RESOURCEGROUPS\\/AZURE-COSMOS\\/PROVIDERS\\/MICROSOFT.DOCUMENTDB\\/DATABASEACCOUNTS\\/DEV-COSMO\",\"category\":\"Unknown\",\"operationName\":\"Create\",\"properties\":{\"activityId\":\"880d1d92-159c-4134-ae35-9c4e48807fd3\",\"requestResourceType\":\"DatabaseFeed\",\"requestResourceId\":\"\\/dbs\",\"collectionRid\":\"\",\"databaseRid\":\"\",\"statusCode\":\"201\",\"duration\":\"444.264900\",\"userAgent\":\"Microsoft.Azure.Documents.Common\\/2.14.0\",\"clientIpAddress\":\"20.193.136.102\",\"requestCharge\":\"1.000000\",\"requestLength\":\"25\",\"responseLength\":\"175\",\"resourceTokenPermissionId\":\"\",\"resourceTokenPermissionMode\":\"\",\"resourceTokenUserRid\":\"\",\"region\":\"Central India\",\"partitionId\":\"\",\"aadAppliedRoleAssignmentId\":\"\",\"aadPrincipalId\":\"\",\"authTokenType\":\"SystemReadWrite\",\"keyType\":\"SystemReadWrite\",\"connectionMode\":\"Gateway\",\"subscriptionId\":\"083de1fb-cd2d-4b7c-895a-2b5af1d091e8\",\"databaseName\":\"\",\"collectionName\":\"\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testInvalidRecords() {
		final String CosmoString="{\"records\":\"\"}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", CosmoString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}
	
	
	@Test
	public void getIdTest() {
		final String CosmoString="";
		Event e = new org.logstash.Event();
		e.setField("message", CosmoString);
		String id=filter.getId();
	}
}
class TestMatchListener implements FilterMatchListener {
	private AtomicInteger matchCount = new AtomicInteger(0);

	public int getMatchCount() {
		return matchCount.get();
	}

	@Override
	public void filterMatched(co.elastic.logstash.api.Event arg0) {
		matchCount.incrementAndGet();

	}
}
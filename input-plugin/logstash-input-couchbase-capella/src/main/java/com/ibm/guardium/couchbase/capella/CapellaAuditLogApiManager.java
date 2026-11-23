package com.ibm.guardium.couchbase.capella;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import com.google.gson.Gson;
import org.apache.http.*;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CapellaAuditLogApiManager {
    private static Logger log = LogManager.getLogger(CapellaAuditLogApiManager.class);
    private static final String HTTPS_PROXY_HOST = "https_proxy";
    private static final int RETRY_BASE = 10;

    public static String getHttpsProxyHost() {
        String gEnv = System.getenv(HTTPS_PROXY_HOST);
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = System.getProperty(HTTPS_PROXY_HOST);
        }
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = null;
        }


        gEnv = System.getenv(HTTPS_PROXY_HOST.toUpperCase());
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = System.getProperty(HTTPS_PROXY_HOST.toUpperCase());
        }
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = null;
        }
        return gEnv;
    }

    private static CloseableHttpClient buildHttpClient(){
        CloseableHttpClient httpClient = null;
        String proxyHost = getHttpsProxyHost();
        if (proxyHost!=null) {
            httpClient = HttpClients
                    .custom()
                    .setRoutePlanner(new DefaultProxyRoutePlanner(HttpHost.create(proxyHost)))
                    .build();
            if (log.isDebugEnabled()) {
                log.debug("using proxy "+proxyHost);
            }
        } else {
            httpClient = HttpClients.createDefault();
        };
        return httpClient;
    }



    /** Create an export job with Capella API:
     * <a href="https://docs.couchbase.com/cloud/management-api-reference/index.html#tag/Audit-Logs/operation/postAuditLogExport">
     *     Create Cluster Audit Log Export job</a>
     * @return If success, return the id of the export job; otherwise, return null.
     */
    public String createExportJob(String baseUrl, String orgID, String projID, String clusterID,
                                  String token, String startTime, String endTime) {
        URL url;
        try {
            url = new URL(baseUrl);

            DigestScheme digestScheme = new DigestScheme();

            String requester = InetAddress.getLocalHost().toString();
            digestScheme.overrideParamter("realm", requester);
            digestScheme.overrideParamter("nonce", Long.toString(new Random().nextLong(), 36));

        } catch (MalformedURLException e) {
            log.error("invalid base URL {}", e);
            return null;
        } catch (UnknownHostException e) {
            log.error("unknown host", e);
            return null;
        }

        try( CloseableHttpClient httpClient =  buildHttpClient();) {

            HttpClientContext context = HttpClientContext.create();

            String endpoint = String.format("%s/organizations/%s/projects/%s/clusters/%s/auditLogExports",
                    baseUrl, orgID, projID, clusterID);

            final String reqBody = String.format("{\"start\":\"%s\",\"end\":\"%s\"}", startTime, endTime);
            final StringEntity entity = new StringEntity(reqBody);
            HttpPost httppost = new HttpPost(endpoint);
            httppost.setHeader("Accept", "application/json");
            httppost.addHeader("Content-Type", "application/json");
            httppost.addHeader("Authorization", token);


            httppost.setEntity(entity);
            HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
            CloseableHttpResponse response = httpClient.execute(targetHost, httppost, context);

            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity e = response.getEntity();
            String responseString = e != null ? EntityUtils.toString(e, "UTF-8") : "";
            Gson gson = new Gson();
            if (statusCode == HttpStatus.SC_ACCEPTED) {
                CreateClusterAuditLogExportJobResponse res
                        = gson.fromJson(responseString, CreateClusterAuditLogExportJobResponse.class);

                response.close();
                return res != null ? res.exportId : null;
            }

            ErrorResponse res = null;
            try {
                res = gson.fromJson(responseString, ErrorResponse.class);
            } catch (Exception parseEx) {
                log.debug("Could not parse ErrorResponse JSON: {}", parseEx.getMessage());
            }

            if (res != null) {
                log.error("Request failed, httpStatusCode {}, code {}, message {}, hint {}, raw response {}",
                        res.httpStatusCode != 0 ? res.httpStatusCode : statusCode, res.code, res.message, res.hint, responseString);
            } else {
                log.error("Request failed, httpStatusCode {}, raw response {}", statusCode, responseString);
            }
            response.close();
            return null;

        } catch (Exception e) {
            log.error("Create cluster audit log export job exception {}" , e);
            return null;
        }
    }

    /**
     *  Get the status of audit log export job with Capella API:
     *  <a href="https://docs.couchbase.com/cloud/management-api-reference/index.html#tag/Audit-Logs/operation/postAuditLogExport">
     *  Get Cluster Audit Log Export</a>
     * @param baseUrl the Capella REST endpoint baseURL, e.g. https://cloudapi.cloud.couchbase.com/v4/
     * @param orgID organization ID
     * @param projID project ID
     * @param clusterID cluster ID
     * @param token Bearer token for the HTTP Authorization header, e.g. Bearer xyz=
     * @param exportJobID the ID of the audit log export job
     * @return if success, return the auditLogDownloadURL; otherwise return null
     */
    public String getExportJobStatus( String baseUrl, String orgID, String projID, String clusterID,
                                      String token, String exportJobID) {
        return getExportJobStatusWithRetry(baseUrl, orgID, projID, clusterID, token, exportJobID, 2);
    }

    /**
     *  Get the status of audit log export job with Capella API:
     *  <a href="https://docs.couchbase.com/cloud/management-api-reference/index.html#tag/Audit-Logs/operation/postAuditLogExport">
     *  Get Cluster Audit Log Export</a>
     * @param baseUrl the Capella REST endpoint baseURL, e.g. https://cloudapi.cloud.couchbase.com/v4/
     * @param orgID organization ID
     * @param projID project ID
     * @param clusterID cluster ID
     * @param token Bearer token for the HTTP Authorization header, e.g. Bearer xyz=
     * @param exportJobID the ID of the audit log export job
     * @param retryLimit the limit of retry
     * @return if success, return the auditLogDownloadURL; otherwise return null
     */
    protected String getExportJobStatusWithRetry( String baseUrl, String orgID, String projID, String clusterID,
                                                  String token, String exportJobID, int retryLimit) {
        URL url;
        try {
            url = new URL(baseUrl);

            DigestScheme digestScheme = new DigestScheme();

            String requester = InetAddress.getLocalHost().toString();
            digestScheme.overrideParamter("realm", requester);
            digestScheme.overrideParamter("nonce", Long.toString(new Random().nextLong(), 36));

        } catch (MalformedURLException e) {
            log.error("invalid base URL {}", e);
            return null;
        } catch (UnknownHostException e) {
            log.error("unknown host", e);
            return null;
        }

        try( CloseableHttpClient httpClient = buildHttpClient();) {

            HttpClientContext context = HttpClientContext.create();

            String endpoint = String.format("%s/organizations/%s/projects/%s/clusters/%s/auditLogExports/%s",
                    baseUrl, orgID, projID, clusterID, exportJobID);

            HttpGet request = new HttpGet(endpoint);
            request.setHeader("Accept", "application/json");
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Authorization", token);


            HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
            int retry = 0;
            int statusCode = 0;
            GetClusterAuditLogExportResponse res = new GetClusterAuditLogExportResponse();
            CloseableHttpResponse response = null;
            while (retry <= retryLimit) {
                log.debug("getExportJob retry loop: retry {}, retryLimit {}", retry, retryLimit);
                response = httpClient.execute(targetHost, request, context);
                statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {

                    HttpEntity entity = response.getEntity();
                    String responseString = entity != null ? EntityUtils.toString(entity, "UTF-8") : "";

                    Gson gson = new Gson();
                    try {
                        res = gson.fromJson(responseString, GetClusterAuditLogExportResponse.class);
                    } catch (Exception parseEx) {
                        log.debug("Could not parse GetClusterAuditLogExportResponse JSON: {}", parseEx.getMessage());
                    }

                    if (res != null && res.status != null && res.status.equalsIgnoreCase("completed")) {
                        response.close();
                        return res.auditLogDownloadURL;
                    }
                }

                // Important: close response before sleep !!
                response.close();
                if (retryLimit != 0) {
                    int sleep =  (retry+1)*RETRY_BASE ;
                    log.debug("getExportJob sleep {} seconds", sleep);
                    TimeUnit.SECONDS.sleep(sleep);
                }
                retry++;

            }

            String resStatus = (res != null && res.status != null) ? res.status : "unknown";
            log.error("Fail to get completed audit log export job, http status code is {}, status is {}",
                    statusCode, resStatus);
            if (response != null) {
                response.close();
            }
            return null;
        } catch (Exception e) {
            log.error("Get audit log export job status exception {}" , e);
            return null;
        }
    }

    /**
     * Downloads gzip file from the downloadURL and processes each line using lineConsumer.
     * @param downloadURL the URL to download log gz
     * @param lineConsumer consumer to process each line
     * @return true if successfully downloaded and processed; false otherwise
     */
    public boolean downloadAuditLogFile(String downloadURL, Consumer<String> lineConsumer) {
        return downloadAuditLogFileWithRetry(downloadURL, lineConsumer, 2);
    }

    /**
     * Downloads gzip file from downloadURL and processes each line using lineConsumer while adhering to the retryLimit.
     * Each successive failure to connect will result in an exponential backoff.
     * @param downloadURL the URL to download log gz
     * @param lineConsumer consumer to process each line
     * @param retryLimit the limit of retry times
     * @return true if successfully downloaded and processed; false otherwise
     */
    protected boolean downloadAuditLogFileWithRetry(String downloadURL, Consumer<String> lineConsumer, int retryLimit) {
        URL url;
        try {
            url = new URL(downloadURL);
        } catch (MalformedURLException e) {
            log.error("invalid base URL {}", e);
            return false;
        }

        try (CloseableHttpClient httpClient = buildHttpClient()) {
            HttpClientContext context = HttpClientContext.create();

            HttpGet request = new HttpGet(downloadURL);
            request.addHeader("Accept", "application/gzip");

            HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
            int retry = 0;
            int statusCode = 0;
            while (retry <= retryLimit) {
                try (CloseableHttpResponse response = httpClient.execute(targetHost, request, context)) {
                    statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        HttpEntity entity = response.getEntity();
                        if (entity != null && (entity.getContentLength() > 0 || entity.isChunked())) {
                            try (InputStream inputStream = entity.getContent();
                                 GZIPInputStream gzipIn = new GZIPInputStream(inputStream);
                                 BufferedReader reader = new BufferedReader(new InputStreamReader(gzipIn, StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    lineConsumer.accept(line);
                                }
                                return true;
                            }
                        } else {
                            log.error("Empty response body from the downloading URL");
                        }
                    }
                } catch (Exception e) {
                    log.error("Error during download attempt {}: {}", retry, e.getMessage());
                }

                if (retryLimit != 0 && retry < retryLimit) {
                    int sleep = (retry + 1) * RETRY_BASE;
                    log.debug("downloadAuditLog sleep {} seconds", sleep);
                    TimeUnit.SECONDS.sleep(sleep);
                }
                retry++;
            }
            log.error("Failed to download audit log file, httpStatusCode {}", statusCode);
            return false;
        } catch (Exception e) {
            log.error("Download audit log tar.gz file exception {}", e);
            return false;
        }
    }
}
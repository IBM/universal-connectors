package com.ibm.guardium.mongodb;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.http.*;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
/**
 * handle calling mongo atlas api
 */
public class MongoApi {
    private static Logger log = LogManager.getLogger(MongoApi.class);
    private static String MONGO_API_URL= "https://cloud.mongodb.com/api/atlas/v1.0/groups/";


    /**
     * get the logs as string after unzipped form mongo atlas - null on error
     *
     * @param publicKey
     * @param privateKey
     * @param groupId
     * @param hostname
     * @param fileName
     * @param startDateInEpoch
     * @param endDateInEpoch
     * @return
     */
    public static String getResponseFromJsonURL(String publicKey, String privateKey, String groupId, String hostname, String fileName, long startDateInEpoch, long endDateInEpoch) {
        //build url
        String url = MONGO_API_URL + groupId + "/clusters/" + hostname + "/logs/"+fileName+"?startDate=" + startDateInEpoch + "&endDate=" + endDateInEpoch;
        if (log.isDebugEnabled()) {
            log.debug(url);
        }
        try {
            URL object = new URL(url);
            //get the file unzipped from url
            return downloadAndUnzipWithDigestAuth(object, url, publicKey, privateKey);
        } catch (Exception e) {
            log.error("Failed to downlod logs file",e);

        }
        return null;
    }

    /**
     * unzip input stream as string - return null on error
     *
     * @param inStr
     * @return
     * @throws Exception
     */
    private static String unzip(InputStream inStr) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); GZIPInputStream in = new GZIPInputStream(inStr)) {
            byte[] buffer = new byte[4096];
            int c = 0;
            while ((c = in.read(buffer, 0, 4096)) > 0) {
                out.write(buffer, 0, c);
            }
            return out.toString(StandardCharsets.UTF_8.toString());
        } catch (Exception ex) {
            log.error("Failed to unzip gz response",ex);
            return null;
        }

    }

    /**
     * @param url
     * @param path
     * @param publicKey
     * @param privateKey
     * @return
     * @throws Exception
     */
    private static String downloadAndUnzipWithDigestAuth(URL url, String path, String publicKey, String privateKey)
            throws Exception {
        HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpClientContext context = HttpClientContext.create();
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(publicKey, privateKey));
        AuthCache authCache = new BasicAuthCache();
        DigestScheme digestScheme = new DigestScheme();
        authCache.put(targetHost, digestScheme);
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        HttpGet httpget = new HttpGet(path);
        httpget.addHeader("Accept", "application/gzip");
        CloseableHttpResponse response = httpClient.execute(targetHost, httpget, context);
        if (log.isDebugEnabled()) {
            log.debug("before unauth response.getStatusLine().getStatusCode() = " + response.getStatusLine().getStatusCode());
        }
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            Header authHeader = response.getFirstHeader(AUTH.WWW_AUTH);
            digestScheme = new DigestScheme();
            digestScheme.overrideParamter("realm", "User Login Required !!");
            digestScheme.processChallenge(authHeader);

            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(publicKey, privateKey);
            httpget.addHeader(digestScheme.authenticate(creds, httpget, context));

            response.close();
            response = httpClient.execute(targetHost, httpget, context);
            if (log.isDebugEnabled()) {
                log.debug("before unauth response.getStatusLine().getStatusCode() = " + response.getStatusLine().getStatusCode());
            }
        }
        final HttpEntity entity = response.getEntity();
        if (entity != null) {
            try (InputStream inputStream = entity.getContent()) {
                return unzip(inputStream);
            }
        }
        return null;
    }

    private static volatile boolean stop = false;

}

package com.ibm.guardium.mongodb;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
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
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * handle calling mongo atlas api
 */
public class MongoApi {
    private static Logger log = LogManager.getLogger(MongoApi.class);
    private static String MONGO_API_URL= "https://cloud.mongodb.com/api/atlas/v1.0/groups/";
    private static final String HTTPS_PROXY_HOST = "https_proxy";
    private static final String HTTPS_PROXY_HOST_UP = "HTTPS_PROXY";

    public static String getHttpsProxyHost() {
        String gEnv = System.getenv(HTTPS_PROXY_HOST);
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = System.getProperty(HTTPS_PROXY_HOST);
        }
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = null;
        }
        gEnv = System.getenv(HTTPS_PROXY_HOST_UP);
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = System.getProperty(HTTPS_PROXY_HOST_UP);
        }
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = null;
        }
        return gEnv;
    }

    private static CloseableHttpClient buildHttpClient(URL url){
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

    /**
     * get the logs as string after unzipped form mongo atlas - null on error
     *
     * @param publicKey
     * @param privateKey
     * @param groupId
     * @param hostname
     * @param mongoApiUrl
     * @param fileName
     * @param startDateInEpoch
     * @param endDateInEpoch
     * @return
     */
    public static String getResponseFromJsonURL(String publicKey, String privateKey, String groupId, String mongoApiUrl, String hostname, String fileName, long startDateInEpoch, long endDateInEpoch) {
        // Use default API URL if mongoApiUrl is null or empty
        if (mongoApiUrl == null || mongoApiUrl.isEmpty()) {
            log.info("MongoDB API URL is not set, using default: {}", MONGO_API_URL);
            mongoApiUrl = MONGO_API_URL;
        }

        //build url
        String url = mongoApiUrl + groupId + "/clusters/" + hostname + "/logs/"+fileName+"?startDate=" + startDateInEpoch + "&endDate=" + endDateInEpoch;
        if (log.isDebugEnabled()) {
            log.debug("Constructed MongoDB Atlas API URL: {}", url);
        }

        try {
            URL object = new URL(url);
            //get the file unzipped from url
            return downloadAndUnzipWithDigestAuth(object, url, publicKey, privateKey);
        } catch (Exception e) {
            log.error("Failed to downlod logs file for group id "+groupId+", hostname "+hostname+", fileName "+fileName+", startDateInEpoch "+startDateInEpoch+", endDateInEpoch "+endDateInEpoch,e);
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
        if (log.isDebugEnabled()) {
            log.debug("path path  = " + path);
        }

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(publicKey, privateKey));

        HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        DigestScheme digestScheme = new DigestScheme();
        
        String requester = InetAddress.getLocalHost().toString();
        digestScheme.overrideParamter("realm", requester);
        digestScheme.overrideParamter("nonce", Long.toString(new Random().nextLong(), 36));

        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, digestScheme);

        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        CloseableHttpClient httpClient =  buildHttpClient(url);

        HttpGet httpget = new HttpGet(path);
        httpget.addHeader("Accept", "application/gzip");

        CloseableHttpResponse response = httpClient.execute(targetHost, httpget, context);
        if (log.isDebugEnabled()) {
            log.debug("response.getStatusLine().getStatusCode() = " + response.getStatusLine().getStatusCode());
        }

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            Header authHeader = response.getFirstHeader(AUTH.WWW_AUTH);
            digestScheme = new DigestScheme();
            digestScheme.overrideParamter("realm", requester);
            digestScheme.overrideParamter("nonce", Long.toString(new Random().nextLong(), 36));
            
            digestScheme.processChallenge(authHeader);

            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(publicKey, privateKey);
            httpget.addHeader(digestScheme.authenticate(creds, httpget, context));

            response.close();
            response = httpClient.execute(targetHost, httpget, context);
            if (log.isDebugEnabled()) {
                log.debug("with digest - response.getStatusLine().getStatusCode() = " + response.getStatusLine().getStatusCode());
            }
        }

        if (response.getStatusLine().getStatusCode() < HttpStatus.SC_BAD_REQUEST) {
            final HttpEntity entity = response.getEntity();
            if (entity != null && ( entity.getContentLength() > 0 || entity.isChunked())) {
                try (InputStream inputStream = entity.getContent()) {
                    return unzip(inputStream);
                }
            }
            log.debug("no data was recieved on path "+path+", response is "+response.getStatusLine());
            return null;
        }

        log.error("Failed to get data on path "+path+", response is "+response.getStatusLine());
        return null;
    }

    private static volatile boolean stop = false;

}

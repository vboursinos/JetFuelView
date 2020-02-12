package headfront.utils;

import org.apache.http.HttpHost;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


/**
 * Created by Deepak on 16/09/2016.
 */
public class WebServiceRequest {

    private static final Logger LOG = LoggerFactory.getLogger(WebServiceRequest.class);
    private static SSLContext acceptAnySSl = null;
    private static final int TIMEOUT_INTERVAL = 30000;

    static {
        // accept everything
        TrustManager[] allTrustManager = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};
        try {
            acceptAnySSl = SSLContext.getInstance("SSL");
            acceptAnySSl.init(null, allTrustManager, new java.security.SecureRandom());
        } catch (Exception e) {
            LOG.error("Unable to set up a SSL Context", e);
        }
    }

    public static String doWebRequests(String fullUrl) {
        String httpProtocol = fullUrl.substring(0, fullUrl.indexOf("//")) + "//";
        String url = "";
        try {
            // added this so we can time out correctly.
            CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(acceptAnySSl)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

            HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
            httpRequestFactory.setConnectionRequestTimeout(TIMEOUT_INTERVAL);
            httpRequestFactory.setConnectTimeout(TIMEOUT_INTERVAL);
            httpRequestFactory.setReadTimeout(TIMEOUT_INTERVAL);
            httpRequestFactory.setHttpClient(httpClient);

            RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
            AuthCache authCache = new BasicAuthCache();
            String plainCreds = fullUrl.split("@")[0].replace(httpProtocol, "");
            String[] credParts = plainCreds.split(":");
            url = fullUrl.replace(plainCreds + "@", "");
            String hostname = url.replace(httpProtocol, "").split(":")[0];
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(new HttpHost(hostname), basicAuth);

            BasicHttpContext localcontext = new BasicHttpContext();
            localcontext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
            restTemplate.getInterceptors().add(
                    new BasicAuthorizationInterceptor(credParts[0], credParts[1]));
            ResponseEntity<String> exchange = restTemplate.exchange(
                    url,
                    HttpMethod.GET, null, String.class);
            return exchange.getBody();
        } catch (Exception e) {
            LOG.error("Could not process request " + StringUtils.removePassword(url) + "  Error Details " + e.getMessage());
        }
        return null;
    }

    public static void main(String args[]) {
        String url = "https://www.google.co.uk";
        final String result = WebServiceRequest.doWebRequests(url);
        System.out.println("Got url " + result);
    }

}

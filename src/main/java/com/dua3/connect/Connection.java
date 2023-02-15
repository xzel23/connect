package com.dua3.connect;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.win.WinHttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

/**
 * A connection interface that unifies access to network resources by using Windows authorization
 * if available. Use the static {@code create()} methods to create instances.
 */
public interface Connection extends AutoCloseable {

    record HeaderParameter(String name, Object value) {}

    /**
     * Open Stream for reading.
     * @return InputStream
     * @throws IOException on error
     */
    InputStream openInputStream() throws IOException;

    // override to narrow the exception specification
    @Override
    void close() throws IOException;

    /**
     * Create connection to a URI given as a string.
     * @param uri the URI
     * @param parameters header parameters to set
     * @return the connection
     */
    static Connection create(String uri, HeaderParameter... parameters) {
        return create(URI.create(uri), parameters);
    }

    /**
     * Create connection to a {@link URL}
     * @param url the URL
     * @param parameters header parameters to set
     * @return the connection
     */
    static Connection create(URL url, HeaderParameter... parameters) {
        try {
            return create(url.toURI(), parameters);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Create connection to a {@link URI}
     * @param uri the URI
     * @param parameters header parameters to set
     * @return the connection
     */
    static Connection create(URI uri, HeaderParameter... parameters) {
        return new ConnectionImp(uri, parameters);
    }

    class ConnectionImp implements Connection {
        private static final Logger LOG = LoggerFactory.getLogger(ConnectionImp.class);

        final CloseableHttpClient httpclient;
        private final URI uri;
        private final HeaderParameter[] parameters;
        CloseableHttpResponse response;

        public ConnectionImp(URI uri, HeaderParameter... parameters) {
            this.uri = uri;
            this.httpclient = getHttpClient();
            this.parameters = parameters;
            this.response = null;
        }

        private static CloseableHttpClient getHttpClient() {
            if (WinHttpClients.isWinAuthAvailable()) {
                return WinHttpClients.createDefault();
            } else {
                return HttpClients.createDefault();
            }
        }

        @Override
        public InputStream openInputStream() throws IOException {
            LOG.debug("opening connection to {}", uri);
            HttpUriRequest request = new HttpGet(uri);
            Arrays.stream(parameters).forEach(p -> request.setHeader(p.name(), p.value()));
            response = httpclient.execute(request);
            LOG.debug("response: {}", response.getReasonPhrase());

            if (response.getCode()==401) {
                throw new IllegalStateException("unauthorized: "+response.getReasonPhrase());
            }

            return response.getEntity().getContent();
        }

        @Override
        public void close() throws IOException {
            try {
                if (response != null) {
                    LOG.debug("closing response: {}", uri);
                    response.close();
                }
            } finally {
                LOG.debug("closing connection: {}", uri);
                httpclient.close();
            }
        }
    }
}

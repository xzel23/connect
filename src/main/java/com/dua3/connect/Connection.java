package com.dua3.connect;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.win.WinHttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * A connection interface that unifies access to network resources by using Windows authorization
 * if available. Use the static {@code create()} methods to create instances.
 */
public interface Connection extends AutoCloseable {

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
     * @return the connection
     */
    static Connection create(String uri) {
        return create(URI.create(uri));
    }

    /**
     * Create connection to a {@link URL}
     * @param url the URL
     * @return the connection
     */
    static Connection create(URL url) {
        try {
            return create(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Create connection to a {@link URI}
     * @param uri the URI
     * @return the connection
     */
    static Connection create(URI uri) {
        if (!WinHttpClients.isWinAuthAvailable()) {
            LoggerHolder.LOG.fine("windows-authentification not supported!");
            return new Connection() {

                private InputStream stream = null;

                @Override
                public InputStream openInputStream() throws IOException {
                    LoggerHolder.LOG.fine(() -> "opening connection: " + uri);
                    stream = uri.toURL().openStream();
                    return stream;
                }

                @Override
                public void close() throws IOException {
                    LoggerHolder.LOG.fine(() -> "closing connection: " + uri);
                    if (stream!=null) {
                        stream.close();
                    }
                }
            };
        } else {
            LoggerHolder.LOG.fine("windows-authentification supported.");
            return new Connection() {
                final CloseableHttpClient httpclient = WinHttpClients.createDefault();
                CloseableHttpResponse response = null;

                @Override
                public InputStream openInputStream() throws IOException {
                    LoggerHolder.LOG.fine(() -> "opening connection (using winauth): " + uri);
                    HttpUriRequest httpget = new HttpGet(uri);
                    response = httpclient.execute(httpget);
                    LoggerHolder.LOG.fine(() -> "response: " + response.getReasonPhrase());
                    if (response.getCode()==401) {
                        throw new IllegalStateException("unauthorized: "+response.getReasonPhrase());
                    }
                    return response.getEntity().getContent();
                }

                @Override
                public void close() throws IOException {
                    LoggerHolder.LOG.fine(() -> "closing connection: " + uri);
                    try {
                        if (response != null) {
                            response.close();
                        }
                    } finally {
                        httpclient.close();
                    }
                }
            };
        }
    }
}

/**
 * Helper class to hold the logger reference.
 */
@SuppressWarnings("LoggerInitializedWithForeignClass")
final class LoggerHolder {
    static final Logger LOG = Logger.getLogger(Connection.class.getName());

    private LoggerHolder() {
    }
}

package com.dua3.connect;

import com.dua3.cabe.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * A connection interface that unifies access to network resources by using Windows authorization
 * if available. Use the static {@code create()} methods to create instances.
 */
public interface Connection extends AutoCloseable {

    /**
     * Record holding HTTP header parameter and its value.
     * @param name the HTTP parameter name
     * @param value the parameter value
     */
    record HeaderParameter(String name, @Nullable Object value) {}

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

}

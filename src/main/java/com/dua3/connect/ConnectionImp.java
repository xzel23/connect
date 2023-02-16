package com.dua3.connect;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.win.WinHttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

class ConnectionImp implements Connection {
    private static final Logger LOG = LoggerFactory.getLogger(com.dua3.connect.ConnectionImp.class);

    final CloseableHttpClient httpclient;
    private final URI uri;
    private final HeaderParameter[] parameters;
    CloseableHttpResponse response;

    ConnectionImp(URI uri, HeaderParameter... parameters) {
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

        if (response.getCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new IllegalStateException("unauthorized: " + response.getReasonPhrase());
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

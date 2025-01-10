package com.certimeter.asset.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class LogEndpointFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(LogEndpointFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Wrapping the request to capture the body
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(req);

        // Wrapping the response to capture the status
        StatusCaptureResponseWrapper responseWrapper = new StatusCaptureResponseWrapper(res);

        LOG.info("--------- START - GET {}?{} ------", req.getRequestURI(), req.getQueryString());
        LOG.info("Endpoint: {}", req.getRequestURI());
        LOG.info("Method: {}", req.getMethod());
        LOG.info("Query string: {}", req.getQueryString());
        LOG.info("Request body: {}", wrappedRequest.getBody());
        chain.doFilter(wrappedRequest, responseWrapper);

        // Log the status after the chain is executed
        LOG.info("Response status: {}", responseWrapper.getStatus());
        LOG.info("---------- END - GET {} ------", req.getRequestURI());
    }
}

class StatusCaptureResponseWrapper extends HttpServletResponseWrapper {

    private int status;

    public StatusCaptureResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void setStatus(int sc) {
        super.setStatus(sc);
        this.status = sc;
    }

    @Override
    public void sendError(int sc) throws IOException {
        super.sendError(sc);
        this.status = sc;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        super.sendError(sc, msg);
        this.status = sc;
    }

    @Override
    public int getStatus() {
        return this.status != 0 ? this.status : super.getStatus();
    }
}

class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final String body;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            stringBuilder.append(bufferedReader.lines().collect(Collectors.joining(System.lineSeparator())));
        }
        body = stringBuilder.toString();
    }

    public String getBody() {
        return this.body;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }
}
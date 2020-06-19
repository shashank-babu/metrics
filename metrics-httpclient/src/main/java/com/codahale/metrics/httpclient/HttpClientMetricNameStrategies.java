package com.codahale.metrics.httpclient;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

import static com.codahale.metrics.MetricRegistry.name;

public class HttpClientMetricNameStrategies {

    public static final HttpClientMetricNameStrategy METHOD_ONLY = new HttpClientMetricNameStrategy() {

        @Override
        public String getRequestName(String serviceName, HttpRequest request) {
            return name(HttpClient.class,
                    serviceName,
                    methodName(request),
                    requestName());
        }

        @Override
        public String getResponseName(String serviceName, HttpRequest request, HttpResponse response) {
            return name(HttpClient.class,
                    serviceName,
                    methodName(request),
                    responseName(response)
            );
        }
    };

    public static final HttpClientMetricNameStrategy HOST_AND_METHOD = new HttpClientMetricNameStrategy() {
        @Override
        public String getRequestName(String serviceName, HttpRequest request) {
            return name(HttpClient.class,
                    serviceName,
                    requestURI(request).getHost(),
                    requestName());
        }

        @Override
        public String getResponseName(String serviceName, HttpRequest request, HttpResponse httpResponse) {
            return name(HttpClient.class,
                    serviceName,
                    requestURI(request).getHost(),
                    responseName(httpResponse));
        }
    };

    public static final HttpClientMetricNameStrategy PATH_AND_METHOD = new HttpClientMetricNameStrategy() {
        @Override
        public String getRequestName(String serviceName, HttpRequest request) {
            final URIBuilder url = new URIBuilder(requestURI(request));
            return name(HttpClient.class,
                    serviceName,
                    url.getPath(),
                    requestName());
        }

        @Override
        public String getResponseName(String serviceName, HttpRequest request, HttpResponse response) {
            final URIBuilder url = new URIBuilder(requestURI(request));
            return name(HttpClient.class,
                    serviceName,
                    url.getPath(),
                    responseName(response));
        }
    };

    public static final HttpClientMetricNameStrategy QUERYLESS_URL_AND_METHOD = new HttpClientMetricNameStrategy() {
        @Override
        public String getRequestName(String serviceName, HttpRequest request) {
            try {
                final URIBuilder url = new URIBuilder(requestURI(request));
                return name(HttpClient.class,
                        serviceName,
                        url.removeQuery().build().toString(),
                        requestName());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public String getResponseName(String serviceName, HttpRequest request, HttpResponse httpResponse) {
            try {
                final URIBuilder url = new URIBuilder(requestURI(request));
                return name(HttpClient.class,
                        serviceName,
                        url.removeQuery().build().toString(),
                        responseName(httpResponse));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
    };

    private static String requestName() {
        return "request";
    }

    private static String methodName(HttpRequest request) {
        return request.getRequestLine().getMethod().toLowerCase();
    }

    private static String responseName(HttpResponse response) {
        return String.format("%sxx", response.getStatusLine().getStatusCode() % 100);
    }

    private static URI requestURI(HttpRequest request) {
        if (request instanceof HttpRequestWrapper)
            return requestURI(((HttpRequestWrapper) request).getOriginal());

        return (request instanceof HttpUriRequest) ?
                ((HttpUriRequest) request).getURI() :
                URI.create(request.getRequestLine().getUri());
    }
}

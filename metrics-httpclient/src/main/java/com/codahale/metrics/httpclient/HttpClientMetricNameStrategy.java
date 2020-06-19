package com.codahale.metrics.httpclient;

import com.codahale.metrics.MetricRegistry;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

public interface HttpClientMetricNameStrategy {

    String getRequestName(String name, HttpRequest request);

    String getResponseName(String name, HttpRequest request, HttpResponse response);

    default String getNameFor(String name, Exception exception) {
        return MetricRegistry.name(HttpClient.class,
                name,
                exception.getClass().getSimpleName());
    }
}

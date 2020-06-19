package com.codahale.metrics.httpclient;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;

import com.codahale.metrics.Meter;

import java.io.IOException;

public class InstrumentedHttpRequestExecutor extends HttpRequestExecutor {
    private final MetricRegistry registry;
    private final HttpClientMetricNameStrategy metricNameStrategy;
    private final String name;

    public InstrumentedHttpRequestExecutor(MetricRegistry registry,
                                           HttpClientMetricNameStrategy metricNameStrategy) {
        this(registry, metricNameStrategy, null);
    }

    public InstrumentedHttpRequestExecutor(MetricRegistry registry,
                                           HttpClientMetricNameStrategy metricNameStrategy,
                                           String name) {
        this(registry, metricNameStrategy, name, HttpRequestExecutor.DEFAULT_WAIT_FOR_CONTINUE);
    }

    public InstrumentedHttpRequestExecutor(MetricRegistry registry,
                                           HttpClientMetricNameStrategy metricNameStrategy,
                                           String name,
                                           int waitForContinue) {
        super(waitForContinue);
        this.registry = registry;
        this.name = name;
        this.metricNameStrategy = metricNameStrategy;
    }

    @Override
    public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context) throws HttpException, IOException {
        try(Timer.Context timerContext = requestTimer(request).time()) {
            HttpResponse response = super.execute(request, conn, context);
            responseMeter(request, response);
            return  response;
        } catch (HttpException | IOException e) {
            meter(e).mark();
            throw e;
        }
    }

    private Timer requestTimer(HttpRequest request) {
        return registry.timer(metricNameStrategy.getRequestName(name, request));
    }

    private Meter meter(Exception e) {
        return registry.meter(metricNameStrategy.getNameFor(name, e));
    }

    private Meter responseMeter(HttpRequest httpRequest, HttpResponse httpResponse) {
        return registry.meter(metricNameStrategy.getResponseName(name, httpRequest, httpResponse));
    }
}

package org.apache.isis.core.tracing;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;

public class Configuration2 extends Configuration {
    public Configuration2(final String serviceName) {
        super(serviceName);
    }

    @Override protected JaegerTracer.Builder createTracerBuilder(final String serviceName) {
        return new JaegerTracerBuilder2(serviceName);
    }

}

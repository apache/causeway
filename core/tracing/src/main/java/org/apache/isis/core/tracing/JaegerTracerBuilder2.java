package org.apache.isis.core.tracing;

import io.jaegertracing.internal.JaegerTracer;

class JaegerTracerBuilder2 extends JaegerTracer.Builder {
    JaegerTracerBuilder2(final String serviceName) {
        super(serviceName, new JaegerObjectFactory2());
    }

}

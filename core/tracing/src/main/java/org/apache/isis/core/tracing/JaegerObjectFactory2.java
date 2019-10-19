package org.apache.isis.core.tracing;

import java.util.List;
import java.util.Map;

import io.jaegertracing.internal.JaegerObjectFactory;
import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.JaegerSpanContext;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.Reference;

class JaegerObjectFactory2 extends JaegerObjectFactory {
    @Override public JaegerSpan createSpan(
            final JaegerTracer tracer,
            final String operationName,
            final JaegerSpanContext context,
            final long startTimeMicroseconds,
            final long startTimeNanoTicks,
            final boolean computeDurationViaNanoTicks,
            final Map<String, Object> tags,
            final List<Reference> references) {
        return new JaegerSpan2(tracer, operationName, context, startTimeMicroseconds, startTimeNanoTicks,
                computeDurationViaNanoTicks, tags, references);
    }

}

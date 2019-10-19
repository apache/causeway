package org.apache.isis.core.tracing;

import java.util.List;
import java.util.Map;

import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.JaegerSpanContext;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.Reference;

class JaegerSpan2 extends JaegerSpan implements Span2 {

    private boolean finished;
    private Scope2 scope2;

    public JaegerSpan2(
            final JaegerTracer tracer,
            final String operationName,
            final JaegerSpanContext context,
            final long startTimeMicroseconds,
            final long startTimeNanoTicks,
            final boolean computeDurationViaNanoTicks,
            final Map<String, Object> tags,
            final List<Reference> references) {
        super(tracer, operationName, context, startTimeMicroseconds, startTimeNanoTicks, computeDurationViaNanoTicks, tags, references);
    }

    @Override
    public void finish() {
        super.finish();
        finished = true;
    }

    @Override
    public void finish(final long finishMicros) {
        super.finish(finishMicros);
        finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public Scope2 scope() {
        return scope2;
    }

    @Override public boolean hasTag(final String tag) {
        return getTags().keySet().contains(tag);
    }

    void setScope(final Scope2 scope2) {
        this.scope2 = scope2;
    }

}

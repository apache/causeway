import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.micrometer.MicrometerMetricsFactory;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import tracing.ThreadLocalScope2;
import tracing.ThreadLocalScopeManager2;

public class Dummy {

    public static void main(String[] args) throws InterruptedException {

        GlobalTracer.registerIfAbsent(() -> {

            final Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv()
                    .withType(ConstSampler.TYPE)
                    .withParam(1);

            final Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv()
                    .withLogSpans(true)
                    .withSender(Configuration.SenderConfiguration.fromEnv());

            final Configuration config = new Configuration("dummy")
                    .withSampler(samplerConfig)
                    .withReporter(reporterConfig);

            final MicrometerMetricsFactory metricsReporter = new MicrometerMetricsFactory();

            return config
                    .getTracerBuilder()
                    .withScopeManager(new ThreadLocalScopeManager2())
                    .withMetricsFactory(metricsReporter)
                    .build();
        });

        //explicit();
        decoupled();

        // allow agent to complete...
        Thread.sleep(1000);
    }

    private static void decoupled() throws InterruptedException {

        // creates a child (but top-level if no existing parent)
        // we don't need to hold onto the scope created; we can look it up later (see end of this method)
        final ThreadLocalScope2 unused =
                ThreadLocalScopeManager2.get().childSpan("outer-Z");

        Thread.sleep(300);


        // for nested scope, option (1) is handle scope and close
        ThreadLocalScope2 tracingScope = ThreadLocalScopeManager2.get().childSpan("inner-1");
        try {
            Thread.sleep(500);


            // for nested scope, option (2) is to use a callable or runnable
            ThreadLocalScopeManager2.get().callInSpanEx("inner-2", () -> {
                Thread.sleep(500);
                return null;
            });


        } finally {
            tracingScope.closeAndFinish();
        }

        Thread.sleep(200);

        // can look up the existing scope, don't need to pass through
        final ThreadLocalScope2 scope = ThreadLocalScopeManager2.get().activeScope();
        if(scope != null) {
            scope.closeAndFinish();
        }

    }

    private static void innerSpan() throws InterruptedException {
        ThreadLocalScope2 tracingScope = ThreadLocalScopeManager2.get().childSpan("inner-1");
        try {
            Thread.sleep(500);
            innerSpan2();
        } finally {
            tracingScope.closeAndFinish();
        }
    }

    private static void innerSpan2() {
        ThreadLocalScopeManager2.get().callInSpanEx("inner-2", () -> {
            Thread.sleep(500);
            return null;
        });
    }



    private static void explicit() throws InterruptedException {
        final Tracer tracer = GlobalTracer.get();

        final Tracer.SpanBuilder spanBuilder = tracer.buildSpan("outer-1");

        final Span outer = spanBuilder.start();
        final Scope outerScope = tracer.activateSpan(outer);

        Thread.sleep(300);

        final Span inner = tracer.buildSpan("inner-1").asChildOf(outer).start();
        final Scope innerScope = tracer.activateSpan(inner);

        Thread.sleep(500);
        inner.finish();
        innerScope.close();

        Thread.sleep(200);
        outer.finish();
        outerScope.close();
    }

    private static void explicitWithLogging() throws InterruptedException {
        final Tracer tracer = GlobalTracer.get();

        final Tracer.SpanBuilder spanBuilder = tracer.buildSpan("outer-1");
        final Span outer = spanBuilder.start();
        final Scope outerScope = tracer.activateSpan(outer);

        Thread.sleep(300);

        final Span span = tracer.activeSpan();
        final SpanContext context = span.context();
        final Span inner = tracer.buildSpan("inner-1").asChildOf(outer).start();
        final Scope innerScope = tracer.activateSpan(inner);
        Thread.sleep(500);
        final Span span1 = tracer.scopeManager().activeSpan();
        inner.finish();
        innerScope.close();
        final Span span3 = tracer.activeSpan();
        final Span spanAfterInnerFinish = tracer.scopeManager().activeSpan();

        final Span span2 = tracer.activeSpan();
        Thread.sleep(200);
        outer.finish();
        final Span span4 = tracer.activeSpan();
        outerScope.close();
        final Span span5 = tracer.activeSpan();
    }

}

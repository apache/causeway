package org.apache.isis.core.tracing;

import java.util.concurrent.Callable;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.micrometer.MicrometerMetricsFactory;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;



/**
 *
 * to demo, use:
 *
 * docker run -d -p 5775:5775/udp -p 16686:16686 jaegertracing/all-in-one:latest
 *
 * And access the Jaeger console at: http://localhost:16686)
 *
 * This demo then programmatically sets up the following properties:
 *
 *   -DJAEGER_AGENT_HOST=localhost -DJAEGER_AGENT_PORT=5775
 *
 * These are picked up in the call to Configuration.ReporterConfiguration.fromEnv().
 */
public class TracingDemoMain {

    public static void main(String[] args) throws InterruptedException {

        init();

        runScenario();

        // allow agent to flush UDP packets etc...
        Thread.sleep(500);
    }

    private static void init() {
        System.setProperty("JAEGER_AGENT_HOST", "localhost");
        System.setProperty("JAEGER_AGENT_PORT", "5775");

        GlobalTracer.registerIfAbsent(new Callable<Tracer>() {
            @Override public Tracer call() throws Exception {

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
            }
        });
    }

    private static void runScenario() throws InterruptedException {

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
            ThreadLocalScopeManager2.get().callInSpanEx("inner-2", new Callable<Object>() {
                @Override public Object call() throws Exception {
                    Thread.sleep(500);
                    return null;
                }
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



}

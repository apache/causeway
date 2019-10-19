/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.webapp;

import java.util.concurrent.Callable;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.isis.core.tracing.Configuration2;
import org.apache.isis.core.tracing.TraceScopeManager;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.micrometer.MicrometerMetricsFactory;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import lombok.val;

/**
 * This implementation of {@link ServletContextListener} should be registered in the <code>web.xml</code>, along with {@link io.opentracing.contrib.web.servlet.filter.TracingFilter}.
 * Together, these will cause traces to be sent to Jaeger.
 *
 * In terms of its implementation, this bootstrapper sets up a {@link Tracer} as a singleton,
 * accessible using {@link GlobalTracer#get()}. This is then picked up by the filter.
 *
 * It also installs a custom variant of {@link io.opentracing.util.ThreadLocalScopeManager}, namely
 * {@link TraceScopeManager}.  Together with {@link org.apache.isis.core.tracing.ThreadLocalScope2}, this provides some convenience APIs and makes it possible to close a scope
 * in a different method (though must be in the same thread) as the method that opened the scope.
 *
 * See the <code>TracingDemoMain</code> class (in <code>isis-core-tracing</code> module) for
 * more details.
 *
 * to demo, use:
 *
 * docker run -d -p 5775:5775/udp -p 16686:16686 jaegertracing/all-in-one:latest
 *
 * And access the Jaeger console at: http://localhost:16686)
 *
 * Then run the app with the following properties:
 *
 *   -DJAEGER_AGENT_HOST=localhost -DJAEGER_AGENT_PORT=5775
 *
 * These are picked up in the call to Configuration.ReporterConfiguration.fromEnv().
 */
public class OpenTracingBootstrapper implements ServletContextListener {


    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {

        final String openTracingServiceName =
                initParamFrom(servletContextEvent, "openTracingServiceName", "apache-isis-app");

        GlobalTracer.registerIfAbsent(new Callable<Tracer>() {
            @Override public Tracer call() {

                val samplerConfig = Configuration.SamplerConfiguration.fromEnv()
                        .withType(ConstSampler.TYPE)
                        .withParam(1);
                val reporterConfig = Configuration.ReporterConfiguration.fromEnv()
                        .withLogSpans(true)
                        .withSender(Configuration.SenderConfiguration.fromEnv());


                val config = new Configuration2(openTracingServiceName)
                        .withSampler(samplerConfig)
                        .withReporter(reporterConfig)
                        ;

                // https://github.com/jaegertracing/jaeger-client-java/tree/master/jaeger-micrometer
                val metricsReporter = new MicrometerMetricsFactory();

                return config
                        .getTracerBuilder()
                        .withScopeManager(new TraceScopeManager())

                        .withMetricsFactory(metricsReporter)
                        .build();
            }
        });

    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {

    }

    private static String initParamFrom(
            final ServletContextEvent ev,
            final String paramName,
            final String defaultValue) {
        final String value = ev.getServletContext().getInitParameter(paramName);
        return value != null ? value : defaultValue;
    }


}

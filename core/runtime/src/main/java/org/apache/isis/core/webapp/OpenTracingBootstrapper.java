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

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.micrometer.MicrometerMetricsFactory;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import lombok.val;

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

                // to demo, use:
                //
                // * docker run -d -p 5775:5775/udp -p 16686:16686 jaegertracing/all-in-one:latest
                // * access at http://localhost:16686)
                //
                // * run the app with:
                //
                //   -DJAEGER_AGENT_HOST=localhost -DJAEGER_AGENT_PORT=5775
                //
                val reporterConfig = Configuration.ReporterConfiguration.fromEnv()
                        .withLogSpans(true)
                        .withSender(Configuration.SenderConfiguration.fromEnv());


                val config = new Configuration(openTracingServiceName)
                        .withSampler(samplerConfig)
                        .withReporter(reporterConfig)
                        ;

                // https://github.com/jaegertracing/jaeger-client-java/tree/master/jaeger-micrometer
                val metricsReporter = new MicrometerMetricsFactory();

                return config
                        .getTracerBuilder()
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

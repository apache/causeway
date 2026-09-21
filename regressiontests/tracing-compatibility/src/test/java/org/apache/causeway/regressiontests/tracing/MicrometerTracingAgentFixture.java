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
package org.apache.causeway.regressiontests.tracing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelBaggageManager;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Runs in a child JVM so the compatibility test can attach the real OpenTelemetry Java agent.
 */
@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration
public final class MicrometerTracingAgentFixture {

    static final String OBSERVATION_NAME = "causeway.compatibility.jdbc";
    static final String SUCCESS_MARKER = "CAUSEWAY_TRACING_FIXTURE_OK";

    private MicrometerTracingAgentFixture() {
    }

    public static void main(final String[] args) throws Exception {
        try (ConfigurableApplicationContext ignored = new SpringApplicationBuilder(
                MicrometerTracingAgentFixture.class)
                .web(WebApplicationType.NONE)
                .logStartupInfo(false)
                .properties("spring.main.banner-mode=off")
                .run(args)) {
            final ObservationRegistry observationRegistry = observationRegistry();
            final Observation observation = Observation.createNotStarted(
                    OBSERVATION_NAME, observationRegistry);

            observation.start();
            try (Observation.Scope scope = observation.openScope()) {
                executeJdbcWork();
            } catch (Exception ex) {
                observation.error(ex);
                throw ex;
            } finally {
                observation.stop();
            }

            System.out.println(SUCCESS_MARKER);
        }
    }

    private static ObservationRegistry observationRegistry() {
        final OtelCurrentTraceContext currentTraceContext = new OtelCurrentTraceContext();
        final OtelBaggageManager baggageManager = new OtelBaggageManager(
                currentTraceContext,
                Collections.emptyList(),
                Collections.emptyList());
        final Tracer tracer = new OtelTracer(
                GlobalOpenTelemetry.getTracer("org.apache.causeway.compatibility"),
                currentTraceContext,
                event -> { },
                baggageManager);

        final ObservationRegistry observationRegistry = ObservationRegistry.create();
        observationRegistry.observationConfig()
                .observationHandler(new DefaultTracingObservationHandler(tracer));
        return observationRegistry;
    }

    private static void executeJdbcWork() throws Exception {
        Class.forName("org.h2.Driver");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:causeway-tracing;DB_CLOSE_DELAY=-1")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("create table trace_probe (id integer primary key, name varchar(32))");
                statement.executeUpdate("insert into trace_probe (id, name) values (1, 'compatible')");
                try (ResultSet resultSet = statement.executeQuery(
                        "select name from trace_probe where id = 1")) {
                    if (!resultSet.next() || !"compatible".equals(resultSet.getString(1))) {
                        throw new IllegalStateException("Unexpected JDBC probe result");
                    }
                }
            }
        }
    }
}

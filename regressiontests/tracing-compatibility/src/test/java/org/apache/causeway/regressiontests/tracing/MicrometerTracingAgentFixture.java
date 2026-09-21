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

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import org.apache.causeway.core.config.observation.CausewayObservationConfiguration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.ObservationClosure;

/**
 * Child-process application launched by {@link MicrometerTracingCompatibilityTest}.
 *
 * <p>The fixture needs a {@code main} method because the automated JUnit test starts a new JVM
 * with the real OpenTelemetry {@code -javaagent} attached before application classes load.</p>
 */
@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration
@Import(CausewayObservationConfiguration.class)
public final class MicrometerTracingAgentFixture {

    static final String OBSERVATION_NAME = "causeway.compatibility.jdbc";
    static final String SUCCESS_MARKER = "CAUSEWAY_TRACING_FIXTURE_OK";

    private MicrometerTracingAgentFixture() {
    }

    public static void main(final String[] args) throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                MicrometerTracingAgentFixture.class)
                .web(WebApplicationType.NONE)
                .profiles("observation")
                .logStartupInfo(false)
                .properties("spring.main.banner-mode=off")
                .run(args)) {
            final CausewayObservationIntegration observationIntegration = context.getBean(
                    CausewayObservationIntegration.class);
            final ObservationClosure observationClosure = new ObservationClosure()
                    .startAndOpenScope(observationIntegration.createNotStarted(
                            MicrometerTracingAgentFixture.class,
                            OBSERVATION_NAME));

            try {
                executeJdbcWork();
            } catch (Exception | Error ex) {
                observationClosure.onError(ex);
                throw ex;
            } finally {
                observationClosure.close();
            }

            System.out.println(SUCCESS_MARKER);
        }
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

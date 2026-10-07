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

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.Test;

import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;

class MicrometerTracingCompatibilityTest {
    @Test void agentJoinsFrameworkAndJdbcWithoutDuplicateSpans() throws Exception {
        try (var collector = new Collector()) {
            launch("agent", collector);
            assertFalse(collector.metricNames.isEmpty(), "Micrometer metrics must export despite OTEL_METRICS_EXPORTER=none");
            var spans = collector.spans;
            var roots = spans.stream().filter(s -> s.getName().equals("Causeway Root Interaction")).toList();
            assertEquals(2, roots.size(), names(spans));
            assertEquals(1, roots.stream().filter(s -> s.getStatus().getCode() == Status.StatusCode.STATUS_CODE_ERROR).count());
            assertNotEquals(roots.get(0).getTraceId(), roots.get(1).getTraceId());
            for (var root : roots) {
                var http = spans.stream().filter(s -> s.getSpanId().equals(root.getParentSpanId())).findFirst().orElseThrow(() -> new AssertionError(names(spans)));
                assertEquals(Span.SpanKind.SPAN_KIND_SERVER, http.getKind());
                assertEquals(http.getTraceId(), root.getTraceId());
                var actions = spans.stream().filter(s -> s.getParentSpanId().equals(root.getSpanId()) && s.getName().startsWith("Action ")).toList();
                assertEquals(1, actions.size(), names(spans));
                var action = actions.get(0);
                assertEquals(root.getTraceId(), action.getTraceId());
                assertJpaAncestry(spans, action, true);
                assertIdentity(root, "sentinel-user", "sentinel-tenant");
            }
        }
    }
    @Test void obsoleteJpaThresholdDoesNotDiscardExportedParents() throws Exception {
        for (String mode : List.of("boot", "agent")) {
            try (var collector = new Collector()) {
                launch(mode, collector, "--causeway.observation.jpa-duration-threshold=1d");
                var actions = collector.spans.stream().filter(s -> s.getName().startsWith("Action ")).toList();
                assertEquals(2, actions.size(), names(collector.spans));
                for (var action : actions) assertJpaAncestry(collector.spans, action, mode.equals("agent"));
                assertFalse(collector.spans.stream().anyMatch(s -> attribute(s, "causeway.discard") != null));
            }
        }
    }
    @Test void bootDefaultsRetainJpaParentsAndIncludeIdentity() throws Exception {
        try (var collector = new Collector()) {
            launch("boot", collector);
            var actions = collector.spans.stream().filter(s -> s.getName().startsWith("Action ")).toList();
            assertEquals(2, actions.size(), names(collector.spans));
            for (var action : actions) assertJpaAncestry(collector.spans, action, false);
            collector.spans.stream().filter(s -> s.getName().equals("Causeway Root Interaction"))
                    .forEach(s -> assertIdentity(s, "sentinel-user", "sentinel-tenant"));
        }
    }
    @Test void identityIsExportedByDefaultInBothModes() throws Exception {
        for (String mode : List.of("boot", "agent")) {
            try (var collector = new Collector()) {
                launch(mode, collector);
                var roots = collector.spans.stream().filter(s -> s.getName().equals("Causeway Root Interaction")).toList();
                assertEquals(2, roots.size(), names(collector.spans));
                roots.forEach(s -> assertIdentity(s, "sentinel-user", "sentinel-tenant"));
            }
        }
    }
    private void assertIdentity(Span span, String userName, String token) {
        var attributes = span.getAttributesList().stream().collect(java.util.stream.Collectors.toMap(
                a -> a.getKey(), a -> a.getValue().getStringValue()));
        assertEquals(userName, attributes.get("causeway.user.name"));
        assertEquals(token, attributes.get("causeway.user.multiTenancyToken"));
    }
    private void assertJpaAncestry(List<Span> spans, Span action, boolean agent) {
        var parents = spans.stream().filter(s -> s.getParentSpanId().equals(action.getSpanId())
                && s.getName().startsWith("Persist ")).toList();
        assertEquals(1, parents.size(), names(spans));
        var jpa = parents.get(0);
        assertEquals("Persist " + org.apache.causeway.persistence.jpa.integration.entity.JpaObservationFixture.Entity.class.getName(), jpa.getName());
        assertEquals(action.getTraceId(), jpa.getTraceId());
        assertTrue(spans.stream().anyMatch(s -> s.getParentSpanId().equals(jpa.getSpanId())
                && s.getTraceId().equals(jpa.getTraceId())
                && (agent ? s.getKind() == Span.SpanKind.SPAN_KIND_CLIENT : s.getName().equals("fixture.jdbc"))), names(spans));
        assertFalse((jpa.getName() + jpa.getAttributesList()).contains("sentinel"));
        assertTrue(action.getAttributesList().stream().anyMatch(a -> a.getKey().equals("causeway.member.id")));
    }
    @Test void inactiveProfileLeavesAutomaticAgentSpansWorking() throws Exception {
        try (var collector = new Collector()) {
            launch("inactive", collector);
            assertTrue(collector.spans.stream().anyMatch(s -> s.getKind() == Span.SpanKind.SPAN_KIND_SERVER), names(collector.spans));
            assertFalse(collector.spans.stream().anyMatch(s -> s.getName().contains("Causeway Root Interaction") || s.getName().startsWith("Action ")), names(collector.spans));
        }
    }
    @Test void activeWithoutAgentOrExporterIsSafe() throws Exception {
        try (var collector = new Collector()) {
            launch("none", collector);
            assertTrue(collector.spans.isEmpty(), names(collector.spans));
        }
    }
    @Test void servletAndQuartzEntryClassificationAndCorrelation() throws Exception {
        for (String mode : List.of("boot", "agent")) {
            try (var collector = new Collector()) {
                launchFixture(EntryPointTracingFixture.class, mode, collector);
                var http = collector.spans.stream().filter(s -> s.getKind() == Span.SpanKind.SPAN_KIND_SERVER).toList();
                assertEquals(4, http.size(), names(collector.spans));
                http.forEach(s -> assertEquals("foreground", attribute(s, "causeway.execution.mode"), names(collector.spans)));
                var background = collector.spans.stream().filter(s -> "background".equals(attribute(s, "causeway.execution.mode"))).toList();
                assertEquals(1, background.size(), names(collector.spans));
                var roots = collector.spans.stream().filter(s -> s.getName().equals("Causeway Root Interaction")).toList();
                assertEquals(6, roots.size(), names(collector.spans));
                for (var root : roots) {
                    var id = attribute(root, "causeway.interaction.id");
                    assertNotNull(id);
                    java.util.UUID.fromString(id);
                    assertNull(attribute(root, "causeway.execution.mode"));
                    assertIdentity(root, root.getTraceId().equals(background.get(0).getTraceId())
                            ? "scheduler_user" : "prototyping", null);
                }
                for (var entry : http) {
                    var security = collector.spans.stream().filter(s -> s.getName().equals("fixture.security")
                            && s.getParentSpanId().equals(entry.getSpanId())).toList();
                    assertEquals(1, security.size());
                    var children = roots.stream().filter(s -> s.getParentSpanId().equals(security.get(0).getSpanId())).toList();
                    assertEquals(1, children.size());
                    assertEquals(entry.getTraceId(), children.get(0).getTraceId());
                }
                var replay = roots.stream().filter(s -> EntryPointTracingFixture.REPLAY_ID.toString()
                        .equals(attribute(s, "causeway.interaction.id"))).findFirst().orElseThrow();
                assertEquals(background.get(0).getSpanId(), replay.getParentSpanId());
                assertEquals(background.get(0).getTraceId(), replay.getTraceId());
                for (var span : collector.spans) {
                    if (!roots.contains(span)) assertNull(attribute(span, "causeway.interaction.id"));
                    if (!http.contains(span) && !background.contains(span)) assertNull(attribute(span, "causeway.execution.mode"));
                }
                assertEquals(5, roots.stream().map(Span::getTraceId).distinct().count());
                assertEquals(1, roots.stream().filter(s -> s.getStatus().getCode() == Status.StatusCode.STATUS_CODE_ERROR).count());
            }
        }
    }
    private static String attribute(Span span, String key) {
        return span.getAttributesList().stream().filter(a -> a.getKey().equals(key))
                .map(a -> a.getValue().getStringValue()).findFirst().orElse(null);
    }
    private void launch(String mode, Collector collector, String... properties) throws Exception {
        launchFixture(MicrometerTracingAgentFixture.class, mode, collector, properties);
    }
    private void launchFixture(Class<?> fixture, String mode, Collector collector, String... properties) throws Exception {
        boolean agent = mode.equals("agent") || mode.equals("inactive");
        var command = new ArrayList<String>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        if (agent) command.add("-javaagent:" + System.getProperty("otel.javaagent.path"));
        command.add("-javaagent:" + Path.of(org.mockito.Mockito.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        command.add("-cp");
        command.add(System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")));
        command.add(fixture.getName());
        command.add("--spring.profiles.active=" + (mode.equals("inactive") ? "agent" : agent ? "observation,agent" : "observation"));
        command.add("--management.tracing.sampling.probability=1.0");
        command.add("--management.otlp.metrics.export.url=" + collector.metricsEndpoint());
        command.add("--management.opentelemetry.tracing.export.otlp.endpoint=" + collector.endpoint());
        if (!agent) command.add("--management.tracing.export.enabled=" + mode.equals("boot"));
        if (mode.equals("none")) {
            command.add("--spring.autoconfigure.exclude=" + String.join(",",
                    "org.springframework.boot.opentelemetry.autoconfigure.OpenTelemetrySdkAutoConfiguration",
                    "org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.OpenTelemetryTracingAutoConfiguration",
                    "org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.otlp.OtlpTracingAutoConfiguration",
                    "org.springframework.boot.micrometer.tracing.autoconfigure.MicrometerTracingAutoConfiguration",
                    "org.springframework.boot.webmvc.autoconfigure.WebMvcObservationAutoConfiguration"));
        }
        if (!agent) command.add("--management.opentelemetry.map-environment-variables=false");
        command.addAll(List.of(properties));
        var builder = new ProcessBuilder(command).redirectErrorStream(true);
        builder.environment().keySet().removeIf(key -> key.startsWith("OTEL_"));
        builder.environment().put("OTEL_SERVICE_NAME", "causeway-foundation-test");
        builder.environment().put("OTEL_TRACES_EXPORTER", "otlp");
        builder.environment().put("OTEL_METRICS_EXPORTER", "none");
        builder.environment().put("OTEL_LOGS_EXPORTER", "none");
        builder.environment().put("OTEL_EXPORTER_OTLP_TRACES_ENDPOINT", collector.endpoint());
        builder.environment().put("OTEL_EXPORTER_OTLP_PROTOCOL", "http/protobuf");
        builder.environment().put("OTEL_TRACES_SAMPLER", "always_on");
        builder.environment().put("OTEL_BSP_SCHEDULE_DELAY", "100");
        var output = Path.of("target", "fixture-" + fixture.getSimpleName() + "-" + mode + ".log");
        var process = builder.start();
        var drainer = new Thread(() -> {
            try (var stream = process.getInputStream()) { Files.copy(stream, output, java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        });
        drainer.setDaemon(true);
        drainer.start();
        try {
            assertTrue(process.waitFor(120, TimeUnit.SECONDS), "Fixture timed out; see " + output);
            drainer.join(5000);
            var log = Files.readString(output);
            assertEquals(0, process.exitValue(), log);
            assertTrue(log.contains("CAUSEWAY_TRACING_FIXTURE_OK"), log);
        } finally { process.destroyForcibly(); process.waitFor(10, TimeUnit.SECONDS); }
    }
    private String names(List<Span> spans) { return spans.stream().map(s -> s.getName() + ":" + s.getKind()).toList().toString(); }
    private static class Collector implements AutoCloseable {
        final List<Span> spans = new CopyOnWriteArrayList<>();
        final List<String> metricNames = new CopyOnWriteArrayList<>();
        final HttpServer server;
        Collector() throws Exception {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/v1/traces", exchange -> {
                try (var body = "gzip".equals(exchange.getRequestHeaders().getFirst("Content-Encoding"))
                        ? new GZIPInputStream(exchange.getRequestBody()) : exchange.getRequestBody()) {
                    var request = ExportTraceServiceRequest.parseFrom(body);
                    request.getResourceSpansList().forEach(resource -> resource.getScopeSpansList().forEach(scope -> spans.addAll(scope.getSpansList())));
                    exchange.sendResponseHeaders(200, 0);
                } finally { exchange.close(); }
            });
            server.createContext("/v1/metrics", exchange -> {
                try (var body = "gzip".equals(exchange.getRequestHeaders().getFirst("Content-Encoding"))
                        ? new GZIPInputStream(exchange.getRequestBody()) : exchange.getRequestBody()) {
                    var request = io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest.parseFrom(body);
                    request.getResourceMetricsList().forEach(resource -> resource.getScopeMetricsList().forEach(scope ->
                            scope.getMetricsList().forEach(metric -> metricNames.add(metric.getName()))));
                    exchange.sendResponseHeaders(200, 0);
                } finally { exchange.close(); }
            });
            server.start();
        }
        String endpoint() { return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/traces"; }
        String metricsEndpoint() { return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/metrics"; }
        public void close() { server.stop(0); }
    }
}

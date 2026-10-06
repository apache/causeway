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
            launch("agent", collector, "--causeway.observation.duration-filtering-enabled=false",
                    "--causeway.observation.jpa-duration-threshold=1d");
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
                assertIdentity(root, false);
            }
            assertTrue(spans.stream().anyMatch(s -> s.getName().equals("threshold-success")), names(spans));
            assertTrue(spans.stream().anyMatch(s -> s.getName().equals("threshold-failure") && s.getStatus().getCode() == Status.StatusCode.STATUS_CODE_ERROR), names(spans));
        }
    }
    @Test void bootManagedExportAndDiscardPolicy() throws Exception {
        try (var collector = new Collector()) {
            launch("boot", collector, "--causeway.observation.duration-filtering-enabled=true",
                    "--causeway.observation.jpa-duration-threshold=1d");
            assertEquals(2, collector.spans.stream().filter(s -> s.getName().equals("Causeway Root Interaction")).count(), names(collector.spans));
            assertFalse(collector.spans.stream().anyMatch(s -> s.getName().equals("threshold-success")), names(collector.spans));
            assertFalse(collector.spans.stream().anyMatch(s -> s.getName().startsWith("Persist ")), names(collector.spans));
            var children = collector.spans.stream().filter(s -> s.getName().equals("fixture.jdbc")).toList();
            assertEquals(2, children.size(), names(collector.spans));
            // Explicit filtering is lossy: independent children survive their suppressed JPA parents.
            for (var child : children) assertFalse(collector.spans.stream()
                    .anyMatch(s -> s.getSpanId().equals(child.getParentSpanId())), names(collector.spans));
            assertTrue(collector.spans.stream().anyMatch(s -> s.getName().equals("threshold-failure")), names(collector.spans));
        }
    }
    @Test void bootDefaultsRetainJpaParentsAndOmitIdentity() throws Exception {
        try (var collector = new Collector()) {
            launch("boot", collector, "--causeway.observation.jpa-duration-threshold=1d");
            assertTrue(collector.spans.stream().anyMatch(s -> s.getName().equals("threshold-success")), names(collector.spans));
            var actions = collector.spans.stream().filter(s -> s.getName().startsWith("Action ")).toList();
            assertEquals(2, actions.size(), names(collector.spans));
            for (var action : actions) assertJpaAncestry(collector.spans, action, false);
            collector.spans.stream().filter(s -> s.getName().equals("Causeway Root Interaction"))
                    .forEach(s -> assertIdentity(s, false));
        }
    }
    @Test void identityOptInsAreExportedInBothModes() throws Exception {
        for (String mode : List.of("boot", "agent")) {
            try (var collector = new Collector()) {
                launch(mode, collector, "--causeway.observation.include-user-name=true",
                        "--causeway.observation.include-multi-tenancy-token=true");
                var roots = collector.spans.stream().filter(s -> s.getName().equals("Causeway Root Interaction")).toList();
                assertEquals(2, roots.size(), names(collector.spans));
                roots.forEach(s -> assertIdentity(s, true));
            }
        }
    }
    private void assertIdentity(Span span, boolean enabled) {
        var attributes = span.getAttributesList().stream().collect(java.util.stream.Collectors.toMap(
                a -> a.getKey(), a -> a.getValue().getStringValue()));
        assertEquals(enabled ? "sentinel-user" : null, attributes.get("causeway.user.name"));
        assertEquals(enabled ? "sentinel-tenant" : null, attributes.get("causeway.user.multiTenancyToken"));
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
    private void launch(String mode, Collector collector, String... properties) throws Exception {
        boolean agent = mode.equals("agent") || mode.equals("inactive");
        var command = new ArrayList<String>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        if (agent) command.add("-javaagent:" + System.getProperty("otel.javaagent.path"));
        command.add("-javaagent:" + Path.of(org.mockito.Mockito.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        command.add("-cp");
        command.add(System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")));
        command.add(MicrometerTracingAgentFixture.class.getName());
        command.add("--spring.profiles.active=" + (mode.equals("inactive") ? "agent" : agent ? "observation,agent" : "observation"));
        command.add("--management.tracing.sampling.probability=1.0");
        command.add("--management.opentelemetry.tracing.export.otlp.endpoint=" + collector.endpoint());
        command.add("--management.tracing.export.enabled=" + mode.equals("boot"));
        if (agent || mode.equals("none")) {
            command.add("--spring.autoconfigure.exclude=" + String.join(",",
                    "org.springframework.boot.opentelemetry.autoconfigure.OpenTelemetrySdkAutoConfiguration",
                    "org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.OpenTelemetryTracingAutoConfiguration",
                    "org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.otlp.OtlpTracingAutoConfiguration",
                    "org.springframework.boot.micrometer.tracing.autoconfigure.MicrometerTracingAutoConfiguration"));
        }
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
        var output = Path.of("target", "fixture-" + mode + ".log");
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
            server.start();
        }
        String endpoint() { return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/traces"; }
        public void close() { server.stop(0); }
    }
}

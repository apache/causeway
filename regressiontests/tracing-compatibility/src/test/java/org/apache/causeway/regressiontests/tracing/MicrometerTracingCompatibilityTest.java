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
                var actions = spans.stream().filter(s -> s.getParentSpanId().equals(root.getSpanId()) && s.getName().startsWith("act ")).toList();
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
                var actions = collector.spans.stream().filter(s -> s.getName().startsWith("act ")).toList();
                assertEquals(2, actions.size(), names(collector.spans));
                for (var action : actions) assertJpaAncestry(collector.spans, action, mode.equals("agent"));
                assertFalse(collector.spans.stream().anyMatch(s -> attribute(s, "causeway.discard") != null));
            }
        }
    }
    @Test void bootDefaultsRetainJpaParentsAndIncludeIdentity() throws Exception {
        try (var collector = new Collector()) {
            launch("boot", collector);
            var actions = collector.spans.stream().filter(s -> s.getName().startsWith("act ")).toList();
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
    @Test void semanticMembersExportDomainIdentityAndCompactNamesInBothModes() throws Exception {
        for (String mode : List.of("boot", "agent")) {
            try (var collector = new Collector()) {
                launch(mode, collector, "--fixture.semantic=true");
                var members = collector.spans.stream().filter(span -> attribute(span, "causeway.member.id") != null).toList();
                assertEquals(14, members.size(), names(collector.spans)); // seven operations, two requests
                for (String key : List.of("causeway.action.id", "causeway.property.id", "causeway.collection.id")) {
                    var selected = members.stream().filter(span -> attribute(span, key) != null).toList();
                    assertEquals(key.equals("causeway.action.id") ? 10 : 2, selected.size(), names(members));
                    for (var span : selected) {
                        assertTrue(span.getName().startsWith(key.equals("causeway.action.id") ? "act "
                                : key.equals("causeway.property.id") ? "prop " : "coll "), span.getName());
                        assertTrue(span.getName().contains("UpperCaseOwner#"), span.getName());
                        assertTrue(span.getName().length() <= 50);
                        assertFalse(attribute(span, key).contains("implementation."));
                        if (!key.equals("causeway.action.id")) assertNull(attribute(span, "causeway.action.id"));
                        var root = collector.spans.stream().filter(parent -> parent.getSpanId().equals(span.getParentSpanId()))
                                .findFirst().orElseThrow();
                        assertEquals("Causeway Root Interaction", root.getName());
                        assertEquals(root.getTraceId(), span.getTraceId());
                        assertJpaAncestry(collector.spans, span, mode.equals("agent"));
                    }
                }
                var longActions = members.stream().filter(span -> {
                    var id = attribute(span, "causeway.action.id");
                    return id != null && id.startsWith("very.long.namespace.");
                }).toList();
                assertEquals(4, longActions.size());
                assertEquals(1, longActions.stream().map(Span::getName).distinct().count());
                assertEquals(2, longActions.stream().map(span -> attribute(span, "causeway.action.id")).distinct().count());
                assertTrue(longActions.stream().allMatch(span -> attribute(span, "causeway.action.id").endsWith("(java.lang.String)")));
                assertTrue(members.stream().anyMatch(span -> "act domain.UpperCaseOwner#updateName".equals(span.getName())));
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
    @Test void wicketRegionsExportRealRenderAndAjaxAncestryInBothModes() throws Exception {
        for (String mode : List.of("boot", "agent")) {
            try (var collector = new Collector()) {
                launch(mode, collector, "--fixture.wicket=true");
                var spans = collector.spans;
                var regions = spans.stream().filter(span -> attribute(span, "causeway.object.type") != null).toList();
                assertFalse(regions.isEmpty(), names(spans));
                for (var region : regions) {
                    assertTrue(region.getName().length() <= 50);
                    assertEquals("fixture.UpperCaseOwner", attribute(region, "causeway.object.type"));
                    assertTrue(region.getName().contains("UpperCaseOwner") || region.getName().equals("render fieldset identity"), names(spans));
                    var parent = spans.stream().filter(span -> span.getSpanId().equals(region.getParentSpanId())).findFirst().orElseThrow(() -> new AssertionError(names(spans)));
                    assertEquals(parent.getTraceId(), region.getTraceId());
                }
                assertEquals(2, regions.stream().filter(span -> span.getName().equals("prepare fixture.UpperCaseOwner")).count());
                assertEquals(2, regions.stream().filter(span -> span.getName().equals("render fixture.UpperCaseOwner")).count());
                var properties = regions.stream().filter(span -> attribute(span, "causeway.property.id") != null).toList();
                assertEquals(3, properties.size(), names(spans)); // failed full render, successful full render, Ajax update
                assertEquals(1, properties.stream().filter(span -> span.getStatus().getCode() == Status.StatusCode.STATUS_CODE_ERROR).count());
                assertEquals(1, regions.stream().filter(span -> span.getName().equals("prompt fixture.UpperCaseOwner#updateName")).count());
                assertTrue(regions.stream().anyMatch(span -> "fixture.UpperCaseOwner#updateName(java.lang.String)".equals(attribute(span, "causeway.action.id"))));
                var framework = spans.stream().filter(span -> span.getName().startsWith("Apache Wicket Request Cycle")).toList();
                assertTrue(framework.stream().anyMatch(span -> span.getName().endsWith("(AJAX)")), names(spans));
                for (var property : properties) {
                    var actions = spans.stream().filter(span -> span.getParentSpanId().equals(property.getSpanId()) && span.getName().startsWith("act ")).toList();
                    assertEquals(1, actions.size(), names(spans));
                    assertJpaAncestry(spans, actions.get(0), mode.equals("agent"));
                }
                var ajax = framework.stream().filter(span -> span.getName().endsWith("(AJAX)")).findFirst().orElseThrow();
                var ajaxRoot = spans.stream().filter(span -> span.getParentSpanId().equals(ajax.getSpanId()) && span.getName().equals("Causeway Root Interaction")).findFirst().orElseThrow();
                assertTrue(properties.stream().anyMatch(span -> span.getParentSpanId().equals(ajaxRoot.getSpanId())));
                for (var request : framework) {
                    assertTrue(spans.stream().anyMatch(span -> span.getSpanId().equals(request.getParentSpanId()) && span.getKind() == Span.SpanKind.SPAN_KIND_SERVER), names(spans));
                }
            }
        }
    }
    @Test void collectionCallbacksExportInBothModesWithSummariesAndAdmission() throws Exception {
        for(String mode : List.of("boot", "agent")) {
            for(String detail : List.of("MEMBERS", "REGIONS")) {
                try(var collector = new Collector()) {
                    launch(mode, collector, "--fixture.collections=true",
                            "--causeway.viewer.wicket.observation.detail=" + detail);
                    var spans = collector.spans;
                    var collections = spans.stream().filter(span -> span.getName().startsWith("render collection ")).toList();
                    assertEquals(3, collections.size(), names(spans)); // failed full render, successful full render, Ajax
                    assertTrue(collections.stream().anyMatch(span -> "2".equals(attribute(span, "causeway.wicket.collection.row.count"))), names(spans));
                    assertTrue(collections.stream().anyMatch(span -> "2".equals(attribute(span, "causeway.wicket.collection.logical-cell.count"))), names(spans));
                    var rows = spans.stream().filter(span -> span.getName().startsWith("render row ")).toList();
                    assertEquals(detail.equals("MEMBERS"), !rows.isEmpty(), names(spans));
                    for(var row : rows) {
                        var parent = spans.stream().filter(span -> span.getSpanId().equals(row.getParentSpanId())).findFirst().orElseThrow();
                        assertTrue(parent.getName().startsWith("render table body "), names(spans));
                        assertEquals("fixture.CollectionOwner#items", attribute(row, "causeway.collection.id"));
                    }
                    var actions = spans.stream().filter(span -> span.getName().startsWith("act ")).toList();
                    assertFalse(actions.isEmpty(), names(spans));
                    for(var action : actions) assertJpaAncestry(spans, action, mode.equals("agent"));
                    if(detail.equals("REGIONS")) assertTrue(spans.stream().anyMatch(span -> attribute(span, "causeway.wicket.suppressed.detail") != null));
                }
            }
        }
    }
    @Test void collectionBudgetIsHardAndDoesNotSuppressDomainWork() throws Exception {
        for(String mode : List.of("boot", "agent")) {
            try(var collector = new Collector()) {
                launch(mode, collector, "--fixture.collections=true", "--causeway.viewer.wicket.observation.max-spans-per-request=5");
                var spans = collector.spans;
                var frameworks = spans.stream().filter(span -> span.getName().startsWith("Apache Wicket Request Cycle")).toList();
                assertTrue(frameworks.stream().anyMatch(span -> span.getName().endsWith("(AJAX)")), names(spans));
                for(var framework : frameworks) {
                    long count = spans.stream().filter(span -> attribute(span, "causeway.object.type") != null
                            && ancestorIs(spans, span, framework)).count();
                    assertTrue(count <= 5, names(spans));
                }
                // Group by framework interactions: the agent can add servlet
                // boundaries inside the synthetic WicketTester HTTP requests.
                var roots = spans.stream().filter(span -> span.getName().equals("Causeway Root Interaction")).toList();
                long accounted = 0;
                for(var root : roots) {
                    long count = spans.stream().filter(span ->
                            "fixture.CollectionOwner".equals(attribute(span, "causeway.object.type"))
                            && ancestorIs(spans, span, root)).count();
                    assertTrue(count <= 5, names(spans));
                    accounted += count;
                }
                assertEquals(15, accounted, names(spans)); // failed full render, success, Ajax
                assertTrue(spans.stream().anyMatch(span -> attribute(span, "causeway.wicket.suppressed.budget") != null));
                assertTrue(spans.stream().anyMatch(span -> span.getName().startsWith("act ")), names(spans));
            }
        }
    }
    private static boolean ancestorIs(List<Span> spans, Span span, Span ancestor) {
        for(int depth=0; depth<100 && !span.getParentSpanId().isEmpty(); depth++) {
            if(span.getParentSpanId().equals(ancestor.getSpanId())) return true;
            var parentId = span.getParentSpanId();
            var parent = spans.stream().filter(candidate -> candidate.getSpanId().equals(parentId)).findFirst();
            if(parent.isEmpty()) return false;
            span = parent.get();
        }
        return false;
    }
    @Test void wicketRegionsAreSafeWhenInactiveOrExporterIsAbsent() throws Exception {
        for (String mode : List.of("inactive", "none")) {
            try (var collector = new Collector()) {
                launch(mode, collector, "--fixture.wicket=true");
                assertFalse(collector.spans.stream().anyMatch(span -> attribute(span, "causeway.object.type") != null));
            }
        }
    }
    @Test void inactiveProfileLeavesAutomaticAgentSpansWorking() throws Exception {
        try (var collector = new Collector()) {
            launch("inactive", collector);
            assertTrue(collector.spans.stream().anyMatch(s -> s.getKind() == Span.SpanKind.SPAN_KIND_SERVER), names(collector.spans));
            assertFalse(collector.spans.stream().anyMatch(s -> s.getName().contains("Causeway Root Interaction") || s.getName().startsWith("act ")), names(collector.spans));
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
    @Test void semanticEntryNamesSurviveExportInBothOwnersAndAdmissionSettings() throws Exception {
        for (String mode : List.of("boot", "agent")) {
            for (String setting : List.of("--causeway.viewer.wicket.observation.detail=MEMBERS",
                    "--causeway.viewer.wicket.observation.detail=NONE",
                    "--causeway.viewer.wicket.observation.max-spans-per-request=1")) {
                try (var collector = new Collector()) {
                    launchFixture(SemanticTraceTracingFixture.class, mode, collector, setting);
                    var entries = collector.spans.stream().filter(s -> "foreground".equals(attribute(s, "causeway.execution.mode"))).toList();
                    assertEquals(6, entries.size(), names(collector.spans));
                    assertEquals(1, entries.stream().filter(s -> s.getName().equals("view " + SemanticTraceTracingFixture.TYPE)).count(), names(entries));
                    assertEquals(1, entries.stream().filter(s -> s.getName().equals("prompt " + SemanticTraceTracingFixture.TYPE + "#UpdateName")).count(), names(entries));
                    assertEquals(2, entries.stream().filter(s -> s.getName().equals("act " + SemanticTraceTracingFixture.TYPE + "#UpdateName")).count(), names(entries));
                    assertEquals(2, entries.stream().filter(s -> attribute(s, "causeway.trace.name") == null).count(), names(entries));
                    for (var entry : entries) {
                        assertEquals(Span.SpanKind.SPAN_KIND_SERVER, entry.getKind());
                        assertEquals("0123456789abcdef0123456789abcdef", java.util.HexFormat.of().formatHex(entry.getTraceId().toByteArray()));
                        assertEquals("0123456789abcdef", java.util.HexFormat.of().formatHex(entry.getParentSpanId().toByteArray()));
                        assertEquals("GET", attribute(entry, mode.equals("boot") ? "method" : "http.request.method"), entry.toString());
                        String status = entry.getAttributesList().stream()
                                .filter(a -> a.getKey().equals(mode.equals("boot") ? "status" : "http.response.status_code"))
                                .map(a -> a.getValue().hasIntValue() ? Long.toString(a.getValue().getIntValue()) : a.getValue().getStringValue())
                                .findFirst().orElseThrow(() -> new AssertionError(entry.toString()));
                        assertEquals("/fail".equals(attribute(entry, mode.equals("boot") ? "http.url" : "url.path")) ? "500" : "204", status);
                        assertNotNull(attribute(entry, mode.equals("boot") ? "http.url" : "url.path"), entry.toString());
                        assertNotNull(attribute(entry, mode.equals("boot") ? "uri" : "http.route"), entry.toString());
                        if (attribute(entry, "causeway.trace.name") != null) assertEquals(entry.getName(), attribute(entry, "causeway.trace.name"));
                        if (entry.getName().startsWith("act ") || entry.getName().startsWith("prompt ")) {
                            assertEquals(SemanticTraceTracingFixture.ACTION, attribute(entry, "causeway.action.id"));
                        }
                        if (entry.getName().startsWith("view ")) assertEquals(SemanticTraceTracingFixture.TYPE, attribute(entry, "causeway.object.type"));
                        var children = collector.spans.stream().filter(s -> s.getName().equals("fixture.security") && s.getParentSpanId().equals(entry.getSpanId())).toList();
                        assertEquals(1, children.size(), names(collector.spans));
                        assertEquals(entry.getTraceId(), children.get(0).getTraceId());
                        assertNull(attribute(children.get(0), "causeway.trace.name"));
                    }
                    if (setting.endsWith("NONE")) assertFalse(collector.spans.stream().anyMatch(s -> s.getName().startsWith("render ") || s.getName().startsWith("prompt ") && !entries.contains(s)), names(collector.spans));
                    assertEquals(6, entries.stream().map(Span::getSpanId).distinct().count());
                }
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

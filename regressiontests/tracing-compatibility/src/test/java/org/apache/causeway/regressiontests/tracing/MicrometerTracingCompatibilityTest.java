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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import com.google.protobuf.ByteString;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import org.junit.jupiter.api.Test;

import static org.apache.causeway.core.config.observation.CausewaySemanticTraceNamer.ACTION_ID_ATTRIBUTE;
import static org.apache.causeway.core.config.observation.CausewaySemanticTraceNamer.OBJECT_TYPE_ATTRIBUTE;
import static org.apache.causeway.core.config.observation.CausewaySemanticTraceNamer.TRACE_NAME_ATTRIBUTE;
import static org.apache.causeway.core.config.observation.CausewayTraceClassifier.EXECUTION_MODE_ATTRIBUTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Automated JUnit coverage for the production observation substrate with and without the agent.
 *
 * <p>Each test launches {@link MicrometerTracingAgentFixture} in a child JVM because Java agents
 * must be attached during JVM startup.</p>
 */
class MicrometerTracingCompatibilityTest {

    private static final Duration PROCESS_TIMEOUT = Duration.ofSeconds(45);
    private static final String FIXTURE_JAVA_HOME_PROPERTY =
            "causeway.tracing.fixture.java.home";
    private static final String FIXTURE_JAVA_HOME_ENV =
            "CAUSEWAY_TRACING_FIXTURE_JAVA_HOME";

    @Test
    void javaAgentExportsHttpRootInteractionActionAndJdbcAncestry() throws Exception {
        try (OtlpTraceCollector collector = new OtlpTraceCollector()) {
            final ProcessResult result = runFixture(true, collector.endpoint());

            assertEquals(0, result.exitCode, result.output);
            assertTrue(result.output.contains(MicrometerTracingAgentFixture.SUCCESS_MARKER), result.output);

            final List<ExportedSpan> spans = collector.exportedSpans();
            final ExportedSpan rootSpan = spanNamed(
                    spans,
                    MicrometerTracingAgentFixture.ROOT_INTERACTION_NAME,
                    result.output);
            final ExportedSpan actionSpan = spans.stream()
                    .filter(span -> MicrometerTracingAgentFixture.ACTION_INVOCATION_NAME.equals(span.name))
                    .filter(span -> rootSpan.traceId.equals(span.traceId))
                    .filter(span -> rootSpan.spanId.equals(span.parentSpanId))
                    .findFirst()
                    .orElseGet(() -> fail("Action span is not a child of the root interaction.\n"
                            + describe(spans) + "\n" + result.output));
            final ExportedSpan actionPrimerSpan = childSpanNamed(
                    spans,
                    actionSpan,
                    MicrometerTracingAgentFixture.ACTION_PRIMER_NAME,
                    result.output);
            final ExportedSpan actionPrimerJdbcSpan = spans.stream()
                    .filter(span -> actionPrimerSpan.traceId.equals(span.traceId))
                    .filter(span -> actionPrimerSpan.spanId.equals(span.parentSpanId))
                    .filter(span -> "h2".equals(span.attributes.get("db.system")))
                    .findFirst()
                    .orElseGet(() -> fail("Primer JDBC span is not a child of action primer.\n"
                            + describe(spans) + "\n" + result.output));
            final ExportedSpan viewPrimerSpan = childSpanNamed(
                    spans,
                    rootSpan,
                    MicrometerTracingAgentFixture.VIEW_PRIMER_NAME,
                    result.output);
            final ExportedSpan viewPrimerJdbcSpan = spans.stream()
                    .filter(span -> viewPrimerSpan.traceId.equals(span.traceId))
                    .filter(span -> viewPrimerSpan.spanId.equals(span.parentSpanId))
                    .filter(span -> "h2".equals(span.attributes.get("db.system")))
                    .findFirst()
                    .orElseGet(() -> fail("Primer JDBC span is not a child of view primer.\n"
                            + describe(spans) + "\n" + result.output));
            final ExportedSpan preparationSpan = spans.stream()
                    .filter(span -> MicrometerTracingAgentFixture.PAGE_PREPARATION_NAME.equals(span.name))
                    .filter(span -> rootSpan.traceId.equals(span.traceId))
                    .filter(span -> rootSpan.spanId.equals(span.parentSpanId))
                    .findFirst()
                    .orElseGet(() -> fail("Page preparation span is not a child of the root interaction.\n"
                            + describe(spans) + "\n" + result.output));
            final ExportedSpan initializationJdbcSpan = spans.stream()
                    .filter(span -> preparationSpan.traceId.equals(span.traceId))
                    .filter(span -> preparationSpan.spanId.equals(span.parentSpanId))
                    .filter(span -> "h2".equals(span.attributes.get("db.system")))
                    .findFirst()
                    .orElseGet(() -> fail("Construction-time JDBC span is not a child of page preparation.\n"
                            + describe(spans) + "\n" + result.output));
            final ExportedSpan collectionPreparationSpan = childSpanNamed(
                    spans,
                    preparationSpan,
                    MicrometerTracingAgentFixture.COLLECTION_PREPARATION_NAME,
                    result.output);
            final ExportedSpan rowPreparationSpan = childSpanNamed(
                    spans,
                    collectionPreparationSpan,
                    MicrometerTracingAgentFixture.ROW_PREPARATION_NAME,
                    result.output);
            final ExportedSpan preparationJdbcSpan = spans.stream()
                    .filter(span -> rowPreparationSpan.traceId.equals(span.traceId))
                    .filter(span -> rowPreparationSpan.spanId.equals(span.parentSpanId))
                    .filter(span -> "h2".equals(span.attributes.get("db.system")))
                    .findFirst()
                    .orElseGet(() -> fail("Preparation JDBC span is not a child of row preparation.\n"
                            + describe(spans) + "\n" + result.output));
            final ExportedSpan pageSpan = spans.stream()
                    .filter(span -> MicrometerTracingAgentFixture.PAGE_RENDER_NAME.equals(span.name))
                    .filter(span -> rootSpan.traceId.equals(span.traceId))
                    .filter(span -> rootSpan.spanId.equals(span.parentSpanId))
                    .findFirst()
                    .orElseGet(() -> fail("Page render span is not a child of the root interaction.\n"
                            + describe(spans) + "\n" + result.output));
            final ExportedSpan fieldsetSpan = childSpanNamed(
                    spans,
                    pageSpan,
                    MicrometerTracingAgentFixture.FIELDSET_RENDER_NAME,
                    result.output);
            final ExportedSpan propertySpan = childSpanNamed(
                    spans,
                    fieldsetSpan,
                    MicrometerTracingAgentFixture.PROPERTY_RENDER_NAME,
                    result.output);
            final ExportedSpan actionRenderSpan = childSpanNamed(
                    spans,
                    fieldsetSpan,
                    MicrometerTracingAgentFixture.ACTION_RENDER_NAME,
                    result.output);
            final ExportedSpan collectionSpan = childSpanNamed(
                    spans,
                    pageSpan,
                    MicrometerTracingAgentFixture.COLLECTION_RENDER_NAME,
                    result.output);
            final ExportedSpan tableSpan = childSpanNamed(
                    spans,
                    collectionSpan,
                    MicrometerTracingAgentFixture.TABLE_RENDER_NAME,
                    result.output);
            final ExportedSpan tableHeaderSpan = childSpanNamed(
                    spans,
                    tableSpan,
                    MicrometerTracingAgentFixture.TABLE_HEADER_RENDER_NAME,
                    result.output);
            final ExportedSpan tableBodySpan = childSpanNamed(
                    spans,
                    tableSpan,
                    MicrometerTracingAgentFixture.TABLE_BODY_RENDER_NAME,
                    result.output);
            final ExportedSpan tableFooterSpan = childSpanNamed(
                    spans,
                    tableSpan,
                    MicrometerTracingAgentFixture.TABLE_FOOTER_RENDER_NAME,
                    result.output);
            final ExportedSpan rowSpan = childSpanNamed(
                    spans,
                    tableBodySpan,
                    MicrometerTracingAgentFixture.ROW_RENDER_NAME,
                    result.output);
            final ExportedSpan rowPropertySpan = childSpanNamed(
                    spans,
                    rowSpan,
                    MicrometerTracingAgentFixture.ROW_PROPERTY_RENDER_NAME,
                    result.output);
            final ExportedSpan rowActionSpan = childSpanNamed(
                    spans,
                    rowSpan,
                    MicrometerTracingAgentFixture.ROW_ACTION_RENDER_NAME,
                    result.output);
            final ExportedSpan promptSpan = childSpanNamed(
                    spans,
                    pageSpan,
                    MicrometerTracingAgentFixture.PROMPT_RENDER_NAME,
                    result.output);
            final ExportedSpan entityChangeEvaluationSpan = childSpanNamed(
                    spans,
                    actionSpan,
                    MicrometerTracingAgentFixture.ENTITY_CHANGE_EVALUATION_NAME,
                    result.output);
            final long entityChangeJdbcSpanCount = spans.stream()
                    .filter(span -> entityChangeEvaluationSpan.traceId.equals(span.traceId))
                    .filter(span -> entityChangeEvaluationSpan.spanId.equals(span.parentSpanId))
                    .filter(span -> "h2".equals(span.attributes.get("db.system")))
                    .count();
            final ExportedSpan auditTrailWriteSpan = childSpanNamed(
                    spans,
                    actionSpan,
                    MicrometerTracingAgentFixture.AUDIT_TRAIL_WRITE_NAME,
                    result.output);
            final long auditJdbcSpanCount = spans.stream()
                    .filter(span -> auditTrailWriteSpan.traceId.equals(span.traceId))
                    .filter(span -> auditTrailWriteSpan.spanId.equals(span.parentSpanId))
                    .filter(span -> "h2".equals(span.attributes.get("db.system")))
                    .count();
            final ExportedSpan actionApplicationSpan = childSpanNamed(
                    spans,
                    actionSpan,
                    MicrometerTracingAgentFixture.APPLICATION_ACTION_NAME,
                    result.output);
            final ExportedSpan jdbcSpan = spans.stream()
                    .filter(span -> actionApplicationSpan.traceId.equals(span.traceId))
                    .filter(span -> actionApplicationSpan.spanId.equals(span.parentSpanId))
                    .filter(span -> "h2".equals(span.attributes.get("db.system")))
                    .findFirst()
                    .orElseGet(() -> fail("JDBC span is not a child of the application span.\n"
                            + describe(spans) + "\n" + result.output));
            final ExportedSpan httpSpan = ancestorWithAttribute(
                    spans,
                    rootSpan,
                    "http.route",
                    "/trace",
                    result.output);

            assertEquals(MicrometerTracingAgentFixture.ACTION_INVOCATION_NAME, httpSpan.name);
            assertEquals("foreground", httpSpan.attributes.get(EXECUTION_MODE_ATTRIBUTE));
            assertEquals(MicrometerTracingAgentFixture.ACTION_INVOCATION_NAME,
                    httpSpan.attributes.get(TRACE_NAME_ATTRIBUTE));
            assertEquals(MicrometerTracingAgentFixture.ACTION_ID,
                    httpSpan.attributes.get(ACTION_ID_ATTRIBUTE));
            assertEquals("GET", httpSpan.attributes.get("http.method"));
            assertEquals("/trace", httpSpan.attributes.get("http.route"));
            assertEquals("/trace", httpSpan.attributes.get("http.target"));
            assertEquals("204", httpSpan.attributes.get("http.status_code"));
            assertEquals(1L, spans.stream()
                    .filter(span -> "/trace".equals(span.attributes.get("http.route")))
                    .count());
            assertEquals(httpSpan.traceId, rootSpan.traceId);
            assertEquals(rootSpan.traceId, actionSpan.traceId);
            assertEquals(actionSpan.traceId, jdbcSpan.traceId);
            assertEquals(MicrometerTracingAgentFixture.ACTION_ID,
                    actionSpan.attributes.get("causeway.action.id"));
            assertEquals(MicrometerTracingAgentFixture.INTERACTION_ID.toString(),
                    rootSpan.attributes.get("causeway.interaction.id"));
            assertFalse(httpSpan.attributes.containsKey("causeway.interaction.id"));
            assertFalse(actionSpan.attributes.containsKey("causeway.interaction.id"));
            assertEquals("jdbcWork", actionApplicationSpan.attributes.get(
                    "causeway.application.span.suffix"));
            assertEquals("ApplicationSpanServiceDefault",
                    actionApplicationSpan.attributes.get("causeway.bean"));
            assertEquals("runtimeservices",
                    actionApplicationSpan.attributes.get("causeway.module"));
            assertEquals(1L, spans.stream()
                    .filter(span -> actionSpan.traceId.equals(span.traceId))
                    .filter(span -> MicrometerTracingAgentFixture.ENTITY_CHANGE_EVALUATION_NAME
                            .equals(span.name))
                    .count());
            assertEquals(2L, entityChangeJdbcSpanCount);
            assertEquals("SemanticTracingController",
                    entityChangeEvaluationSpan.attributes.get("causeway.bean"));
            assertEquals("persistence.commons",
                    entityChangeEvaluationSpan.attributes.get("causeway.module"));
            assertFalse(entityChangeEvaluationSpan.attributes.containsKey("causeway.property.id"));
            assertFalse(entityChangeEvaluationSpan.attributes.containsKey("causeway.object.type"));
            assertFalse(entityChangeEvaluationSpan.attributes.containsKey("causeway.user"));
            assertFalse(entityChangeEvaluationSpan.attributes.containsKey("causeway.tenant"));
            assertFalse(entityChangeEvaluationSpan.attributes.containsKey("causeway.change.count"));
            assertEquals(1L, spans.stream()
                    .filter(span -> actionSpan.traceId.equals(span.traceId))
                    .filter(span -> MicrometerTracingAgentFixture.AUDIT_TRAIL_WRITE_NAME
                            .equals(span.name))
                    .count());
            assertTrue(auditJdbcSpanCount >= 2L,
                    () -> "Expected multiple JDBC children beneath audit write.\n"
                            + describe(spans) + "\n" + result.output);
            assertEquals("SemanticTracingController",
                    auditTrailWriteSpan.attributes.get("causeway.bean"));
            assertEquals("ext.auditTrail",
                    auditTrailWriteSpan.attributes.get("causeway.module"));
            assertFalse(auditTrailWriteSpan.attributes.containsKey("causeway.property.id"));
            assertFalse(auditTrailWriteSpan.attributes.containsKey("causeway.object.type"));
            assertFalse(auditTrailWriteSpan.attributes.containsKey("causeway.user"));
            assertFalse(auditTrailWriteSpan.attributes.containsKey("causeway.tenant"));
            assertFalse(auditTrailWriteSpan.attributes.containsKey("causeway.audit.entry.count"));
            assertEquals(1L, spans.stream()
                    .filter(span -> actionSpan.traceId.equals(span.traceId))
                    .filter(span -> MicrometerTracingAgentFixture.ACTION_PRIMER_NAME
                            .equals(span.name))
                    .count());
            assertEquals(1L, spans.stream()
                    .filter(span -> rootSpan.traceId.equals(span.traceId))
                    .filter(span -> MicrometerTracingAgentFixture.VIEW_PRIMER_NAME
                            .equals(span.name))
                    .count());
            assertEquals(MicrometerTracingAgentFixture.OBJECT_TYPE,
                    actionPrimerSpan.attributes.get("causeway.object.type"));
            assertEquals(MicrometerTracingAgentFixture.ACTION_ID,
                    actionPrimerSpan.attributes.get("causeway.action.id"));
            assertEquals("h2", actionPrimerJdbcSpan.attributes.get("db.system"));
            assertEquals(MicrometerTracingAgentFixture.OBJECT_TYPE,
                    viewPrimerSpan.attributes.get("causeway.object.type"));
            assertFalse(viewPrimerSpan.attributes.containsKey("causeway.action.id"));
            assertEquals("h2", viewPrimerJdbcSpan.attributes.get("db.system"));
            assertFalse(actionPrimerSpan.attributes.containsKey("causeway.primer.class"));
            assertFalse(viewPrimerSpan.attributes.containsKey("causeway.primer.class"));
            assertEquals(MicrometerTracingAgentFixture.OBJECT_TYPE,
                    preparationSpan.attributes.get("causeway.object.type"));
            assertEquals("h2", initializationJdbcSpan.attributes.get("db.system"));
            assertFalse(spans.stream().anyMatch(span ->
                    "causeway.wicket.collection.initialize".equals(
                            span.attributes.get("causeway.observation.name"))
                    || "initialize collection roles".equals(span.name)));
            assertEquals(MicrometerTracingAgentFixture.OBJECT_TYPE,
                    collectionPreparationSpan.attributes.get("causeway.object.type"));
            assertEquals(MicrometerTracingAgentFixture.COLLECTION_ID,
                    collectionPreparationSpan.attributes.get("causeway.collection.id"));
            assertEquals(MicrometerTracingAgentFixture.ROW_OBJECT_TYPE,
                    rowPreparationSpan.attributes.get("causeway.object.type"));
            assertEquals(MicrometerTracingAgentFixture.COLLECTION_ID,
                    rowPreparationSpan.attributes.get("causeway.collection.id"));
            assertEquals("h2", preparationJdbcSpan.attributes.get("db.system"));
            assertEquals(MicrometerTracingAgentFixture.OBJECT_TYPE,
                    pageSpan.attributes.get("causeway.object.type"));
            assertEquals(MicrometerTracingAgentFixture.OBJECT_TYPE,
                    promptSpan.attributes.get("causeway.object.type"));
            assertEquals(MicrometerTracingAgentFixture.ACTION_ID,
                    promptSpan.attributes.get("causeway.action.id"));
            assertEquals("identity",
                    fieldsetSpan.attributes.get("causeway.fieldset.id"));
            assertEquals(MicrometerTracingAgentFixture.OBJECT_TYPE + "#emailAddress",
                    propertySpan.attributes.get("causeway.property.id"));
            assertEquals(MicrometerTracingAgentFixture.COLLECTION_ID,
                    collectionSpan.attributes.get("causeway.collection.id"));
            assertEquals(MicrometerTracingAgentFixture.COLLECTION_ID,
                    tableSpan.attributes.get("causeway.collection.id"));
            assertEquals(MicrometerTracingAgentFixture.COLLECTION_ID,
                    tableHeaderSpan.attributes.get("causeway.collection.id"));
            assertEquals(MicrometerTracingAgentFixture.COLLECTION_ID,
                    tableBodySpan.attributes.get("causeway.collection.id"));
            assertEquals(MicrometerTracingAgentFixture.COLLECTION_ID,
                    tableFooterSpan.attributes.get("causeway.collection.id"));
            assertEquals(MicrometerTracingAgentFixture.ROW_OBJECT_TYPE,
                    rowSpan.attributes.get("causeway.object.type"));
            assertEquals(MicrometerTracingAgentFixture.COLLECTION_ID,
                    rowSpan.attributes.get("causeway.collection.id"));
            assertEquals(MicrometerTracingAgentFixture.ROW_PROPERTY_ID,
                    rowPropertySpan.attributes.get("causeway.property.id"));
            assertEquals(MicrometerTracingAgentFixture.ROW_ACTION_ID,
                    rowActionSpan.attributes.get("causeway.action.id"));
            assertEquals(MicrometerTracingAgentFixture.ACTION_ID,
                    actionRenderSpan.attributes.get("causeway.action.id"));

            final ExportedSpan promptEntrySpan = spanWithAttribute(
                    spans, "http.route", "/trace/prompt", result.output);
            assertEquals(MicrometerTracingAgentFixture.PROMPT_RENDER_NAME,
                    promptEntrySpan.name);
            assertEquals(MicrometerTracingAgentFixture.PROMPT_RENDER_NAME,
                    promptEntrySpan.attributes.get(TRACE_NAME_ATTRIBUTE));
            assertEquals(MicrometerTracingAgentFixture.ACTION_ID,
                    promptEntrySpan.attributes.get(ACTION_ID_ATTRIBUTE));

            final ExportedSpan viewEntrySpan = spanWithAttribute(
                    spans, "http.route", "/trace/view", result.output);
            assertEquals(MicrometerTracingAgentFixture.VIEW_TRACE_NAME,
                    viewEntrySpan.name);
            assertEquals(MicrometerTracingAgentFixture.OBJECT_TYPE,
                    viewEntrySpan.attributes.get(OBJECT_TYPE_ATTRIBUTE));

            final ExportedSpan applicationEntrySpan = spanWithAttribute(
                    spans, "http.route", "/trace/application", result.output);
            final ExportedSpan applicationRootSpan = spanNamedInTrace(
                    spans,
                    applicationEntrySpan.traceId,
                    MicrometerTracingAgentFixture.ROOT_INTERACTION_NAME,
                    result.output);
            final ExportedSpan applicationOuterSpan = childSpanNamed(
                    spans,
                    applicationRootSpan,
                    MicrometerTracingAgentFixture.APPLICATION_OUTER_NAME,
                    result.output);
            final ExportedSpan applicationInnerSpan = childSpanNamed(
                    spans,
                    applicationOuterSpan,
                    MicrometerTracingAgentFixture.APPLICATION_INNER_NAME,
                    result.output);
            assertEquals("outer", applicationOuterSpan.attributes.get(
                    "causeway.application.span.suffix"));
            assertEquals("inner", applicationInnerSpan.attributes.get(
                    "causeway.application.span.suffix"));
            assertFalse(applicationOuterSpan.attributes.containsKey("causeway.member.id"));

            final ExportedSpan applicationFailureEntrySpan = spanWithAttribute(
                    spans, "http.route", "/trace/application/failure", result.output);
            final ExportedSpan applicationFailureRootSpan = spanNamedInTrace(
                    spans,
                    applicationFailureEntrySpan.traceId,
                    MicrometerTracingAgentFixture.ROOT_INTERACTION_NAME,
                    result.output);
            final ExportedSpan applicationFailureSpan = childSpanNamed(
                    spans,
                    applicationFailureRootSpan,
                    MicrometerTracingAgentFixture.APPLICATION_FAILURE_NAME,
                    result.output);
            assertEquals("500", applicationFailureEntrySpan.attributes.get("http.status_code"));
            assertEquals("STATUS_CODE_ERROR", applicationFailureSpan.statusCode);

            final ExportedSpan unsupportedEntrySpan = spanWithAttribute(
                    spans, "http.route", "/trace/unsupported", result.output);
            assertEquals("GET /trace/unsupported", unsupportedEntrySpan.name);
            assertFalse(unsupportedEntrySpan.attributes.containsKey(TRACE_NAME_ATTRIBUTE));

            final ExportedSpan failedEntrySpan = spanWithAttribute(
                    spans, "http.route", "/trace/failure", result.output);
            assertEquals(MicrometerTracingAgentFixture.VIEW_TRACE_NAME,
                    failedEntrySpan.name);
            assertEquals("500", failedEntrySpan.attributes.get("http.status_code"));
            assertEquals("STATUS_CODE_ERROR", failedEntrySpan.statusCode);
            System.out.printf(
                    "CAUSEWAY_TRACING_EVIDENCE traceId=%s http=%s rootSpanId=%s viewPrimer=%s viewPrimerJdbc=%s prepare=%s initializationJdbc=%s collectionPrepare=%s rowPrepare=%s preparationJdbc=%s page=%s collection=%s table=%s tableHeader=%s tableBody=%s tableFooter=%s row=%s prompt=%s action=%s actionPrimer=%s actionPrimerJdbc=%s entityChange=%s entityChangeJdbc=%s audit=%s auditInserts=%s jdbcSpanId=%s%n",
                    rootSpan.traceId,
                    httpSpan.name,
                    rootSpan.spanId,
                    viewPrimerSpan.name,
                    viewPrimerJdbcSpan.name,
                    preparationSpan.name,
                    initializationJdbcSpan.name,
                    collectionPreparationSpan.name,
                    rowPreparationSpan.name,
                    preparationJdbcSpan.name,
                    pageSpan.name,
                    collectionSpan.name,
                    tableSpan.name,
                    tableHeaderSpan.name,
                    tableBodySpan.name,
                    tableFooterSpan.name,
                    rowSpan.name,
                    promptSpan.name,
                    actionSpan.name,
                    actionPrimerSpan.name,
                    actionPrimerJdbcSpan.name,
                    entityChangeEvaluationSpan.name,
                    entityChangeJdbcSpanCount,
                    auditTrailWriteSpan.name,
                    auditJdbcSpanCount,
                    jdbcSpan.spanId);
        }
    }

    @Test
    void fixtureDegradesSafelyWithoutJavaAgent() throws Exception {
        final ProcessResult result = runFixture(false, null);

        assertEquals(0, result.exitCode, result.output);
        assertTrue(result.output.contains(MicrometerTracingAgentFixture.SUCCESS_MARKER), result.output);
        assertFalse(result.output.contains("OpenTelemetrySdk.builder"), result.output);
    }

    private static ProcessResult runFixture(
            final boolean withAgent,
            final String otlpEndpoint) throws Exception {
        final List<String> command = new ArrayList<>();
        command.add(javaExecutable());
        if (withAgent) {
            final Path agentPath = Paths.get(System.getProperty("otel.javaagent.path"));
            assertTrue(Files.isRegularFile(agentPath), "Missing Java agent: " + agentPath);
            command.add("-javaagent:" + agentPath.toAbsolutePath());
            command.add("-Dotel.traces.exporter=otlp");
            command.add("-Dotel.metrics.exporter=none");
            command.add("-Dotel.logs.exporter=none");
            command.add("-Dotel.exporter.otlp.protocol=http/protobuf");
            command.add("-Dotel.exporter.otlp.traces.endpoint=" + otlpEndpoint);
            command.add("-Dotel.bsp.schedule.delay=100");
            command.add("-Dotel.service.name=causeway-tracing-compatibility");
        }
        command.add("-cp");
        command.add(System.getProperty(
                "surefire.test.class.path", System.getProperty("java.class.path")));
        command.add(MicrometerTracingAgentFixture.class.getName());

        final Path outputPath = Files.createTempFile("causeway-tracing-fixture-", ".log");
        try {
            final Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectOutput(outputPath.toFile())
                    .start();
            final boolean completed = process.waitFor(
                    PROCESS_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly().waitFor();
                fail("Tracing compatibility fixture timed out\n"
                        + Files.readString(outputPath, StandardCharsets.UTF_8));
            }
            final String output = Files.readString(outputPath, StandardCharsets.UTF_8);
            return new ProcessResult(process.exitValue(), output);
        } finally {
            Files.deleteIfExists(outputPath);
        }
    }

    private static String javaExecutable() {
        final String executable = System.getProperty("os.name").toLowerCase().contains("win")
                ? "java.exe"
                : "java";
        final String configuredJavaHome = System.getProperty(
                FIXTURE_JAVA_HOME_PROPERTY,
                System.getenv().getOrDefault(
                        FIXTURE_JAVA_HOME_ENV,
                        System.getProperty("java.home")));
        return Paths.get(configuredJavaHome, "bin", executable).toString();
    }

    private static ExportedSpan spanNamed(
            final List<ExportedSpan> spans,
            final String name,
            final String processOutput) {
        return spans.stream()
                .filter(span -> name.equals(span.name))
                .findFirst()
                .orElseGet(() -> fail("Span not exported: " + name + "\n"
                        + describe(spans) + "\n" + processOutput));
    }

    private static ExportedSpan spanNamedInTrace(
            final List<ExportedSpan> spans,
            final String traceId,
            final String name,
            final String processOutput) {
        return spans.stream()
                .filter(span -> traceId.equals(span.traceId))
                .filter(span -> name.equals(span.name))
                .findFirst()
                .orElseGet(() -> fail("Span not exported in expected trace: " + name + "\n"
                        + describe(spans) + "\n" + processOutput));
    }

    private static ExportedSpan childSpanNamed(
            final List<ExportedSpan> spans,
            final ExportedSpan parent,
            final String name,
            final String processOutput) {
        return spans.stream()
                .filter(span -> name.equals(span.name))
                .filter(span -> parent.traceId.equals(span.traceId))
                .filter(span -> parent.spanId.equals(span.parentSpanId))
                .findFirst()
                .orElseGet(() -> fail("Span is not a child of its expected parent: " + name + "\n"
                        + describe(spans) + "\n" + processOutput));
    }

    private static ExportedSpan spanWithAttribute(
            final List<ExportedSpan> spans,
            final String attributeName,
            final String attributeValue,
            final String processOutput) {
        return spans.stream()
                .filter(span -> attributeValue.equals(span.attributes.get(attributeName)))
                .findFirst()
                .orElseGet(() -> fail("Span not exported with "
                        + attributeName + "=" + attributeValue + "\n"
                        + describe(spans) + "\n" + processOutput));
    }

    private static ExportedSpan ancestorWithAttribute(
            final List<ExportedSpan> spans,
            final ExportedSpan descendant,
            final String attributeName,
            final String attributeValue,
            final String processOutput) {
        String parentSpanId = descendant.parentSpanId;
        while (!parentSpanId.isEmpty()) {
            final String expectedSpanId = parentSpanId;
            final ExportedSpan parent = spans.stream()
                    .filter(span -> descendant.traceId.equals(span.traceId))
                    .filter(span -> expectedSpanId.equals(span.spanId))
                    .findFirst()
                    .orElse(null);
            if (parent == null) {
                break;
            }
            if (attributeValue.equals(parent.attributes.get(attributeName))) {
                return parent;
            }
            parentSpanId = parent.parentSpanId;
        }
        return fail("Span has no ancestor with " + attributeName + "=" + attributeValue + "\n"
                + describe(spans) + "\n" + processOutput);
    }

    private static String describe(final List<ExportedSpan> spans) {
        final StringBuilder buf = new StringBuilder("Exported spans:");
        for (ExportedSpan span : spans) {
            buf.append(System.lineSeparator())
                    .append(span.name)
                    .append(" traceId=").append(span.traceId)
                    .append(" spanId=").append(span.spanId)
                    .append(" parentSpanId=").append(span.parentSpanId)
                    .append(" attributes=").append(span.attributes);
        }
        return buf.toString();
    }

    private static String hex(final ByteString bytes) {
        final char[] digits = "0123456789abcdef".toCharArray();
        final char[] result = new char[bytes.size() * 2];
        for (int i = 0; i < bytes.size(); i++) {
            final int value = bytes.byteAt(i) & 0xff;
            result[i * 2] = digits[value >>> 4];
            result[i * 2 + 1] = digits[value & 0x0f];
        }
        return new String(result);
    }

    private static final class OtlpTraceCollector implements AutoCloseable {
        private final List<byte[]> requests = Collections.synchronizedList(new ArrayList<>());
        private final HttpServer server;

        private OtlpTraceCollector() throws IOException {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/v1/traces", this::collect);
            server.start();
        }

        private String endpoint() {
            return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/traces";
        }

        private void collect(final HttpExchange exchange) throws IOException {
            try (InputStream requestBody = requestBody(exchange)) {
                requests.add(requestBody.readAllBytes());
            }
            exchange.getResponseHeaders().add("Content-Type", "application/x-protobuf");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        }

        private InputStream requestBody(final HttpExchange exchange) throws IOException {
            final InputStream requestBody = exchange.getRequestBody();
            if ("gzip".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Encoding"))) {
                return new GZIPInputStream(requestBody);
            }
            return requestBody;
        }

        private List<ExportedSpan> exportedSpans() throws IOException {
            final List<ExportedSpan> result = new ArrayList<>();
            synchronized (requests) {
                for (byte[] request : requests) {
                    final ExportTraceServiceRequest export = ExportTraceServiceRequest.parseFrom(
                            new ByteArrayInputStream(request));
                    for (ResourceSpans resourceSpans : export.getResourceSpansList()) {
                        for (ScopeSpans scopeSpans : resourceSpans.getScopeSpansList()) {
                            for (Span span : scopeSpans.getSpansList()) {
                                final Map<String, String> attributes = new LinkedHashMap<>();
                                span.getAttributesList().forEach(attribute -> attributes.put(
                                        attribute.getKey(),
                                        attributeValue(attribute.getValue())));
                                result.add(new ExportedSpan(
                                        span.getName(),
                                        hex(span.getTraceId()),
                                        hex(span.getSpanId()),
                                        hex(span.getParentSpanId()),
                                        attributes,
                                        span.getStatus().getCode().name()));
                            }
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }

    private static String attributeValue(final AnyValue value) {
        switch (value.getValueCase()) {
            case STRING_VALUE:
                return value.getStringValue();
            case INT_VALUE:
                return Long.toString(value.getIntValue());
            case BOOL_VALUE:
                return Boolean.toString(value.getBoolValue());
            case DOUBLE_VALUE:
                return Double.toString(value.getDoubleValue());
            default:
                return value.toString();
        }
    }

    private static final class ProcessResult {
        private final int exitCode;
        private final String output;

        private ProcessResult(final int exitCode, final String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }

    private static final class ExportedSpan {
        private final String name;
        private final String traceId;
        private final String spanId;
        private final String parentSpanId;
        private final Map<String, String> attributes;
        private final String statusCode;

        private ExportedSpan(
                final String name,
                final String traceId,
                final String spanId,
                final String parentSpanId,
                final Map<String, String> attributes,
                final String statusCode) {
            this.name = name;
            this.traceId = traceId;
            this.spanId = spanId;
            this.parentSpanId = parentSpanId;
            this.attributes = attributes;
            this.statusCode = statusCode;
        }
    }
}

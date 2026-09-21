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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import com.google.protobuf.ByteString;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class MicrometerTracingCompatibilityTest {

    private static final Duration PROCESS_TIMEOUT = Duration.ofSeconds(45);

    @Test
    void javaAgentJdbcSpanIsAChildOfMicrometerObservation() throws Exception {
        try (OtlpTraceCollector collector = new OtlpTraceCollector()) {
            final ProcessResult result = runFixture(true, collector.endpoint());

            assertEquals(0, result.exitCode, result.output);
            assertTrue(result.output.contains(MicrometerTracingAgentFixture.SUCCESS_MARKER), result.output);

            final List<ExportedSpan> spans = collector.exportedSpans();
            final ExportedSpan customSpan = spans.stream()
                    .filter(span -> MicrometerTracingAgentFixture.OBSERVATION_NAME.equals(span.name))
                    .findFirst()
                    .orElseGet(() -> fail("Custom Micrometer span not exported.\n" + result.output));
            final ExportedSpan jdbcSpan = spans.stream()
                    .filter(span -> customSpan.traceId.equals(span.traceId))
                    .filter(span -> customSpan.spanId.equals(span.parentSpanId))
                    .findFirst()
                    .orElseGet(() -> fail("No agent span was parented by the custom span.\n"
                            + describe(spans) + "\n" + result.output));

            assertEquals(customSpan.traceId, jdbcSpan.traceId);
            assertEquals(customSpan.spanId, jdbcSpan.parentSpanId);
            System.out.printf(
                    "CAUSEWAY_TRACING_EVIDENCE traceId=%s customSpanId=%s jdbcSpanId=%s jdbcParentSpanId=%s jdbcName=%s%n",
                    customSpan.traceId,
                    customSpan.spanId,
                    jdbcSpan.spanId,
                    jdbcSpan.parentSpanId,
                    jdbcSpan.name);
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

        final Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        final boolean completed = process.waitFor(PROCESS_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        if (!completed) {
            process.destroyForcibly();
            fail("Tracing compatibility fixture timed out");
        }
        final String output = new String(
                process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new ProcessResult(process.exitValue(), output);
    }

    private static String javaExecutable() {
        final String executable = System.getProperty("os.name").toLowerCase().contains("win")
                ? "java.exe"
                : "java";
        return Paths.get(System.getProperty("java.home"), "bin", executable).toString();
    }

    private static String describe(final List<ExportedSpan> spans) {
        final StringBuilder buf = new StringBuilder("Exported spans:");
        for (ExportedSpan span : spans) {
            buf.append(System.lineSeparator())
                    .append(span.name)
                    .append(" traceId=").append(span.traceId)
                    .append(" spanId=").append(span.spanId)
                    .append(" parentSpanId=").append(span.parentSpanId);
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
                                result.add(new ExportedSpan(
                                        span.getName(),
                                        hex(span.getTraceId()),
                                        hex(span.getSpanId()),
                                        hex(span.getParentSpanId())));
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

        private ExportedSpan(
                final String name,
                final String traceId,
                final String spanId,
                final String parentSpanId) {
            this.name = name;
            this.traceId = traceId;
            this.spanId = spanId;
            this.parentSpanId = parentSpanId;
        }
    }
}

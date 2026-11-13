/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.regressiontests.tracing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.opentelemetry.proto.trace.v1.Span;

/** Opt-in offline capture only: never filters or forwards an exported span. */
final class SpanDurationCapture {
    private SpanDurationCapture() { }

    static void write(final Class<?> fixture, final String mode, final List<Span> spans) throws Exception {
        var directory = System.getProperty("causeway.tracing.evaluation.capture-dir");
        if (directory == null || directory.isBlank()) return;
        var path = Path.of(directory);
        Files.createDirectories(path);
        var file = Files.createTempFile(path, mode + "-" + fixture.getSimpleName() + "-", ".json");
        var manifest = spans.stream().map(span -> quote(hex(span.getTraceId().toByteArray()) + ":"
                + hex(span.getSpanId().toByteArray()))).collect(Collectors.joining(","));
        var json = "{\"format\":\"causeway-duration-corpus-v1\",\"mode\":" + quote(mode)
                + ",\"fixture\":" + quote(fixture.getSimpleName())
                + ",\"closed\":true,\"closure\":\"offline captured dataset after fixture process exit; not distributed completion\""
                + ",\"observed_manifest\":[" + manifest + "],\"spans\":["
                + spans.stream().map(SpanDurationCapture::json).collect(Collectors.joining(",")) + "]}";
        Files.writeString(file, json);
    }

    private static String json(final Span span) {
        var exception = span.getEventsList().stream()
                .anyMatch(event -> event.getName().toLowerCase(Locale.ROOT).contains("exception"));
        var errorAttribute = span.getAttributesList().stream().anyMatch(attribute ->
                ((attribute.getKey().equals("error.type") || attribute.getKey().startsWith("exception."))
                        && !attribute.getValue().getStringValue().isBlank())
                || (attribute.getKey().equals("error") && attribute.getValue().getBoolValue()));
        var code = span.getStatus().getCodeValue();
        var error = (code != 0 && code != 1) || exception || errorAttribute;
        return "{\"trace_id\":" + quote(hex(span.getTraceId().toByteArray()))
                + ",\"span_id\":" + quote(hex(span.getSpanId().toByteArray()))
                + ",\"parent_id\":" + quote(hex(span.getParentSpanId().toByteArray()))
                + ",\"name\":" + quote(span.getName()) + ",\"kind\":" + quote(span.getKind().name())
                + ",\"start_ns\":" + span.getStartTimeUnixNano() + ",\"end_ns\":" + span.getEndTimeUnixNano()
                + ",\"error\":" + error + ",\"wire\":"
                + quote(Base64.getEncoder().encodeToString(span.toByteArray())) + "}";
    }

    private static String hex(final byte[] bytes) { return HexFormat.of().formatHex(bytes); }

    static String quote(final String value) {
        var result = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') result.append('\\').append(c);
            else if (c < 32) result.append(String.format("\\u%04x", (int)c));
            else result.append(c);
        }
        return result.append('"').toString();
    }
}

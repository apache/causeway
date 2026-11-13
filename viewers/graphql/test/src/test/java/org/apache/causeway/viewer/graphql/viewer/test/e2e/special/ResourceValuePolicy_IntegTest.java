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
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR  CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.graphql.viewer.test.e2e.special;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@Order(62)
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
public class ResourceValuePolicy_IntegTest extends Abstract_IntegTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DynamicPropertySource
    static void resourceValuePolicy(final DynamicPropertyRegistry registry) {
        registry.add(
                "causeway.viewer.graphql.resources.value-content-response-type",
                CausewayConfiguration.Viewer.Graphql.ResponseType.DIRECT::name);
        registry.add("causeway.viewer.graphql.resources.inline-input-max-bytes", () -> 8);
        registry.add("causeway.viewer.graphql.resources.inline-output-max-bytes", () -> 8);
    }

    @Test
    void oversizedActionResultsRemainMetadataOnly() throws Exception {
        var response = executeGraphQl("""
                {
                  rich {
                    university_calc_Calculator {
                      sampleBlob { invoke { results { name byteLength transferMode inlineOutputMaxBytes base64 } } }
                      sampleClob { invoke { results { name byteLength transferMode inlineOutputMaxBytes chars } } }
                    }
                  }
                }
                """, Map.of());

        assertThat(response.at("/errors").isMissingNode()).isTrue();
        var calculator = response.at("/data/rich/university_calc_Calculator");
        assertThat(calculator.at("/sampleBlob/invoke/results/byteLength").intValue()).isEqualTo(11);
        assertThat(calculator.at("/sampleBlob/invoke/results/transferMode").stringValue())
                .isEqualTo("METADATA_ONLY");
        assertThat(calculator.at("/sampleBlob/invoke/results/inlineOutputMaxBytes").intValue()).isEqualTo(8);
        assertThat(calculator.at("/sampleBlob/invoke/results/base64").isNull()).isTrue();
        assertThat(calculator.at("/sampleClob/invoke/results/transferMode").stringValue())
                .isEqualTo("METADATA_ONLY");
        assertThat(calculator.at("/sampleClob/invoke/results/chars").isNull()).isTrue();
    }

    @Test
    void oversizedAndMalformedInputsAreRejectedWithoutContentDisclosure(
            final CapturedOutput output) throws Exception {
        var oversizedSecret = "PRIVATE_RESOURCE_CONTENT";
        var oversized = executeGraphQl("""
                query Oversized($value: ClobInput!) {
                  rich {
                    university_calc_Calculator {
                      echoClob { invoke(value: $value) { results { name } } }
                    }
                  }
                }
                """, Map.of("value", Map.of(
                        "name", "private.txt",
                        "mimeType", "text/plain",
                        "chars", oversizedSecret)));
        assertThat(oversized.at("/errors/0/message").stringValue())
                .contains("configured inline byte limit")
                .doesNotContain(oversizedSecret);

        var malformedSecret = "PRIVATE!";
        var malformed = executeGraphQl("""
                query Malformed($value: BlobInput!) {
                  rich {
                    university_calc_Calculator {
                      echoBlob { invoke(value: $value) { results { name } } }
                    }
                  }
                }
                """, Map.of("value", Map.of(
                        "name", "private.txt",
                        "mimeType", "text/plain",
                        "base64", malformedSecret)));
        assertThat(malformed.at("/errors/0/message").stringValue())
                .contains("invalid base64 content")
                .doesNotContain(malformedSecret);
        assertThat(output)
                .doesNotContain(oversizedSecret)
                .doesNotContain(malformedSecret);
    }

    @Test
    void passwordAndUnknownOutputRemainNonDisclosing(
            final CapturedOutput output) throws Exception {
        var passwordInput = "PRIVATE_PASSWORD_VARIABLE";
        var response = executeGraphQl("""
                query Protected($password: Password!) {
                  rich {
                    university_calc_Calculator {
                      secretPassword { invoke { results } }
                      echoPassword { invoke(value: $password) { results } }
                      someLocale { invoke { results } }
                      sampleUnmappedValue { invoke { results } }
                    }
                  }
                }
                """, Map.of("password", passwordInput));

        var calculator = response.at("/data/rich/university_calc_Calculator");
        assertThat(calculator.at("/secretPassword/invoke/results").stringValue()).isEqualTo("suppressed");
        assertThat(calculator.at("/echoPassword/invoke/results").stringValue()).isEqualTo("suppressed");
        assertThat(calculator.at("/someLocale/invoke/results").stringValue()).isEqualTo("en-GB");
        assertThat(calculator.at("/sampleUnmappedValue/invoke/results").stringValue()).isEqualTo("[unsupported]");
        assertThat(response.toString())
                .doesNotContain("NEVER_DISCLOSE_THIS_PASSWORD")
                .doesNotContain("NEVER_DISCLOSE_UNMAPPED_VALUE")
                .doesNotContain(passwordInput);
        assertThat(output)
                .doesNotContain("NEVER_DISCLOSE_THIS_PASSWORD")
                .doesNotContain("NEVER_DISCLOSE_UNMAPPED_VALUE")
                .doesNotContain(passwordInput);
    }

    @Test
    void scalarAndUnsupportedVariableErrorsDoNotEchoValues(
            final CapturedOutput output) throws Exception {
        var temporalSecret = "PRIVATE_INVALID_TEMPORAL";
        var malformedTemporal = executeGraphQl("""
                query MalformedTemporal($value: LocalDateTime!) {
                  rich {
                    university_calc_Calculator {
                      echoLocalDateTime { invoke(value: $value) { results } }
                    }
                  }
                }
                """, Map.of("value", temporalSecret));
        assertThat(malformedTemporal.at("/errors/0/message").stringValue())
                .contains("Invalid LocalDateTime value")
                .doesNotContain(temporalSecret);

        var urlSecret = "PRIVATE_INVALID_URL";
        var malformedUrl = executeGraphQl("""
                query MalformedUrl($value: Url!) {
                  rich {
                    university_calc_Calculator {
                      echoUrl { invoke(value: $value) { results } }
                    }
                  }
                }
                """, Map.of("value", urlSecret));
        assertThat(malformedUrl.at("/errors/0/message").stringValue())
                .contains("Invalid Url value")
                .doesNotContain(urlSecret);

        var unsupportedSecret = "PRIVATE_UNSUPPORTED_VALUE";
        var unsupported = executeGraphQl("""
                query Unsupported($value: UnsupportedValue!) {
                  rich {
                    university_calc_Calculator {
                      echoUnmappedValue { invoke(value: $value) { results } }
                    }
                  }
                }
                """, Map.of("value", unsupportedSecret));
        assertThat(unsupported.at("/errors/0/message").stringValue())
                .contains("no reversible GraphQL input strategy")
                .doesNotContain(unsupportedSecret);
        assertThat(output)
                .doesNotContain(temporalSecret)
                .doesNotContain(urlSecret)
                .doesNotContain(unsupportedSecret);
    }

    private JsonNode executeGraphQl(
            final String query,
            final Map<String, ?> variables) throws Exception {
        var requestBody = objectMapper.writeValueAsString(Map.of(
                "query", query,
                "variables", variables));
        var request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://0.0.0.0:%d/graphql", port)))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        var response = HttpClient.newHttpClient().send(
                request,
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        return objectMapper.readTree(response.body());
    }
}

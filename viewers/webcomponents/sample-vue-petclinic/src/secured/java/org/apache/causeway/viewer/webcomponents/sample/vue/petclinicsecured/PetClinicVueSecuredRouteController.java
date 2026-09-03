/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.sample.vue.petclinicsecured;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PetClinicVueSecuredRouteController {

    private static final String AUTHENTICATION_CONTEXT_MARKER =
            "<meta name=\"causeway-authentication-context\" content=\"\">";
    private final ClassPathResource applicationDocument = new ClassPathResource("static/vue/index.html");

    @GetMapping(value = {
            "/vue",
            "/vue/",
            "/vue/index.html",
            "/vue/object/{logicalType}/{objectId}",
            "/vue/invalid-route"
    }, produces = MediaType.TEXT_HTML_VALUE)
    ResponseEntity<String> vueApplication(final HttpServletRequest request) throws IOException {
        final var document = applicationDocument.getContentAsString(StandardCharsets.UTF_8);
        if (!document.contains(AUTHENTICATION_CONTEXT_MARKER)) {
            throw new IllegalStateException("The Vue application document has no authentication-context marker.");
        }
        final var contextEndpoint = escape(request.getContextPath() + "/vue/authentication");
        final var securedDocument = document.replace(
                AUTHENTICATION_CONTEXT_MARKER,
                "<meta name=\"causeway-authentication-context\" content=\"" + contextEndpoint + "\">");
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.TEXT_HTML)
                .body(securedDocument);
    }

    private static String escape(final String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

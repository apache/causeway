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
package org.apache.causeway.viewer.webcomponents.htmx;

import java.util.LinkedHashSet;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("${causeway.viewer.webcomponents.htmx.base-path:/htmx}")
public class HtmxViewerController {

    private static final MediaType HTML_UTF8 = MediaType.parseMediaType("text/html;charset=UTF-8");
    private static final String CONTENT_SECURITY_POLICY = "default-src 'self'; script-src 'self'; style-src 'self'; "
            + "style-src-elem 'self'; style-src-attr 'none'; img-src 'self' data:; connect-src 'self'; "
            + "object-src 'none'; base-uri 'self'; frame-ancestors 'self'";
    private static final List<String> VAADIN_REFERENCE_STYLE_HASHES = List.of(
            "sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw=",
            "sha256-LGebpGBP4rWWgHT+HLo2ODJGtFNV4EbTdFjEntFbBEQ=",
            "sha256-ziQO1YDNfjUz1uv42IGxQ5sgC85OPgAo+omSWhbRRdE=",
            "sha256-/SVoMIwewnXJnEBdXJkzrloVkCW9YHHQ40uLtX2rU0g=");
    private static final List<String> VAADIN_BASIC_STYLE_HASHES = List.of(
            "sha256-0wLqlhzs6Y30XLr3aVbYP1PYgStuEbKPfSQ0hPe+kY4=",
            "sha256-O1QX2gxOlzGqL6KzAmcekP8ficJnQCCVqetcFYUb5ss=",
            "sha256-bpNhRhOAoAX1rQ5VBdLwe5ATkB9Cp6xAt67TmIjsL8c=",
            "sha256-peTsiXSLpuSs7cD42trfTzmd120BSxFbCN5N2acsJGw=",
            "sha256-rRcVg9KnRtadgAGRvS1IlkrlQsH3shuO3yHW7A2DZEk=",
            "sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw=");
    private static final List<String> VAADIN_NUMERIC_STYLE_HASHES = List.of(
            "sha256-8YLhGMhYZnbpzrpjhu2GmLRimv2CABlByy++wN9OR0w=",
            "sha256-YNq3C4skMjxorxPrwhiBBUB3WVp43O5zI8oMR56ES64=",
            "sha256-rRcVg9KnRtadgAGRvS1IlkrlQsH3shuO3yHW7A2DZEk=",
            "sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw=");
    private static final List<String> VAADIN_ACTION_STYLE_HASHES = List.of(
            "sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw=");
    private static final List<String> VAADIN_GRID_STYLE_HASHES = List.of(
            "sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw=");
    private static final List<String> VAADIN_LOCAL_TEMPORAL_STYLE_HASHES = List.of(
            "sha256-3QT3eM+q9TclSqSU3m57G/bQwWnIhIFfAxgKI5k9zxs=",
            "sha256-EJ7xFeV2ubzFN71/RQAb1cN8ak1I1ZC/6W+5JllfWto=",
            "sha256-Mi+i7Phh1UOjZ0x/qGAS342TU+vn7xG5xDIzcKxXhiU=",
            "sha256-WVYjqCndm5Rg7tULwhGngT2GtzCD2oFrfyb9r6y+dZQ=",
            "sha256-liOe7KQsCbiDStYZNHBXpf+AEetcU+X9G3OcPZBW0Ho=",
            "sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw=");

    private final HtmxRouteCodec routeCodec;
    private final HtmxPageRenderer renderer;
    private final HtmxViewerProperties properties;
    private HtmxAuthenticationShell authenticationShell;

    public HtmxViewerController(
            final HtmxRouteCodec routeCodec,
            final HtmxPageRenderer renderer,
            final HtmxViewerProperties properties) {
        this.routeCodec = routeCodec;
        this.renderer = renderer;
        this.properties = properties;
    }

    @Autowired(required = false)
    void setAuthenticationShell(final HtmxAuthenticationShell authenticationShell) {
        this.authenticationShell = authenticationShell;
    }

    @GetMapping({"", "/", "/**"})
    public ResponseEntity<String> route(final HttpServletRequest request) {
        final var rawPath = applicationPath(request);
        String fragment;
        String canonicalPath;
        String routeState;
        if (routeCodec.isRootPath(rawPath)) {
            fragment = renderer.renderLandingFragment();
            canonicalPath = routeCodec.rootPath();
            routeState = "landing";
        } else {
            HtmxObjectRoute objectRoute;
            try {
                objectRoute = routeCodec.parseObjectPath(rawPath);
                fragment = renderer.renderObjectFragment(objectRoute);
                canonicalPath = routeCodec.objectPath(objectRoute);
                routeState = "loading";
            } catch (InvalidHtmxRouteException ex) {
                fragment = renderer.renderInvalidRouteFragment();
                canonicalPath = routeCodec.rootPath();
                routeState = "invalid-route";
            }
        }

        final var historyRestoreRequest = isTrue(request.getHeader("HX-History-Restore-Request"));
        final var fragmentRequest = isTrue(request.getHeader("HX-Request")) || historyRestoreRequest;
        final var contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        final var body = fragmentRequest
                ? fragment
                : renderer.renderShell(
                        contextPath,
                        fragment,
                        canonicalPath,
                        authenticationShell == null
                                ? java.util.Optional.empty()
                                : authenticationShell.state(request));
        final var response = ResponseEntity.ok()
                .contentType(HTML_UTF8)
                .cacheControl(CacheControl.noStore().cachePrivate())
                .header(HttpHeaders.VARY, "HX-Request", "HX-History-Restore-Request")
                .header("X-Causeway-Route-State", routeState);
        if (fragmentRequest && !historyRestoreRequest) {
            response.header("HX-Push-Url", contextPath + canonicalPath);
        } else if (!fragmentRequest) {
            response.header("Content-Security-Policy", contentSecurityPolicy());
        }
        return response.body(body);
    }

    private String contentSecurityPolicy() {
        final var hashes = new LinkedHashSet<String>();
        if (properties.isEffectiveVaadinReferenceWidgets()) {
            hashes.addAll(VAADIN_REFERENCE_STYLE_HASHES);
        }
        if (properties.isEffectiveVaadinActionButtons()) {
            hashes.addAll(VAADIN_ACTION_STYLE_HASHES);
        }
        if (properties.isEffectiveVaadinCollectionGrid()) {
            hashes.addAll(VAADIN_GRID_STYLE_HASHES);
        }
        for (final var family : properties.getEffectiveVaadinFieldFamilies().split(",")) {
            hashes.addAll(switch (family) {
                case "basic" -> VAADIN_BASIC_STYLE_HASHES;
                case "numeric" -> VAADIN_NUMERIC_STYLE_HASHES;
                case "local-temporal" -> VAADIN_LOCAL_TEMPORAL_STYLE_HASHES;
                default -> List.of();
            });
        }
        if (hashes.isEmpty()) {
            return CONTENT_SECURITY_POLICY;
        }
        final var sources = hashes.stream()
                .map(hash -> "'" + hash + "'")
                .collect(java.util.stream.Collectors.joining(" "));
        final var vaadinStylePolicy = "style-src 'self' " + sources
                + "; style-src-elem 'self' " + sources
                + "; style-src-attr 'none';";
        return CONTENT_SECURITY_POLICY.replace(
                "style-src 'self'; style-src-elem 'self'; style-src-attr 'none';",
                vaadinStylePolicy);
    }

    private String applicationPath(final HttpServletRequest request) {
        final var requestUri = request.getRequestURI();
        final var contextPath = request.getContextPath();
        if (contextPath == null || contextPath.isEmpty()) {
            return requestUri;
        }
        if (!requestUri.startsWith(contextPath)) {
            throw new InvalidHtmxRouteException("The requested application route is invalid.");
        }
        return requestUri.substring(contextPath.length());
    }

    private static boolean isTrue(final String value) {
        return "true".equalsIgnoreCase(value);
    }
}

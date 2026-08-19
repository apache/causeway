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

import jakarta.servlet.http.HttpServletRequest;

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
            + "img-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'self'";

    private final HtmxRouteCodec routeCodec;
    private final HtmxPageRenderer renderer;

    public HtmxViewerController(final HtmxRouteCodec routeCodec, final HtmxPageRenderer renderer) {
        this.routeCodec = routeCodec;
        this.renderer = renderer;
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
                : renderer.renderShell(contextPath, fragment, canonicalPath);
        final var response = ResponseEntity.ok()
                .contentType(HTML_UTF8)
                .cacheControl(CacheControl.noStore().cachePrivate())
                .header(HttpHeaders.VARY, "HX-Request", "HX-History-Restore-Request")
                .header("X-Causeway-Route-State", routeState);
        if (fragmentRequest && !historyRestoreRequest) {
            response.header("HX-Push-Url", contextPath + canonicalPath);
        } else if (!fragmentRequest) {
            response.header("Content-Security-Policy", CONTENT_SECURITY_POLICY);
        }
        return response.body(body);
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

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
package org.apache.causeway.viewer.webcomponents.vue.security.secman;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

final class VueSecmanLoginPageFilter extends OncePerRequestFilter {

    private final VueSecmanPaths paths;
    private final VueSecmanSecurityProperties securityProperties;

    VueSecmanLoginPageFilter(
            final VueSecmanPaths paths,
            final VueSecmanSecurityProperties securityProperties) {
        this.paths = paths;
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
        final var path = request.getRequestURI().substring(request.getContextPath().length());
        if (!"GET".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        if (path.equals(paths.loginStylesheetPath())) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=3600");
            response.setContentType("text/css;charset=UTF-8");
            response.getWriter().write(stylesheet());
            return;
        }
        if (!path.equals(paths.loginPath())) {
            filterChain.doFilter(request, response);
            return;
        }
        final var csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, max-age=0");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8");
        response.getWriter().write(loginPage(request, csrfToken));
    }

    private String loginPage(final HttpServletRequest request, final CsrfToken csrfToken) {
        final var invalid = request.getParameter("error") != null;
        final var signedOut = request.getParameter("logout") != null;
        final var requestedContinue = request.getParameter("continue");
        final var safeContinue = paths.isSafeViewerDestination(requestedContinue) ? requestedContinue : null;
        final var outcome = invalid
                ? "<p id=\"login-outcome\" class=\"login-error\" role=\"alert\">Sign-in failed. Check your username and password.</p>"
                : signedOut
                        ? "<p id=\"login-outcome\" class=\"login-success\" role=\"status\">You have signed out.</p>"
                        : "";
        final var describedBy = outcome.isEmpty() ? "" : " aria-describedby=\"login-outcome\"";
        final var continueField = safeContinue == null
                ? ""
                : "<input type=\"hidden\" name=\"continue\" value=\"" + escape(safeContinue) + "\">";
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <meta name="color-scheme" content="light dark">
                  <meta name="robots" content="noindex,nofollow">
                  <title>Sign in · %s</title>
                  <link rel="stylesheet" href="%s">
                </head>
                <body>
                  <main class="login-card" aria-labelledby="login-title">
                    <div class="login-brand" aria-hidden="true">C</div>
                    <h1 id="login-title">Sign in to %s</h1>
                    %s
                    <form method="post" action="%s"%s>
                      <input type="hidden" name="%s" value="%s">
                      %s
                      <label for="username">Username</label>
                      <input id="username" name="username" type="text" autocomplete="username" required autofocus>
                      <label for="password">Password</label>
                      <input id="password" name="password" type="password" autocomplete="current-password" required>
                      <button type="submit">Sign in</button>
                    </form>
                  </main>
                </body>
                </html>
                """.formatted(
                        escape(securityProperties.getBrand()),
                        escape(request.getContextPath() + paths.loginStylesheetPath()),
                        escape(securityProperties.getBrand()),
                        outcome,
                        escape(request.getContextPath() + paths.loginPath()),
                        describedBy,
                        escape(csrfToken.getParameterName()),
                        escape(csrfToken.getToken()),
                        continueField);
    }

    private static String stylesheet() {
        return """
                :root{font-family:system-ui,sans-serif;color-scheme:light dark}*{box-sizing:border-box}
                body{min-height:100vh;margin:0;display:grid;place-items:center;background:#eef2f7;color:#172033}
                .login-card{width:min(92vw,28rem);padding:2rem;border:1px solid #c7d0df;border-radius:.75rem;background:white;box-shadow:0 1rem 3rem #20304a22}
                .login-brand{width:3rem;height:3rem;display:grid;place-items:center;border:3px solid #173f73;border-radius:50%;color:#173f73;font-size:1.4rem;font-weight:800}
                h1{margin:.9rem 0 1.5rem;font-size:1.55rem}form{display:grid;gap:.65rem}label{font-weight:650}
                input{min-height:2.75rem;padding:.55rem .7rem;border:1px solid #8592a6;border-radius:.35rem;font:inherit}
                button{min-height:2.75rem;margin-top:.7rem;border:0;border-radius:.35rem;background:#17569b;color:white;font:inherit;font-weight:700;cursor:pointer}
                input:focus-visible,button:focus-visible{outline:3px solid #55a7ff;outline-offset:2px}
                .login-error{padding:.75rem;border-left:4px solid #a4262c;background:#fff0f0}.login-success{padding:.75rem;border-left:4px solid #247a3c;background:#effaf2}
                @media(prefers-color-scheme:dark){body{background:#111827;color:#e5e7eb}.login-card{background:#1f2937;border-color:#4b5563}.login-brand{color:#93c5fd;border-color:#93c5fd}.login-error{background:#471d21}.login-success{background:#173e25}}
                """;
    }

    private static String escape(final String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

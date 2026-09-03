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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtmxClasspathShellLoaderTest {

    private static final String RESOURCE_PATH = "META-INF/causeway/webcomponents/shells/htmx.html";

    @TempDir
    Path temporaryDirectory;

    @Test
    void usesBuiltInDefaultWhenApplicationDoesNotProvideAShell() {
        final var shell = loader().load();

        assertThat(shell.applicationAuthored()).isFalse();
        assertThat(shell.safeSourceIdentifier()).isEqualTo("internal:default-shell.html");
        assertThat(shell.render()).contains("<body class=\"causeway-app-shell\">");
    }

    @Test
    void loadsOneApplicationShellAndRejectsDistinctDuplicates() {
        final var html = shell("<header><cw-menubars></cw-menubars></header>");
        final var loaded = loader(named("one", html)).load();

        assertThat(loaded.applicationAuthored()).isTrue();
        assertThat(loaded.safeSourceIdentifier()).isEqualTo("resource:htmx.html");
        assertThat(loaded.render()).isEqualTo(html);

        assertFailure("HTMX_SHELL_DUPLICATE", loader(named("one", html), named("two", html)));
    }

    @Test
    void cachedAndReloadModesRetainRegistrationButDifferForCurrentContent() {
        final var cachedResource = mutable("cached", shell("<cw-menubars></cw-menubars>"));
        final var cached = loader(HtmxViewerProperties.ResourcePageMode.CACHED, cachedResource).load();
        cachedResource.set(shell("<aside><cw-menubars></cw-menubars></aside>"));
        assertThat(cached.render()).doesNotContain("<aside>");

        final var reloadResource = mutable("reload", shell("<cw-menubars></cw-menubars>"));
        final var reload = loader(HtmxViewerProperties.ResourcePageMode.RELOAD, reloadResource).load();
        reloadResource.set(shell("<aside><cw-menubars></cw-menubars></aside>"));
        assertThat(reload.render()).contains("<aside>");

        reloadResource.set("<body>defective</body>");
        assertRenderFailure(reload, "HTMX_SHELL_GRAPHQL_CLIENT_INVALID");
    }

    @Test
    void reappliesEveryBoundedContentValidationWithoutStaleFallback() {
        final var resource = mutable("reload", shell("<cw-menubars></cw-menubars>"));
        final var loaded = loader(HtmxViewerProperties.ResourcePageMode.RELOAD, resource).load();

        resource.set(new byte[0]);
        assertRenderFailure(loaded, "HTMX_SHELL_EMPTY");
        resource.set(new byte[]{(byte) 0xc3, (byte) 0x28});
        assertRenderFailure(loaded, "HTMX_SHELL_INVALID_UTF8");
        resource.set("<body>\0</body>");
        assertRenderFailure(loaded, "HTMX_SHELL_NUL_CONTENT");
        resource.set(new byte[HtmxClasspathShellLoader.MAXIMUM_SHELL_BYTES + 1]);
        assertRenderFailure(loaded, "HTMX_SHELL_SIZE_EXCEEDED");
        resource.unreadable = true;
        assertRenderFailure(loaded, "HTMX_SHELL_UNREADABLE");
    }

    @Test
    void rejectsMalformedStructureAndUnknownBindingsAtStartup() {
        assertFailure("HTMX_SHELL_BODY_INVALID", loader(named("one", "<div>not a body</div>")));
        assertFailure("HTMX_SHELL_DOCUMENT_BOUNDARY_INVALID", loader(named(
                "one", shell("<head></head><cw-menubars></cw-menubars>"))));
        assertFailure("HTMX_SHELL_OBJECT_CONTEXT_INVALID", loader(named(
                "one", shell("<cw-menubars></cw-menubars><cw-object-context></cw-object-context>"))));
        assertFailure("HTMX_SHELL_MENUBARS_INVALID", loader(named("one", shell(""))));
        assertFailure("HTMX_SHELL_BINDING_UNRESOLVED", loader(named(
                "one", shell("<cw-menubars></cw-menubars>{{causeway.unknown}}"))));
        assertFailure("HTMX_SHELL_ROUTE_SLOT_INVALID", loader(named(
                "one", shell("<cw-menubars></cw-menubars>").replace("{{causeway.routeContent}}", ""))));
    }

    @Test
    void discoversApplicationShellFromExplodedDirectoryAndDependencyJar() throws Exception {
        final var exploded = temporaryDirectory.resolve("exploded");
        final var shellPath = exploded.resolve(RESOURCE_PATH);
        Files.createDirectories(shellPath.getParent());
        Files.writeString(shellPath, shell("<cw-menubars></cw-menubars>"), StandardCharsets.UTF_8);
        try (var classLoader = new URLClassLoader(new java.net.URL[]{exploded.toUri().toURL()}, null)) {
            final var loaded = new HtmxClasspathShellLoader(
                    new PathMatchingResourcePatternResolver(classLoader)).load();
            assertThat(loaded.applicationAuthored()).isTrue();
        }

        final var jar = temporaryDirectory.resolve("shell.jar");
        try (var output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry(RESOURCE_PATH));
            output.write(shell("<aside><cw-menubars></cw-menubars></aside>").getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        try (var classLoader = new URLClassLoader(new java.net.URL[]{jar.toUri().toURL()}, null)) {
            final var loaded = new HtmxClasspathShellLoader(
                    new PathMatchingResourcePatternResolver(classLoader)).load();
            assertThat(loaded.render()).contains("<aside>");
        }
    }

    @Test
    void reloadRegistrationDoesNotFollowAddedOrRemovedResources() throws Exception {
        final var root = temporaryDirectory.resolve("registration");
        try (var initialClassLoader = new URLClassLoader(new java.net.URL[]{root.toUri().toURL()}, null)) {
            final var noShell = new HtmxClasspathShellLoader(
                    new PathMatchingResourcePatternResolver(initialClassLoader),
                    HtmxViewerProperties.ResourcePageMode.RELOAD).load();
            assertThat(noShell.applicationAuthored()).isFalse();
        }

        final var path = root.resolve(RESOURCE_PATH);
        Files.createDirectories(path.getParent());
        Files.writeString(path, shell("<cw-menubars></cw-menubars>"), StandardCharsets.UTF_8);
        try (var registeredClassLoader = new URLClassLoader(new java.net.URL[]{root.toUri().toURL()}, null)) {
            final var registered = new HtmxClasspathShellLoader(
                    new PathMatchingResourcePatternResolver(registeredClassLoader),
                    HtmxViewerProperties.ResourcePageMode.RELOAD).load();
            Files.delete(path);
            assertRenderFailure(registered, "HTMX_SHELL_UNREADABLE");
        }
    }

    private static void assertFailure(final String code, final HtmxClasspathShellLoader loader) {
        assertThatThrownBy(loader::load)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(code)
                .hasMessageNotContaining("fixture secret");
    }

    private static void assertRenderFailure(final HtmxShellDefinition shell, final String code) {
        assertThatThrownBy(shell::render)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(code)
                .hasMessageNotContaining("fixture secret")
                .hasMessageNotContaining("causeway-app-shell");
    }

    private static HtmxClasspathShellLoader loader(final Resource... resources) {
        return loader(HtmxViewerProperties.ResourcePageMode.CACHED, resources);
    }

    private static HtmxClasspathShellLoader loader(
            final HtmxViewerProperties.ResourcePageMode mode,
            final Resource... resources) {
        return new HtmxClasspathShellLoader(new FixedResolver(resources), mode);
    }

    private static Resource named(final String identity, final String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "htmx.html";
            }

            @Override
            public String getDescription() {
                return "fixture secret:" + identity;
            }
        };
    }

    private static MutableResource mutable(final String identity, final String content) {
        return new MutableResource(identity, content.getBytes(StandardCharsets.UTF_8));
    }

    private static String shell(final String menuMarkup) {
        return """
                <body class="causeway-app-shell">
                  <cw-graphql-client endpoint="{{causeway.graphQlEndpoint}}">
                    %s
                    {{causeway.authenticationChrome}}
                    <div id="causeway-route-loading"></div>
                    <div id="causeway-route-announcement"></div>
                    <cw-action-results id="causeway-result"></cw-action-results>
                    <main id="causeway-route">{{causeway.routeContent}}</main>
                  </cw-graphql-client>
                </body>
                """.formatted(menuMarkup);
    }

    private static final class MutableResource extends AbstractResource {

        private final String identity;
        private byte[] content;
        private boolean unreadable;

        private MutableResource(final String identity, final byte[] content) {
            this.identity = identity;
            this.content = content;
        }

        void set(final String content) {
            set(content.getBytes(StandardCharsets.UTF_8));
        }

        void set(final byte[] content) {
            this.content = content;
            this.unreadable = false;
        }

        @Override
        public String getDescription() {
            return "fixture secret:" + identity;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (unreadable) {
                throw new IOException("fixture secret exception");
            }
            return new ByteArrayInputStream(content);
        }
    }

    private static final class FixedResolver implements ResourcePatternResolver {

        private final Resource[] resources;

        private FixedResolver(final Resource[] resources) {
            this.resources = resources;
        }

        @Override
        public Resource[] getResources(final String locationPattern) {
            assertThat(locationPattern).isEqualTo(HtmxClasspathShellLoader.LOCATION);
            return resources;
        }

        @Override
        public Resource getResource(final String location) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ClassLoader getClassLoader() {
            return getClass().getClassLoader();
        }
    }
}

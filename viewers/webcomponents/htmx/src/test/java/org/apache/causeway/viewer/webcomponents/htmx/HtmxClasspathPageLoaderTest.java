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
import java.util.Arrays;
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

class HtmxClasspathPageLoaderTest {

    private static final String RESOURCE_DIRECTORY = "META-INF/causeway/webcomponents/pages/";

    @TempDir
    Path temporaryDirectory;

    @Test
    void loadsLiteralUtf8ResourceWithExactLogicalType() {
        final var loader = loader(named("petclinic.PetOwner.html", "<p>Olé {{objectId}}</p>"));

        final var pages = loader.load();

        assertThat(pages).hasSize(1);
        assertThat(pages.get(0).logicalTypeName()).isEqualTo("petclinic.PetOwner");
        assertThat(pages.get(0).source()).isEqualTo(HtmxPageDefinition.Source.RESOURCE);
        assertThat(pages.get(0).safeSourceIdentifier()).isEqualTo("resource:petclinic.PetOwner.html");
        assertThat(pages.get(0).render(new HtmxObjectRoute("petclinic.PetOwner", "opaque")))
                .isEqualTo("<p>Olé {{objectId}}</p>");
    }

    @Test
    void cachedAndReloadModesRetainRegistrationButDifferForCurrentContent() {
        final var cachedResource = mutable("petclinic.Cached.html", "<p>initial cached</p>");
        final var cached = loader(HtmxViewerProperties.ResourcePageMode.CACHED, cachedResource)
                .load().get(0);
        cachedResource.set("<p>changed cached</p>");
        assertThat(cached.render(route("petclinic.Cached"))).isEqualTo("<p>initial cached</p>");

        final var reloadResource = mutable("petclinic.Reload.html", "<p>initial reload</p>");
        final var reloading = loader(HtmxViewerProperties.ResourcePageMode.RELOAD, reloadResource)
                .load().get(0);
        reloadResource.set("<p>changed once</p>");
        assertThat(reloading.render(route("petclinic.Reload"))).isEqualTo("<p>changed once</p>");
        reloadResource.set("<p>changed twice</p>");
        assertThat(reloading.render(route("petclinic.Reload"))).isEqualTo("<p>changed twice</p>");
        assertThat(reloading.logicalTypeName()).isEqualTo("petclinic.Reload");
    }

    @Test
    void reloadReappliesEveryBoundedContentValidationWithoutStaleFallback() {
        final var resource = mutable("petclinic.Reload.html", "<p>valid</p>");
        final var page = loader(HtmxViewerProperties.ResourcePageMode.RELOAD, resource)
                .load().get(0);

        resource.set(new byte[0]);
        assertRenderFailure(page, "HTMX_PAGE_EMPTY");
        resource.set(new byte[] {(byte) 0xc3, (byte) 0x28});
        assertRenderFailure(page, "HTMX_PAGE_INVALID_UTF8");
        resource.set("<p>\0</p>");
        assertRenderFailure(page, "HTMX_PAGE_NUL_CONTENT");
        resource.set(new byte[HtmxClasspathPageLoader.MAXIMUM_PAGE_BYTES + 1]);
        assertRenderFailure(page, "HTMX_PAGE_SIZE_EXCEEDED");
        resource.unreadable = true;
        assertRenderFailure(page, "HTMX_PAGE_UNREADABLE");
    }

    @Test
    void additionsDeletionsAndRenamesRemainStartupBoundInBothModes() throws Exception {
        final var pagesDirectory = temporaryDirectory.resolve(RESOURCE_DIRECTORY);
        Files.createDirectories(pagesDirectory);
        final var original = pagesDirectory.resolve("petclinic.Original.html");
        Files.writeString(original, "<p>original</p>", StandardCharsets.UTF_8);
        try (var classLoader = new URLClassLoader(
                new java.net.URL[] {temporaryDirectory.toUri().toURL()},
                null)) {
            final var resolver = new PathMatchingResourcePatternResolver(classLoader);
            final var cached = new HtmxClasspathPageLoader(
                    resolver,
                    HtmxViewerProperties.ResourcePageMode.CACHED).load();
            final var reload = new HtmxClasspathPageLoader(
                    resolver,
                    HtmxViewerProperties.ResourcePageMode.RELOAD).load();

            Files.writeString(
                    pagesDirectory.resolve("petclinic.Added.html"),
                    "<p>added</p>",
                    StandardCharsets.UTF_8);
            Files.move(original, pagesDirectory.resolve("petclinic.Renamed.html"));

            assertThat(cached).extracting(HtmxPageDefinition::logicalTypeName)
                    .containsExactly("petclinic.Original");
            assertThat(cached.get(0).render(route("petclinic.Original")))
                    .isEqualTo("<p>original</p>");
            assertThat(reload).extracting(HtmxPageDefinition::logicalTypeName)
                    .containsExactly("petclinic.Original");
            assertThatThrownBy(() -> reload.get(0).render(route("petclinic.Original")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("HTMX_PAGE_UNREADABLE")
                    .hasMessageContaining("resource:petclinic.Original.html")
                    .hasMessageNotContaining(temporaryDirectory.toString());
        }
    }

    @Test
    void discoversExplodedAndJarResources() throws Exception {
        final var explodedPage = temporaryDirectory.resolve(RESOURCE_DIRECTORY).resolve("petclinic.Pet.html");
        Files.createDirectories(explodedPage.getParent());
        Files.writeString(explodedPage, "<p>pet</p>", StandardCharsets.UTF_8);
        final var jar = temporaryDirectory.resolve("pages.jar");
        try (var output = new JarOutputStream(Files.newOutputStream(jar))) {
            for (final var directory : new String[] {
                    "META-INF/",
                    "META-INF/causeway/",
                    "META-INF/causeway/webcomponents/",
                    RESOURCE_DIRECTORY}) {
                output.putNextEntry(new JarEntry(directory));
                output.closeEntry();
            }
            output.putNextEntry(new JarEntry(RESOURCE_DIRECTORY + "petclinic.Visit.html"));
            output.write("<p>visit</p>".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        try (var classLoader = new URLClassLoader(
                new java.net.URL[] {temporaryDirectory.toUri().toURL(), jar.toUri().toURL()},
                null)) {
            final var pages = new HtmxClasspathPageLoader(
                    new PathMatchingResourcePatternResolver(classLoader)).load();

            assertThat(pages).extracting(HtmxPageDefinition::logicalTypeName)
                    .containsExactly("petclinic.Pet", "petclinic.Visit");
        }
    }

    @Test
    void rejectsInvalidNameEmptyMalformedUtf8NulAndOversize() {
        assertFailure("HTMX_PAGE_INVALID_NAME", loader(named("bad name.html", "<p/>")));
        assertFailure("HTMX_PAGE_EMPTY", loader(named("petclinic.Empty.html", new byte[0])));
        assertFailure("HTMX_PAGE_INVALID_UTF8", loader(named(
                "petclinic.Encoding.html", new byte[] {(byte) 0xc3, (byte) 0x28})));
        assertFailure("HTMX_PAGE_NUL_CONTENT", loader(named(
                "petclinic.Nul.html", "<p>\0</p>".getBytes(StandardCharsets.UTF_8))));
        assertFailure("HTMX_PAGE_SIZE_EXCEEDED", loader(named(
                "petclinic.Large.html", new byte[HtmxClasspathPageLoader.MAXIMUM_PAGE_BYTES + 1])));
    }

    @Test
    void rejectsUnreadableDiscoveryAndPageCountOverflow() {
        final Resource unreadable = new AbstractResource() {
            @Override
            public String getDescription() {
                return "secret filesystem location";
            }

            @Override
            public String getFilename() {
                return "petclinic.Unreadable.html";
            }

            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("secret exception detail");
            }
        };
        assertThatThrownBy(() -> loader(unreadable).load())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("HTMX_PAGE_UNREADABLE")
                .hasMessageContaining("resource:petclinic.Unreadable.html")
                .hasMessageNotContaining("filesystem")
                .hasMessageNotContaining("exception detail");

        final ResourcePatternResolver failingResolver = new FixedResolver(new Resource[0]) {
            @Override
            public Resource[] getResources(final String locationPattern) throws IOException {
                throw new IOException("secret discovery detail");
            }
        };
        assertThatThrownBy(() -> new HtmxClasspathPageLoader(failingResolver).load())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("HTMX_PAGE_DISCOVERY_FAILED")
                .hasMessageNotContaining("secret discovery detail");

        final var excessive = new Resource[HtmxClasspathPageLoader.MAXIMUM_PAGE_COUNT + 1];
        Arrays.setAll(excessive, index -> named("sample.Type" + index + ".html", "<p/>"));
        assertFailure("HTMX_PAGE_COUNT_EXCEEDED", new HtmxClasspathPageLoader(new FixedResolver(excessive)));
    }

    private static void assertFailure(final String code, final HtmxClasspathPageLoader loader) {
        assertThatThrownBy(loader::load)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(code)
                .hasMessageNotContaining("Byte array resource");
    }

    private static void assertRenderFailure(final HtmxPageDefinition page, final String code) {
        assertThatThrownBy(() -> page.render(route(page.logicalTypeName())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(code)
                .hasMessageContaining("resource:petclinic.Reload.html")
                .hasMessageNotContaining("fixture secret")
                .hasMessageNotContaining("<p>valid</p>");
    }

    private static HtmxObjectRoute route(final String logicalTypeName) {
        return new HtmxObjectRoute(logicalTypeName, "opaque");
    }

    private static HtmxClasspathPageLoader loader(final Resource... resources) {
        return loader(HtmxViewerProperties.ResourcePageMode.CACHED, resources);
    }

    private static HtmxClasspathPageLoader loader(
            final HtmxViewerProperties.ResourcePageMode mode,
            final Resource... resources) {
        return new HtmxClasspathPageLoader(new FixedResolver(resources), mode);
    }

    private static Resource named(final String filename, final String content) {
        return named(filename, content.getBytes(StandardCharsets.UTF_8));
    }

    private static Resource named(final String filename, final byte[] content) {
        return new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }

            @Override
            public String getDescription() {
                return "fixture:" + filename;
            }
        };
    }

    private static MutableResource mutable(final String filename, final String content) {
        return new MutableResource(filename, content.getBytes(StandardCharsets.UTF_8));
    }

    private static final class MutableResource extends AbstractResource {

        private final String filename;
        private byte[] content;
        private boolean unreadable;

        private MutableResource(final String filename, final byte[] content) {
            this.filename = filename;
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
        public String getFilename() {
            return filename;
        }

        @Override
        public String getDescription() {
            return "fixture secret location:" + filename;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (unreadable) {
                throw new IOException("fixture secret exception");
            }
            return new ByteArrayInputStream(content);
        }
    }

    private static class FixedResolver implements ResourcePatternResolver {

        private final Resource[] resources;

        FixedResolver(final Resource[] resources) {
            this.resources = resources;
        }

        @Override
        public Resource[] getResources(final String locationPattern) throws IOException {
            assertThat(locationPattern).isEqualTo(HtmxClasspathPageLoader.LOCATION);
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

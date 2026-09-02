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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.causeway.viewer.webcomponents.htmx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtmxClasspathPreviewLoaderTest {

    @Test
    void loadsExactLogicalTypeAndHonorsCachedAndReloadModes() {
        final var cachedResource = mutable("petclinic.PetOwner.html", "<cw-peek><cw-property id=\"name\"></cw-property></cw-peek>");
        final var cached = loader(HtmxViewerProperties.ResourcePageMode.CACHED, cachedResource).load().get(0);
        cachedResource.set("<p>changed</p>");
        assertThat(cached.logicalTypeName()).isEqualTo("petclinic.PetOwner");
        assertThat(cached.safeSourceIdentifier()).isEqualTo("resource:petclinic.PetOwner.html");
        assertThat(cached.content()).contains("cw-peek");

        final var reloadResource = mutable("petclinic.Reload.html", "<p>initial</p>");
        final var reload = loader(HtmxViewerProperties.ResourcePageMode.RELOAD, reloadResource).load().get(0);
        reloadResource.set("<p>current</p>");
        assertThat(reload.content()).isEqualTo("<p>current</p>");
    }

    @Test
    void rejectsInvalidContentNamesDiscoveryAndCountWithoutLeakingDetails() {
        assertFailure("HTMX_PREVIEW_INVALID_NAME", named("bad name.html", "<p/>"));
        assertFailure("HTMX_PREVIEW_EMPTY", named("petclinic.Empty.html", new byte[0]));
        assertFailure("HTMX_PREVIEW_INVALID_UTF8", named(
                "petclinic.Encoding.html", new byte[] {(byte) 0xc3, (byte) 0x28}));
        assertFailure("HTMX_PREVIEW_NUL_CONTENT", named(
                "petclinic.Nul.html", "<p>\0</p>".getBytes(StandardCharsets.UTF_8)));
        assertFailure("HTMX_PREVIEW_SIZE_EXCEEDED", named(
                "petclinic.Large.html",
                new byte[HtmxClasspathPreviewLoader.MAXIMUM_PRESENTATION_BYTES + 1]));

        final ResourcePatternResolver failing = new FixedResolver(new Resource[0]) {
            @Override
            public Resource[] getResources(final String locationPattern) throws IOException {
                throw new IOException("secret discovery path");
            }
        };
        assertThatThrownBy(() -> loader(HtmxViewerProperties.ResourcePageMode.CACHED, failing).load())
                .hasMessageContaining("HTMX_PREVIEW_DISCOVERY_FAILED")
                .hasMessageNotContaining("secret");

        final var excessive = new Resource[HtmxClasspathPreviewLoader.MAXIMUM_PRESENTATION_COUNT + 1];
        Arrays.setAll(excessive, index -> named("sample.Type" + index + ".html", "<p/>"));
        assertThatThrownBy(() -> loader(HtmxViewerProperties.ResourcePageMode.CACHED, new FixedResolver(excessive)).load())
                .hasMessageContaining("HTMX_PREVIEW_COUNT_EXCEEDED");
    }

    @Test
    void registryRejectsDuplicatesAndFindsOnlyExactKeys() {
        final var one = HtmxPreviewDefinition.resource("petclinic.PetOwner", "resource:one", "<p>one</p>");
        final var two = HtmxPreviewDefinition.resource("petclinic.PetOwner", "resource:two", "<p>two</p>");
        assertThatThrownBy(() -> new HtmxPreviewRegistry(java.util.List.of(one, two)))
                .hasMessageContaining("HTMX_PREVIEW_DUPLICATE");
        final var registry = new HtmxPreviewRegistry(java.util.List.of(one));
        assertThat(registry.find("petclinic.PetOwner")).contains(one);
        assertThat(registry.find("../petclinic.PetOwner")).isEmpty();
    }

    private static HtmxClasspathPreviewLoader loader(
            final HtmxViewerProperties.ResourcePageMode mode,
            final Resource... resources) {
        return loader(mode, new FixedResolver(resources));
    }

    private static HtmxClasspathPreviewLoader loader(
            final HtmxViewerProperties.ResourcePageMode mode,
            final ResourcePatternResolver resolver) {
        return new HtmxClasspathPreviewLoader(resolver, mode);
    }

    private static void assertFailure(final String code, final Resource resource) {
        assertThatThrownBy(() -> loader(HtmxViewerProperties.ResourcePageMode.CACHED, resource).load())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(code);
    }

    private static Resource named(final String filename, final String text) {
        return named(filename, text.getBytes(StandardCharsets.UTF_8));
    }

    private static Resource named(final String filename, final byte[] bytes) {
        return new ByteArrayResource(bytes, "bounded") {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private static MutableResource mutable(final String filename, final String text) {
        return new MutableResource(filename, text.getBytes(StandardCharsets.UTF_8));
    }

    private static class FixedResolver implements ResourcePatternResolver {
        private final Resource[] resources;

        FixedResolver(final Resource[] resources) {
            this.resources = resources;
        }

        @Override
        public Resource[] getResources(final String locationPattern) throws IOException {
            return resources;
        }

        @Override
        public Resource getResource(final String location) {
            return resources.length == 0 ? named("sample.Empty.html", new byte[0]) : resources[0];
        }

        @Override
        public ClassLoader getClassLoader() {
            return getClass().getClassLoader();
        }
    }

    private static final class MutableResource extends AbstractResource {
        private final String filename;
        private byte[] bytes;

        MutableResource(final String filename, final byte[] bytes) {
            this.filename = filename;
            this.bytes = bytes;
        }

        void set(final String text) {
            bytes = text.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String getDescription() {
            return "mutable";
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }
    }
}

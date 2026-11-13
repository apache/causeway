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
package org.apache.causeway.viewer.webcomponents.sample.vue.petclinic;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ResourceLoader;

import org.apache.causeway.viewer.webcomponents.sample.petclinic.domain.PetOwner;
import org.apache.causeway.viewer.webcomponents.sample.petclinic.domain.PetOwnerRepository;
import org.apache.causeway.viewer.webcomponents.sample.petclinic.domain.ViewerFallback;
import org.apache.causeway.viewer.webcomponents.sample.petclinic.domain.VisitRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = PetClinicVueApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PetClinicVueApplicationIntegTest {

    private static final Pattern SCRIPT_SOURCE = Pattern.compile("src=\"(/vue/assets/[^\"]+\\.js)\"");

    @LocalServerPort
    private int port;

    @Autowired
    private PetOwnerRepository ownerRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    void reusesDeterministicPetclinicDomain() {
        final var mary = ownerRepository.findById(PetOwner.MARY_ID);
        assertThat(mary).isNotNull();
        assertThat(mary.getName()).isEqualTo("Mary Smith");
        assertThat(ownerRepository.findAll()).hasSize(10);
        assertThat(visitRepository.findByPetOwner(mary)).hasSize(2);
        assertThat(entityManager.find(ViewerFallback.class, ViewerFallback.ID).getMessage())
                .isEqualTo("Rendered by the generic Vue page.");
        assertThat(resourceLoader.getResource("classpath:menubars.layout.xml").exists()).isTrue();
    }

    @Test
    void servesProductionVueApplicationAndCanonicalHistoryFallback() throws Exception {
        final var index = get("/vue/");
        assertThat(index.statusCode()).isEqualTo(200);
        assertThat(index.body())
                .contains("<div id=\"app\"></div>")
                .contains("/causeway-webcomponents/component-styles.css")
                .doesNotContain("/src/main.ts");

        final var objectRoute = get("/vue/object/petclinic.PetOwner/s_owner-mary");
        assertThat(objectRoute.statusCode()).isEqualTo(200);
        assertThat(objectRoute.body()).isEqualTo(index.body());

        final var scriptMatcher = SCRIPT_SOURCE.matcher(index.body());
        assertThat(scriptMatcher.find()).isTrue();
        assertThat(get(scriptMatcher.group(1)).statusCode()).isEqualTo(200);
        assertThat(get("/causeway-webcomponents/index.mjs").statusCode()).isEqualTo(200);
        assertThat(get("/causeway-webcomponents/vaadin-grid/vaadin-grid.js").statusCode()).isEqualTo(200);
        assertThat(get("/webjars/font-awesome/7.3.0/css/all.min.css").statusCode()).isEqualTo(200);
    }

    @Test
    void historyFallbackDoesNotCaptureBackendOrAssetRoutes() throws Exception {
        assertThat(get("/vue/assets/does-not-exist.js").statusCode()).isEqualTo(404);
        assertThat(get("/graphql").body()).doesNotContain("<div id=\"app\"></div>");
        assertThat(get("/not-a-vue-route").body()).doesNotContain("<div id=\"app\"></div>");
    }

    private HttpResponse<String> get(final String path) throws Exception {
        return HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
}

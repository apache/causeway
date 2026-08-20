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
package org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic.domain.PetOwner;
import org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic.domain.PetOwnerRepository;
import org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic.domain.VisitRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = PetClinicHtmxApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PetClinicHtmxApplication_IntegTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private PetOwnerRepository ownerRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Test
    void loadsDeterministicPetclinicFixture() {
        final var mary = ownerRepository.findById(PetOwner.MARY_ID);

        assertThat(mary).isNotNull();
        assertThat(mary.getName()).isEqualTo("Mary Smith");
        assertThat(mary.getPets()).extracting("id")
                .containsExactlyInAnyOrder("pet-basil", "pet-samantha");
        assertThat(visitRepository.findByPetOwner(mary)).extracting("id")
                .containsExactlyInAnyOrder("visit-basil-checkup", "visit-samantha-vaccine");
    }

    @Test
    void servesStableShellCanonicalFragmentsAssetsAndWicketComparison() throws Exception {
        final var shell = get("/htmx");
        assertThat(shell.statusCode()).isEqualTo(200);
        assertThat(shell.headers().firstValue("content-security-policy").orElse(""))
                .contains("default-src 'self'");
        assertThat(shell.body())
                .contains("<causeway-menubars>")
                .contains("id=\"causeway-route\"")
                .contains("hx-history-elt")
                .contains("data-navigation-generation=\"0\"")
                .contains("/causeway-htmx/causeway-htmx.mjs")
                .contains("/webjars/htmx.org/2.0.6/dist/htmx.min.js")
                .contains("Compare Wicket viewer");

        final var generic = get("/htmx/object/petclinic.PetOwner/s_owner-mary", "HX-Request", "true");
        assertThat(generic.statusCode()).isEqualTo(200);
        assertThat(generic.headers().firstValue("hx-push-url").orElse(""))
                .isEqualTo("/htmx/object/petclinic.PetOwner/s_owner-mary");
        assertThat(generic.body())
                .doesNotContain("<!doctype html>")
                .contains("data-page-kind=\"generic\"")
                .contains("logical-type=\"petclinic.PetOwner\"")
                .contains("object-id=\"s_owner-mary\"");

        final var historyRestore = get(
                "/htmx/object/petclinic.PetOwner/s_owner-mary",
                "HX-History-Restore-Request",
                "true");
        assertThat(historyRestore.body())
                .doesNotContain("<!doctype html>")
                .contains("data-page-kind=\"generic\"")
                .containsOnlyOnce("<causeway-object-context");
        assertThat(historyRestore.headers().firstValue("hx-push-url")).isEmpty();

        final var custom = get("/htmx/object/petclinic.HomePage/home-fixture", "HX-Request", "true");
        assertThat(custom.body())
                .contains("data-page-kind=\"custom\"")
                .contains("data-testid=\"petclinic-custom-home\"")
                .containsOnlyOnce("<causeway-object-context");

        assertThat(get("/causeway-htmx/causeway-htmx.mjs").statusCode()).isEqualTo(200);
        assertThat(get("/causeway-webcomponents/component-styles.css").statusCode()).isEqualTo(200);
        assertThat(get("/causeway-webcomponents/theme.css").statusCode()).isEqualTo(200);
        assertThat(get("/webjars/htmx.org/2.0.6/dist/htmx.min.js").statusCode()).isEqualTo(200);

        final var wicket = getWithoutRedirect("/wicket/");
        assertThat(wicket.statusCode()).isIn(200, 302);
        if (wicket.statusCode() == 302) {
            assertThat(wicket.headers().firstValue("location").orElse(""))
                    .contains("/wicket/signin");
        }
    }

    @Test
    void exposesObjectHomeEffectiveMenusAndEffectiveGrid() throws Exception {
        final var application = graphQL("""
                query PetClinicApplicationEntry {
                  rich {
                    application {
                      home {
                        kind
                        logicalTypeName
                        object {
                          __typename
                          ... on rich__petclinic_HomePage {
                            _meta { id logicalTypeName title }
                          }
                        }
                      }
                      menuBars { href mediaType formatVersion generation cacheControl }
                      issues { code message }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(application);
        assertThat(application.at("/data/rich/application/home/kind").asText()).isEqualTo("OBJECT");
        assertThat(application.at("/data/rich/application/home/logicalTypeName").asText()).isEqualTo("petclinic.HomePage");
        assertThat(application.at("/data/rich/application/home/object/_meta/title").asText())
                .contains("4 pet owners", "3 upcoming visits");

        final var menuHref = application.at("/data/rich/application/menuBars/href").asText();
        final var menuBars = get(menuHref);
        assertThat(menuBars.statusCode()).isEqualTo(200);
        assertThat(menuBars.headers().firstValue("cache-control").orElse(""))
                .isEqualTo("private, no-store");
        assertThat(menuBars.body())
                .contains("Pet Owners")
                .contains("objectType=\"petclinic.PetOwners\"")
                .contains("objectType=\"petclinic.Visits\"")
                .contains("id=\"count\"")
                .doesNotContain("findByNameExact");

        final var grid = get("/graphql/object/petclinic.PetOwner:s_owner-mary/_meta/grid");
        assertThat(grid.statusCode()).isEqualTo(200);
        assertThat(grid.headers().firstValue("cache-control").orElse(""))
                .isEqualTo("private, no-store");
        assertThat(grid.body())
                .contains("<bs:grid")
                .contains("<cpt:property id=\"notes\" multiLine=\"5\"")
                .contains("<cpt:collection id=\"pets\"")
                .contains("<cpt:action id=\"addPet\"")
                .contains("<cpt:action id=\"removePet\"")
                .contains("<cpt:collection id=\"visits\"")
                .contains("<cpt:action id=\"bookVisit\"");
    }

    @Test
    void exposesPetclinicServiceAndObjectInteractionsThroughRichGraphQL() throws Exception {
        final var service = graphQL("""
                query PetClinicService {
                  rich {
                    petclinic_PetOwners {
                      listAll { hidden disabled invoke { results { _meta { id logicalTypeName title } } } }
                      findByName { hidden disabled }
                      create { hidden disabled }
                    }
                    petclinic_Visits {
                      listUpcoming { hidden disabled invoke { results { _meta { id logicalTypeName title } } } }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(service);
        assertThat(service.at("/data/rich/petclinic_PetOwners/listAll/invoke/results").size()).isEqualTo(4);
        assertThat(service.at("/data/rich/petclinic_Visits/listUpcoming/invoke/results").size()).isEqualTo(3);

        final var owner = graphQL("""
                query PetClinicOwner {
                  rich {
                    petclinic_PetOwner(object: {id: "s_owner-mary"}) {
                      _meta { id logicalTypeName title grid }
                      name { get }
                      pets { get { _meta { id logicalTypeName title } } }
                      visits { get { _meta { id logicalTypeName title } } }
                      addPet { hidden disabled params { name { validity datatype } } }
                      bookVisit { hidden disabled }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(owner);
        assertThat(owner.at("/data/rich/petclinic_PetOwner/name/get").asText()).isEqualTo("Mary Smith");
        assertThat(owner.at("/data/rich/petclinic_PetOwner/pets/get").size()).isEqualTo(2);
        assertThat(owner.at("/data/rich/petclinic_PetOwner/visits/get").size()).isEqualTo(2);
    }

    private JsonNode graphQL(final String query) throws Exception {
        final var request = HttpRequest.newBuilder(uri("/graphql"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        OBJECT_MAPPER.writeValueAsString(java.util.Map.of("query", query)),
                        StandardCharsets.UTF_8))
                .build();
        return OBJECT_MAPPER.readTree(client().send(request, HttpResponse.BodyHandlers.ofString()).body());
    }

    private void assertNoGraphQLErrors(final JsonNode response) {
        assertThat(response.path("errors").isMissingNode() || response.path("errors").isEmpty())
                .as(response.toPrettyString())
                .isTrue();
    }

    private HttpResponse<String> get(final String path, final String... header) throws Exception {
        final var builder = HttpRequest.newBuilder(uri(path)).GET();
        if (header.length == 2) {
            builder.header(header[0], header[1]);
        }
        return client().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getWithoutRedirect(final String path) throws Exception {
        final var request = HttpRequest.newBuilder(uri(path)).GET().build();
        return HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpClient client() {
        return HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    }

    private URI uri(final String path) {
        return URI.create("http://localhost:" + port + path);
    }
}

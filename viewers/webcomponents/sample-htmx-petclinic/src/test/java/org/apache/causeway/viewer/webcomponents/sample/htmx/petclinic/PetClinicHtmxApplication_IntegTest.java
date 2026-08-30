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
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.apache.causeway.viewer.webcomponents.htmx.HtmxViewerProperties;
import org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic.domain.PetOwner;
import org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic.domain.PetOwnerRepository;
import org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic.domain.VisitRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Autowired
    private HtmxViewerProperties viewerProperties;

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
        final boolean nativeToolkit = nativeToolkit();
        final String csp = shell.headers().firstValue("content-security-policy").orElse("");
        assertThat(csp)
                .contains("default-src 'self'")
                .doesNotContain("'unsafe-inline'");
        if (nativeToolkit) {
            assertThat(csp)
                    .doesNotContain("sha256-")
                    .contains("style-src-attr 'none'");
        } else {
            assertThat(csp)
                    .contains("style-src-attr 'none'")
                    .contains("sha256-0wLqlhzs6Y30XLr3aVbYP1PYgStuEbKPfSQ0hPe+kY4=");
        }
        assertThat(shell.body())
                .contains("<cw-menubars>")
                .contains("id=\"causeway-route\"")
                .contains("hx-history-elt")
                .contains("data-navigation-generation=\"0\"")
                .contains("/causeway-htmx/causeway-htmx.mjs")
                .contains("/webjars/htmx.org/2.0.6/dist/htmx.min.js")
                .contains("/webjars/font-awesome/7.3.0/css/all.min.css")
                .contains("data-causeway-component-toolkit=\"" + (nativeToolkit ? "native" : "vaadin") + "\"")
                .contains("data-causeway-presentation=\"" + (nativeToolkit ? "native" : "vaadin") + "\"")
                .contains("data-causeway-action-buttons=\"" + (nativeToolkit ? "native" : "vaadin") + "\"")
                .contains("data-causeway-reference-widgets=\"" + (nativeToolkit ? "native" : "vaadin") + "\"")
                .contains("data-causeway-field-families=\"" + (nativeToolkit ? "" : "basic,numeric,local-temporal") + "\"")
                .contains("Compare Wicket viewer");

        assertResourcePage(
                "/htmx/object/petclinic.PetOwner/s_owner-mary",
                "petclinic.PetOwner",
                "s_owner-mary",
                "petclinic-owner-page");
        assertThat(get("/htmx/object/petclinic.PetOwner/s_owner-mary").body())
                .contains("<cw-action id=\"delete\" named=\"Remove this owner\"")
                .contains("<cw-action id=\"updateName\" named=\"Change the owner's name\"")
                .contains("<cw-parameter id=\"name\"")
                .contains("named=\"Owner's full name\"")
                .contains("<cw-action id=\"addPet\" named=\"Register a pet\"")
                .contains("<cw-action id=\"removePet\"></cw-action>")
                .contains("<cw-action id=\"bookVisit\">");
        assertResourcePage(
                "/htmx/object/petclinic.Pet/s_pet-basil",
                "petclinic.Pet",
                "s_pet-basil",
                "petclinic-pet-page");
        assertResourcePage(
                "/htmx/object/petclinic.Visit/s_visit-basil-checkup",
                "petclinic.Visit",
                "s_visit-basil-checkup",
                "petclinic-visit-page");
        assertResourcePage(
                "/htmx/object/petclinic.HomePage/home-fixture",
                "petclinic.HomePage",
                "home-fixture",
                "petclinic-custom-home");

        final var historyRestore = get(
                "/htmx/object/petclinic.PetOwner/s_owner-mary",
                "HX-History-Restore-Request",
                "true");
        assertThat(historyRestore.body())
                .doesNotContain("<!doctype html>")
                .contains("data-page-kind=\"custom\"")
                .contains("data-page-source=\"resource\"")
                .containsOnlyOnce("<cw-object-context");
        assertThat(historyRestore.headers().firstValue("hx-push-url")).isEmpty();

        assertThat(get("/causeway-htmx/causeway-htmx.mjs").statusCode()).isEqualTo(200);
        assertThat(get("/causeway-webcomponents/component-styles.css").statusCode()).isEqualTo(200);
        assertThat(get("/causeway-webcomponents/theme.css").statusCode()).isEqualTo(200);
        assertThat(get("/css/application.css").body())
                .contains(".petclinic-object-heading {")
                .contains("gap: var(--causeway-space-5);")
                .contains(".petclinic-object-heading > cw-object-header {")
                .contains(".petclinic-object-heading .causeway-object-header {");
        assertThat(get("/webjars/htmx.org/2.0.6/dist/htmx.min.js").statusCode()).isEqualTo(200);
        assertThat(get("/webjars/font-awesome/7.3.0/css/all.min.css").statusCode()).isEqualTo(200);
        assertThat(get("/webjars/font-awesome/7.3.0/webfonts/fa-solid-900.woff2").statusCode()).isEqualTo(200);

        final var wicket = getWithoutRedirect("/wicket/");
        assertThat(wicket.statusCode()).isIn(200, 302);
        if (wicket.statusCode() == 302) {
            assertThat(wicket.headers().firstValue("location").orElse(""))
                    .contains("/wicket/signin");
        }
    }

    @Test
    void reloadsChangedRegisteredPageAndRejectsInvalidCurrentContent() throws Exception {
        assertThat(viewerProperties.getResourcePageMode())
                .isEqualTo(HtmxViewerProperties.ResourcePageMode.RELOAD);
        final var resource = getClass().getClassLoader().getResource(
                "META-INF/causeway/webcomponents/pages/petclinic.PetOwner.html");
        assertThat(resource).isNotNull();
        assertThat(resource.getProtocol()).isEqualTo("file");
        final var path = Path.of(resource.toURI());
        final var original = Files.readAllBytes(path);
        final var marker = "<p data-reload-verification>current classpath content</p>";
        try {
            Files.writeString(path, marker, StandardCharsets.UTF_8);
            final var changed = get("/htmx/object/petclinic.PetOwner/s_owner-mary");
            assertThat(changed.statusCode()).isEqualTo(200);
            assertThat(changed.body())
                    .contains(marker)
                    .doesNotContain("petclinic-owner-page");

            Files.write(path, new byte[0]);
            final var invalid = get("/htmx/object/petclinic.PetOwner/s_owner-mary");
            assertThat(invalid.statusCode()).isEqualTo(500);
            assertThat(invalid.body())
                    .doesNotContain(marker)
                    .doesNotContain("petclinic-owner-page")
                    .doesNotContain(path.toString());
        } finally {
            Files.write(path, original);
        }
        assertThat(get("/htmx/object/petclinic.PetOwner/s_owner-mary").body())
                .contains("petclinic-owner-page")
                .doesNotContain(marker);
    }

    @Test
    void packagesHtmlPagesAndRetainsLayoutFallbackResources() throws Exception {
        final var loader = getClass().getClassLoader();
        for (final var name : java.util.List.of(
                "petclinic.HomePage.html",
                "petclinic.PetOwner.html",
                "petclinic.Pet.html",
                "petclinic.Visit.html")) {
            final var path = "META-INF/causeway/webcomponents/pages/" + name;
            final var resource = loader.getResource(path);
            assertThat(resource).as(path).isNotNull();
            final String html;
            try (var input = resource.openStream()) {
                html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            assertThat(html)
                    .contains("<cw-object-header")
                    .contains("<cw-")
                    .doesNotContain("<script", " style=", " onclick=", "<vaadin-");
            if (!name.equals("petclinic.HomePage.html")) {
                assertThat(html).contains("<cw-breadcrumbs data-testid=\"petclinic-breadcrumbs\"");
            }
        }
        final String ownerHtml;
        try (var input = loader.getResource(
                "META-INF/causeway/webcomponents/pages/petclinic.PetOwner.html").openStream()) {
            ownerHtml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertThat(ownerHtml)
                .contains("<div class=\"petclinic-object-heading\">\n    <cw-object-header></cw-object-header>\n    <div class=\"petclinic-page-toolbar\" aria-label=\"Owner actions\">\n      <cw-action id=\"delete\"")
                .contains("<cw-property id=\"name\" named=\"Full name\">\n            <cw-action id=\"updateName\"")
                .contains("<cw-parameter id=\"name\"\n                            named=\"Owner's full name\"")
                .contains("description-as=\"tooltip\"")
                .contains("<cw-parameter id=\"name\"\n                          named=\"Pet name\"")
                .contains("<cw-parameter id=\"reason\"\n                          named=\"Reason for visit\"")
                .contains("multi-line=\"3\"")
                .doesNotContain("<cw-parameter id=\"species\"", "<cw-action id=\"removePet\">\n            <cw-parameter")
                .contains("id=\"knownAs\" editable\n                       described-as=\"The familiar or preferred name used by this owner.\"")
                .contains("id=\"notes\" editable multi-line=\"5\"")
                .contains("id=\"lastVisit\" editable label-position=\"TOP\"")
                .contains("<cw-collection id=\"pets\" named=\"Companion animals\" active sortable filterable>")
                .contains("<cw-action id=\"addPet\"")
                .contains("<cw-action id=\"removePet\"")
                .contains("<cw-collection id=\"visits\" named=\"Visit history\"")
                .contains("described-as=\"All visits recorded for this owner's pets.\"")
                .contains("active paged=\"1\"")
                .contains("<cw-action id=\"bookVisit\"")
                .doesNotContain("petclinic-associated-actions", "petclinic-member-composition", " offset=", " size=");
        final String homeHtml;
        try (var input = loader.getResource(
                "META-INF/causeway/webcomponents/pages/petclinic.HomePage.html").openStream()) {
            homeHtml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertThat(homeHtml)
                .contains("<cw-collection id=\"futureVisits\" named=\"Next appointments\" active filterable>")
                .contains("<cw-collection id=\"petOwners\" label=\"Pet owners\" active paged=\"2\" sortable filterable>")
                .doesNotContain("id=\"petOwners\" named=", "id=\"petOwners\" described-as=", " offset=", " size=");
        assertThat(ownerHtml.indexOf("id=\"addPet\""))
                .isLessThan(ownerHtml.indexOf("id=\"removePet\""));
        assertThat(get("/META-INF/causeway/webcomponents/pages/petclinic.PetOwner.html").statusCode())
                .isEqualTo(404);
        assertThat(get("/petclinic.PetOwner.html").statusCode()).isEqualTo(404);
        assertThat(loader.getResource(
                "org/apache/causeway/viewer/webcomponents/sample/htmx/petclinic/domain/PetOwner.layout.xml"))
                .isNotNull();
        assertThat(loader.getResource(
                "org/apache/causeway/viewer/webcomponents/sample/htmx/petclinic/domain/PetOwner#pets.columnOrder.txt"))
                .isNotNull();
        assertThat(loader.getResource("menubars.layout.xml")).isNotNull();
        assertThatThrownBy(() -> Class.forName(
                "org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic.PetClinicHomeFragmentFactory"))
                .isInstanceOf(ClassNotFoundException.class);
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
        final var homeId = application.at("/data/rich/application/home/object/_meta/id").asText();
        final var ownerWindow = graphQL("""
                query PetClinicOwnerWindow {
                  rich {
                    petclinic_HomePage(object: {id: "%s"}) {
                      petOwners {
                        window(search: "mary", sortBy: "name", sortDirection: DESCENDING, size: 2) {
                          totalCount ordering sortableMembers searchSupported searchPrompt
                          rows { name { get } }
                        }
                      }
                      futureVisits {
                        window(search: "vaccination", size: 5) {
                          totalCount searchSupported searchPrompt
                          rows { reason { get } }
                        }
                      }
                    }
                  }
                }
                """.formatted(homeId));
        assertNoGraphQLErrors(ownerWindow);
        assertThat(ownerWindow.at("/data/rich/petclinic_HomePage/petOwners/window/totalCount").asInt()).isEqualTo(1);
        assertThat(ownerWindow.at("/data/rich/petclinic_HomePage/petOwners/window/ordering").asText())
                .isEqualTo("REQUESTED");
        assertThat(ownerWindow.at("/data/rich/petclinic_HomePage/petOwners/window/searchSupported").asBoolean()).isTrue();
        assertThat(ownerWindow.at("/data/rich/petclinic_HomePage/petOwners/window/searchPrompt").asText())
                .isEqualTo("Search owner names");
        assertThat(ownerWindow.at("/data/rich/petclinic_HomePage/petOwners/window/sortableMembers").toString())
                .contains("\"name\"");
        assertThat(ownerWindow.at("/data/rich/petclinic_HomePage/petOwners/window/rows/0/name/get").asText())
                .isEqualTo("Mary Smith");
        assertThat(ownerWindow.at("/data/rich/petclinic_HomePage/futureVisits/window/totalCount").asInt())
                .isEqualTo(1);
        assertThat(ownerWindow.at("/data/rich/petclinic_HomePage/futureVisits/window/searchSupported").asBoolean())
                .isTrue();
        assertThat(ownerWindow.at("/data/rich/petclinic_HomePage/futureVisits/window/searchPrompt").asText())
                .isEqualTo("Search visits");
        assertThat(ownerWindow.at("/data/rich/petclinic_HomePage/futureVisits/window/rows/0/reason/get").asText())
                .isEqualTo("Vaccination");

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
                      listAll {
                        hidden disabled
                        metadata { friendlyName description cssClassFa cssClassFaPosition }
                        invoke { results { _meta { id logicalTypeName title } } }
                      }
                      findByName { hidden disabled }
                      create { hidden disabled metadata { description cssClassFa cssClassFaPosition } }
                    }
                    petclinic_Visits {
                      listUpcoming { hidden disabled invoke { results { _meta { id logicalTypeName title } } } }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(service);
        assertThat(service.at("/data/rich/petclinic_PetOwners/listAll/invoke/results").size()).isEqualTo(4);
        assertThat(service.at("/data/rich/petclinic_PetOwners/listAll/metadata/description").asText())
                .isEqualTo("Lists every registered pet owner.");
        assertThat(service.at("/data/rich/petclinic_PetOwners/listAll/metadata/cssClassFa").asText())
                .isEqualTo("users");
        assertThat(service.at("/data/rich/petclinic_PetOwners/listAll/metadata/cssClassFaPosition").asText())
                .isEqualTo("LEFT");
        assertThat(service.at("/data/rich/petclinic_PetOwners/create/metadata/cssClassFa").asText())
                .isEqualTo("user-plus");
        assertThat(service.at("/data/rich/petclinic_Visits/listUpcoming/invoke/results").size()).isEqualTo(3);

        final var owner = graphQL("""
                query PetClinicOwner {
                  rich {
                    petclinic_PetOwner(object: {id: "s_owner-mary"}) {
                      _meta { id logicalTypeName title grid }
                      name { get }
                      pets {
                        metadata { friendlyName description }
                        get { _meta { id logicalTypeName title } }
                        window(search: "cat", size: 5) {
                          totalCount searchSupported searchPrompt
                          rows { name { get } species { get } }
                        }
                      }
                      visits { metadata { friendlyName description } get { _meta { id logicalTypeName title } } }
                      addPet {
                        hidden disabled
                        metadata { friendlyName description cssClassFa cssClassFaPosition }
                        params { name { validity datatype } }
                      }
                      updateName { metadata { description cssClassFa cssClassFaPosition } }
                      delete { hidden disabled metadata { description cssClassFa cssClassFaPosition } }
                      bookVisit { hidden disabled }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(owner);
        assertThat(owner.at("/data/rich/petclinic_PetOwner/name/get").asText()).isEqualTo("Mary Smith");
        assertThat(owner.at("/data/rich/petclinic_PetOwner/pets/metadata/friendlyName").asText()).isEqualTo("Pets");
        assertThat(owner.at("/data/rich/petclinic_PetOwner/pets/metadata/description").asText())
                .isEqualTo("Pets currently registered to this owner.");
        assertThat(owner.at("/data/rich/petclinic_PetOwner/visits/metadata/friendlyName").asText()).isEqualTo("Visits");
        assertThat(owner.at("/data/rich/petclinic_PetOwner/visits/metadata/description").isNull()).isTrue();
        assertThat(owner.at("/data/rich/petclinic_PetOwner/pets/get").size()).isEqualTo(2);
        assertThat(owner.at("/data/rich/petclinic_PetOwner/pets/window/totalCount").asInt()).isEqualTo(1);
        assertThat(owner.at("/data/rich/petclinic_PetOwner/pets/window/searchSupported").asBoolean()).isTrue();
        assertThat(owner.at("/data/rich/petclinic_PetOwner/pets/window/searchPrompt").asText())
                .isEqualTo("Search pets");
        assertThat(owner.at("/data/rich/petclinic_PetOwner/pets/window/rows/0/name/get").asText())
                .isEqualTo("Samantha");
        assertThat(owner.at("/data/rich/petclinic_PetOwner/visits/get").size()).isEqualTo(2);
        assertThat(owner.at("/data/rich/petclinic_PetOwner/addPet/metadata/description").asText())
                .isEqualTo("Adds a pet to this owner's household.");
        assertThat(owner.at("/data/rich/petclinic_PetOwner/addPet/metadata/cssClassFa").asText())
                .isEqualTo("paw");
        assertThat(owner.at("/data/rich/petclinic_PetOwner/addPet/metadata/cssClassFaPosition").asText())
                .isEqualTo("LEFT");
        assertThat(owner.at("/data/rich/petclinic_PetOwner/updateName/metadata/cssClassFaPosition").asText())
                .isEqualTo("RIGHT");
        assertThat(owner.at("/data/rich/petclinic_PetOwner/delete/disabled").asText())
                .isEqualTo("The fixture owner is retained for the presentation demonstration.");
    }

    @Test
    void exposesNavigableBreadcrumbHierarchyThroughRichGraphQL() throws Exception {
        final var response = graphQL("""
                query PetClinicBreadcrumbs {
                  __type(name: "RichNavigableBreadcrumb") {
                    fields { name type { kind name ofType { kind name } } }
                  }
                  rich {
                    petclinic_PetOwner(object: {id: "s_owner-mary"}) {
                      _meta { breadcrumbs { logicalTypeName id title } }
                    }
                    petclinic_Pet(object: {id: "s_pet-basil"}) {
                      _meta { breadcrumbs { logicalTypeName id title } }
                    }
                    petclinic_Visit(object: {id: "s_visit-basil-checkup"}) {
                      _meta { breadcrumbs { logicalTypeName id title } }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(response);
        assertThat(response.at("/data/__type/fields"))
                .extracting(node -> node.path("name").asText())
                .containsExactlyInAnyOrder("logicalTypeName", "id", "title");
        assertThat(response.at("/data/rich/petclinic_PetOwner/_meta/breadcrumbs")).isEmpty();
        assertThat(response.at("/data/rich/petclinic_Pet/_meta/breadcrumbs"))
                .extracting(node -> node.path("logicalTypeName").asText())
                .containsExactly("petclinic.PetOwner");
        assertThat(response.at("/data/rich/petclinic_Pet/_meta/breadcrumbs/0/id").asText())
                .isEqualTo("s_owner-mary");
        assertThat(response.at("/data/rich/petclinic_Visit/_meta/breadcrumbs"))
                .extracting(node -> node.path("logicalTypeName").asText())
                .containsExactly("petclinic.PetOwner", "petclinic.Pet");
        assertThat(response.at("/data/rich/petclinic_Visit/_meta/breadcrumbs/0/title").asText())
                .isEqualTo("Mary Smith (Mary)");
        assertThat(response.at("/data/rich/petclinic_Visit/_meta/breadcrumbs/1/id").asText())
                .isEqualTo("s_pet-basil");
    }

    private void assertResourcePage(
            final String path,
            final String logicalTypeName,
            final String objectId,
            final String testId) throws Exception {
        final var response = get(path, "HX-Request", "true");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("hx-push-url").orElse(""))
                .isEqualTo(path);
        assertThat(response.body())
                .doesNotContain("<!doctype html>")
                .contains("data-page-kind=\"custom\"")
                .contains("data-page-source=\"resource\"")
                .contains("logical-type=\"" + logicalTypeName + "\"")
                .contains("object-id=\"" + objectId + "\"")
                .contains("data-testid=\"" + testId + "\"")
                .containsOnlyOnce("<cw-object-context")
                .containsOnlyOnce("<cw-interaction-controller");
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

    private static boolean nativeToolkit() {
        return "native".equalsIgnoreCase(System.getProperty(
                "causeway.viewer.webcomponents.htmx.component-toolkit",
                System.getProperty("causeway.viewer.webcomponents.htmx.editor-toolkit", "vaadin")));
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

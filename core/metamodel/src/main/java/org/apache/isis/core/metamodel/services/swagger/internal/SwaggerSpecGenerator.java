/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.metamodel.services.swagger.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import io.swagger.models.Info;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

public class SwaggerSpecGenerator {

    private final SpecificationLoader specificationLoader;

    public SwaggerSpecGenerator(final SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    }

    public String generate(final SwaggerService.Visibility visibility, final SwaggerService.Format format) {
        final Swagger swagger = new Swagger();
        swagger.basePath("/restful");
        swagger.info(new Info()
                        .version("1.0.0")
                        .title("Restful Objects"))
                .path("/",
                        new Path()
                            .get(new Operation()
                                    .description(roSpec("5.1"))
                                    .consumes("application/json")
                                    .consumes("application/json;profile=\"urn:org.restfulobjects:repr-types/home-page\"")
                                    .produces("application/json;profile=\"urn:org.restfulobjects:repr-types/home-page\"")
                                    .response(200,
                                            newResponse(Caching.NON_EXPIRING)
                                            .description("OK")
                                            .schema(new RefProperty("#/definitions/home-page"))
                                    )))
                .path("/user",
                        new Path()
                            .get(new Operation()
                                    .description(roSpec("6.1"))
                                    .consumes("application/json")
                                    .consumes("application/json;profile=\"urn:org.restfulobjects:repr-types/user\"")
                                    .produces("application/json;profile=\"urn:org.restfulobjects:repr-types/user\"")
                                    .response(200,
                                            newResponse(Caching.USER_INFO)
                                            .description("OK")
                                            .schema(new RefProperty("#/definitions/user"))
                                    )))
                .path("/services",
                        new Path()
                                .get(new Operation()
                                        .description(roSpec("7.1"))
                                        .consumes("application/json")
                                        .consumes("application/json;profile=\"urn:org.restfulobjects:repr-types/services\"")
                                        .produces("application/json;profile=\"urn:org.restfulobjects:repr-types/services\"")
                                        .response(200,
                                                newResponse(Caching.USER_INFO)
                                                        .description("OK")
                                                        .schema(new RefProperty("#/definitions/services"))
                                        )))

                .path("/version",
                        new Path()
                            .get(new Operation()
                                    .description(roSpec("8.1"))
                                    .consumes("application/json")
                                    .consumes("application/json;profile=\"urn:org.restfulobjects:repr-types/version\"")
                                    .produces("application/json;profile=\"urn:org.restfulobjects:repr-types/version\"")
                                    .response(200,
                                            newResponse(Caching.NON_EXPIRING)
                                            .description("OK")
                                            .schema(new RefProperty("#/definitions/version"))
                                    )));

        swagger.addDefinition("home-page",
                newModel(roSpec("5.2")));

        swagger.addDefinition("user",
                newModel(roSpec("6.2"))
                    .property("userName", stringProperty())
                    .property("roles", arrayOfStrings())
                    .property("links", arrayOfLinksGetOnly())
                    .required("userName")
                    .required("roles")
                );

        swagger.addDefinition("services",
                newModel(roSpec("7.2"))
                    .property("value", arrayOfLinksGetOnly())
                    .required("userName")
                    .required("roles")
                );

        swagger.addDefinition("version",
                newModel(roSpec("8.2"))
                    .property("specVersion", stringProperty())
                    .property("implVersion", stringProperty())
                    .property("optionalCapabilities",
                            new ObjectProperty()
                                .property("blobsClobs", stringProperty())
                                .property("deleteObjects", stringProperty())
                                .property("domainModel", stringProperty())
                                .property("validateOnly", stringProperty())
                                .property("protoPersistentObjects", stringProperty())
                            )
                    .required("userName")
                    .required("roles")
                );

        swagger.addDefinition("link",
                new ModelImpl()
                    .type("object")
                    .property("rel", stringProperty())
                    .property("href", stringProperty())
                    .property("title", stringProperty())
                    .property("method", stringPropertyEnum("GET", "POST", "PUT", "DELETE"))
                    .property("type", stringProperty())
                    .property("arguments", new ObjectProperty())
                    .required("rel")
                    .required("href")
                    .required("method")
        );

        swagger.addDefinition("link-get-only",
                new ModelImpl()
                    .type("object")
                    .property("rel", stringProperty())
                    .property("href", stringProperty())
                    .property("title", stringProperty())
                    .property("method", stringPropertyEnum("GET"))
                    .property("type", stringProperty())
                    .required("rel")
                    .required("href")
                    .required("method")
        );


        final Collection<ObjectSpecification> allSpecs = specificationLoader.allSpecifications();
        for (ObjectSpecification serviceSpec : allSpecs) {
            final DomainServiceFacet domainServiceFacet = serviceSpec.getFacet(DomainServiceFacet.class);
            if (domainServiceFacet == null) {
                continue;
            }
            if (!isVisible(visibility, domainServiceFacet)) {
                continue;
            }

            List<ObjectAction> serviceActions = serviceSpec
                    .getObjectActions(actionTypesFor(visibility), Contributed.EXCLUDED, Filters.<ObjectAction>any());
            if(serviceActions.isEmpty()) {
                continue;
            }

            String serviceId = serviceIdFor(serviceSpec);
            final String serviceDefinitionId = "service-" + serviceId;

            final Path servicePath = new Path();
            swagger.path("/services/" + serviceId, servicePath);

            servicePath.get(
                    new Operation()
                        .description(roSpec("15.1"))
                        .response(200,
                                newResponse(Caching.TRANSACTIONAL)
                                    .description("OK")
                                    .schema(new RefProperty("#/definitions/"
                                            + serviceDefinitionId)))
            );

            final ObjectProperty serviceMembers = new ObjectProperty();
            ModelImpl serviceRepr =
                    newModel(roSpec("15.1.2") + ": representation of " + serviceId)
                    .property("title", stringProperty())
                    .property("serviceId", stringProperty()._default(serviceId))
                    .property("members", serviceMembers);

            swagger.addDefinition(serviceDefinitionId, serviceRepr);

            for (final ObjectAction serviceAction : serviceActions) {
                String serviceActionId = serviceAction.getId();
                serviceMembers.property(serviceActionId,
                        new ObjectProperty()
                            .property("id", stringPropertyEnum(serviceActionId))
                            .property("memberType", stringPropertyEnum("action"))
                            .property("links",
                                    new ObjectProperty()
                                        .property("rel", stringPropertyEnum("urn:org.restfulobjects:rels/details;action=\"" + serviceActionId + "\""))
                                        .property("href", stringPropertyEnum("actions/"
                                                + serviceActionId)))
                            .property("method", stringPropertyEnum("GET"))
                            .property("type", stringPropertyEnum("application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""))
                );
            }

        }

        switch (format) {
            case JSON:
                return Json.pretty(swagger);
            case YAML:
                try {
                    return Yaml.pretty().writeValueAsString(swagger);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            default:
                throw new IllegalArgumentException("Unrecognized format: " + format);
        }
    }

    boolean isVisible(final SwaggerService.Visibility visibility, final DomainServiceFacet domainServiceFacet) {
        if (domainServiceFacet.getNatureOfService() == NatureOfService.VIEW_REST_ONLY) {
            return true;
        }
        if (visibility.isPublic()) {
            return false;
        }
        return domainServiceFacet.getNatureOfService() == NatureOfService.VIEW_MENU_ONLY ||
               domainServiceFacet.getNatureOfService() == NatureOfService.VIEW;
    }

    private ModelImpl newModel(final String description) {
        return new ModelImpl()
                .description(description)
                .type("object")
                .property("links", arrayOfLinksGetOnly())
                .property("extensions", new MapProperty())
                .required("links")
                .required("extensions");
    }

    // TODO: this is horrid, there ought to be a facet we can call instead...
    String serviceIdFor(final ObjectSpecification serviceSpec) {
        Object tempServiceInstance = InstanceUtil.createInstance(serviceSpec.getCorrespondingClass());
        return ServiceUtil.id(tempServiceInstance);
    }

    StringProperty stringProperty() {
        return new StringProperty();
    }

    StringProperty stringPropertyEnum(String... enumValues) {
        StringProperty stringProperty = stringProperty();
        stringProperty._enum(Arrays.asList(enumValues));
        if(enumValues.length >= 1) {
            stringProperty._default(enumValues[0]);
        }
        return stringProperty;
    }

    ArrayProperty arrayOfLinksGetOnly() {
        return new ArrayProperty()
                .items(new RefProperty("#/definitions/link-get-only"));
    }

    ArrayProperty arrayOfStrings() {
        return new ArrayProperty().items(stringProperty());
    }

    private Response newResponse(final Caching caching) {
        return withCachingHeaders(new Response(), caching);
    }

    enum Caching {
        TRANSACTIONAL {
            @Override public void withHeaders(final Response response) {

            }
        },
        USER_INFO {
            @Override public void withHeaders(final Response response) {
                response
                        .header("Cache-Control",
                                new IntegerProperty()
                                        ._default(3600));
            }
        },
        NON_EXPIRING {
            @Override public void withHeaders(final Response response) {
                response
                        .header("Cache-Control",
                                new IntegerProperty()
                                        ._default(86400).description(roSpec("2.13")));
            }
        };

        public abstract void withHeaders(final Response response);
    }

    private static String roSpec(final String section) {
        return "RO Spec v1.0, section " + section;
    }

    private Response withCachingHeaders(final Response response, final Caching caching) {
        caching.withHeaders(response);

        return response;
    }

    private List<ActionType> actionTypesFor(final SwaggerService.Visibility visibility) {
        switch (visibility) {
        case PUBLIC:
            return Arrays.asList(ActionType.USER);
        case PRIVATE:
            return Arrays.asList(ActionType.USER);
        case PRIVATE_WITH_PROTOTYPING:
            return Arrays.asList(ActionType.USER, ActionType.EXPLORATION, ActionType.PROTOTYPE, ActionType.DEBUG);
        }
        throw new IllegalArgumentException("Unrecognized type '" + visibility + "'");
    }


}

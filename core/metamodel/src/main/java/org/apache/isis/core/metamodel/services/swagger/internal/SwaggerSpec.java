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

import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
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
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Json;

public class SwaggerSpec {

    private final SpecificationLoader specificationLoader;

    public SwaggerSpec(final SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    }

    public String generate(final SwaggerService.Type type) {
        final Swagger swagger = new Swagger();
        swagger.info(new Info()
                        .version("1.0.0")
                        .title("Restful Objects"))
                .path("/",
                        new Path()
                            .get(new Operation()
                                    .description("RO Spec v1.0, section 5.1")
                                    .consumes("application/json")
                                    .consumes("application/json;profile=\"urn:org.restfulobjects:repr-types/home-page\"")
                                    .produces("application/json;profile=\"urn:org.restfulobjects:repr-types/home-page\"")
                                    .response(200,
                                            newResponse(Caching.LONG_TERM)
                                            .description("OK")
                                            .schema(new RefProperty("#/definitions/home-page"))
                                    )))
                .path("/user",
                        new Path()
                            .get(new Operation()
                                    .description("RO Spec v1.0, section 6.1")
                                    .consumes("application/json")
                                    .consumes("application/json;profile=\"urn:org.restfulobjects:repr-types/user\"")
                                    .produces("application/json;profile=\"urn:org.restfulobjects:repr-types/user\"")
                                    .response(200,
                                            newResponse(Caching.SHORT_TERM)
                                            .description("OK")
                                            .schema(new RefProperty("#/definitions/user"))
                                    )))
                .path("/version",
                        new Path()
                            .get(new Operation()
                                    .description("RO Spec v1.0, section 8.1")
                                    .consumes("application/json")
                                    .consumes("application/json;profile=\"urn:org.restfulobjects:repr-types/version\"")
                                    .produces("application/json;profile=\"urn:org.restfulobjects:repr-types/version\"")
                                    .response(200,
                                            newResponse(Caching.LONG_TERM)
                                            .description("OK")
                                            .schema(new RefProperty("#/definitions/version"))
                                    )));

        swagger.addDefinition("home-page",
                new ModelImpl()
                    .description("RO Spec v1.0, section 5.2")
                    .type("object")
                    .property("links", arrayOfLinksGetOnly())
                        .required("links")
                    );

        swagger.addDefinition("user",
                new ModelImpl()
                    .description("RO Spec v1.0, section 6.2")
                    .type("object")
                    .property("userName", stringProperty())
                    .property("roles", arrayOfStrings())
                    .property("links", arrayOfLinksGetOnly())
                    .required("userName")
                    .required("roles")
                    .required("links")
                );

        swagger.addDefinition("version",
                new ModelImpl()
                    .description("RO Spec v1.0, section 8.2")
                    .type("object")
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
                    .property("links", arrayOfLinksGetOnly())
                    .property("extensions", new ObjectProperty())
                    .required("userName")
                    .required("roles")
                    .required("links")
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
            if (type.isPublic() && domainServiceFacet.getNatureOfService() != NatureOfService.VIEW_REST_ONLY) {
                continue;
            }

            List<ObjectAction> serviceActions = serviceSpec
                    .getObjectActions(actionTypesFor(type), Contributed.EXCLUDED, Filters.<ObjectAction>any());
            for (ObjectAction serviceAction : serviceActions) {

            }

        }


        return Json.pretty(swagger);
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

    StringProperty stringProperty() {
        return new StringProperty();
    }

    ArrayProperty arrayOfStrings() {
        return new ArrayProperty().items(stringProperty());
    }

    private Response newResponse(final Caching caching) {
        return withCachingHeaders(new Response(), caching);
    }

    enum Caching {
        NO_CACHING {
            @Override public void withHeaders(final Response response) {

            }
        },
        SHORT_TERM {
            @Override public void withHeaders(final Response response) {
                response
                        .header("Cache-Control",
                                new IntegerProperty()
                                        ._default(3600));
            }
        },
        LONG_TERM {
            @Override public void withHeaders(final Response response) {
                response
                        .header("Cache-Control",
                                new IntegerProperty()
                                        ._default(86400));
            }
        };

        public abstract void withHeaders(final Response response);
    }
    private Response withCachingHeaders(final Response response, final Caching caching) {
        caching.withHeaders(response);

        return response;
    }

    private List<ActionType> actionTypesFor(final SwaggerService.Type type) {
        switch (type) {
        case PUBLIC:
            return Arrays.asList(ActionType.USER);
        case PRIVATE:
            return Arrays.asList(ActionType.USER);
        case PRIVATE_WITH_PROTOTYPING:
            return Arrays.asList(ActionType.USER, ActionType.EXPLORATION, ActionType.PROTOTYPE, ActionType.DEBUG);
        }
        throw new IllegalArgumentException("Unrecognized type '" + type + "'");
    }


}

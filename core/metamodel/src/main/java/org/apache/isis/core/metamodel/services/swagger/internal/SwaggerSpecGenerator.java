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
import com.google.common.base.Strings;

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

import io.swagger.models.Info;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
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
                        .title("Restful Objects"));

        appendBoilerplate(swagger);
        appendLinkDefinitions(swagger);

        final Collection<ObjectSpecification> allSpecs = specificationLoader.allSpecifications();
        for (final ObjectSpecification serviceSpec :  allSpecs) {

            final DomainServiceFacet domainServiceFacet = serviceSpec.getFacet(DomainServiceFacet.class);
            if (domainServiceFacet == null) {
                continue;
            }
            if (visibility.isPublic() &&
                domainServiceFacet.getNatureOfService() != NatureOfService.VIEW_REST_ONLY) {
                continue;
            }
            if (domainServiceFacet.getNatureOfService() != NatureOfService.VIEW_MENU_ONLY &&
                domainServiceFacet.getNatureOfService() != NatureOfService.VIEW) {
                continue;
            }

            final List<ActionType> actionTypes = actionTypesFor(visibility);
            final List<ObjectAction> serviceActions = serviceSpec.getObjectActions(
                    actionTypes, Contributed.EXCLUDED, Filters.<ObjectAction>any());
            if (serviceActions.isEmpty()) {
                continue;
            }

            final ObjectProperty serviceMembers = appendServicePath(swagger, serviceSpec);

            for (final ObjectAction serviceAction : serviceActions) {
                if (visibility.isPublic() && !isVisibleForPublic(serviceAction)) {
                    continue;
                }
                appendActionTo(serviceMembers, serviceAction);
                appendServiceActionInvokePath(swagger, serviceAction);
            }
        }

        for (final ObjectSpecification objectSpec : allSpecs) {

            final DomainServiceFacet domainServiceFacet = objectSpec.getFacet(DomainServiceFacet.class);
            if (domainServiceFacet != null) {
                continue;
            }

            if(objectSpec.isAbstract()) {
                continue;
            }

            final List<ActionType> actionTypes = actionTypesFor(visibility);
            final List<ObjectAction> objectActions = objectSpec.getObjectActions(
                    actionTypes, Contributed.INCLUDED, Filters.<ObjectAction>any());
            if (objectActions.isEmpty()) {
                continue;
            }

            final ObjectProperty objectMembers = appendObjectPath(swagger, objectSpec);

            for (final ObjectAction objectAction : objectActions) {
                if (visibility.isPublic() && !isVisibleForPublic(objectAction)) {
                    continue;
                }
                appendActionTo(objectMembers, objectAction);
                appendObjectActionInvokePath(swagger, objectAction);
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

    void appendBoilerplate(final Swagger swagger) {
        appendBoilerplatePaths(swagger);
        appendBoilerplateDefinitions(swagger);
    }

    void appendBoilerplatePaths(final Swagger swagger) {
        swagger.path("/",
                new Path()
                        .get(new Operation()
                                .tag("boilerplate")
                                .description(roSpec("5.1"))
                                .produces("application/json")
                                .produces("application/json;profile=urn:org.restfulobjects:repr-types/home-page")
                                .response(200,
                                        newResponse(Caching.NON_EXPIRING)
                                                .description("OK")
                                                .schema(new RefProperty("#/definitions/home-page"))
                                )))
                .path("/user",
                        new Path()
                                .get(new Operation()
                                        .tag("boilerplate")
                                        .description(roSpec("6.1"))
                                        .produces("application/json")
                                        .produces("application/json;profile=urn:org.restfulobjects:repr-types/user")
                                        .response(200,
                                                newResponse(Caching.USER_INFO)
                                                        .description("OK")
                                                        .schema(new RefProperty("#/definitions/user"))
                                        )))
                .path("/services",
                        new Path()
                                .get(new Operation()
                                        .tag("boilerplate")
                                        .description(roSpec("7.1"))
                                        .produces("application/json")
                                        .produces("application/json;profile=urn:org.restfulobjects:repr-types/services")
                                        .response(200,
                                                newResponse(Caching.USER_INFO)
                                                        .description("OK")
                                                        .schema(new RefProperty("#/definitions/services"))
                                        )))

                .path("/version",
                        new Path()
                                .get(new Operation()
                                        .tag("boilerplate")
                                        .description(roSpec("8.1"))
                                        .produces("application/json")
                                        .produces("application/json;profile=urn:org.restfulobjects:repr-types/version")
                                        .response(200,
                                                newResponse(Caching.NON_EXPIRING)
                                                        .description("OK")
                                                        .schema(new RefProperty("#/definitions/version"))
                                        )));
    }

    void appendBoilerplateDefinitions(final Swagger swagger) {
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
    }

    void appendLinkDefinitions(final Swagger swagger) {

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
    }

    ObjectProperty appendServicePath(final Swagger swagger, final ObjectSpecification objectSpec) {

        final String serviceId = serviceIdFor(objectSpec);

        final Path path = new Path();
        swagger.path(String.format("/services/%s", serviceId), path);

        path.get(
                new Operation()
                        .tag(serviceId)
                        .description(roSpec("15.1"))
                        .produces("application/json")
                        .produces("application/json;profile=urn:org.restfulobjects:repr-types/object")
                        .response(200,
                                newResponse(Caching.TRANSACTIONAL)
                                        .description("OK")
                                        .schema(new RefProperty("#/definitions/service-" + serviceId)))
        );

        final ObjectProperty serviceMembers = new ObjectProperty();
        final ModelImpl serviceRepr =
                newModel(roSpec("15.1.2") + ": representation of " + serviceId)
                        .property("title", stringProperty())
                        .property("serviceId", stringProperty()._default(serviceId))
                        .property("members", serviceMembers);

        swagger.addDefinition("service-" + serviceId, serviceRepr);
        return serviceMembers;
    }

    ObjectProperty appendObjectPath(final Swagger swagger, final ObjectSpecification objectSpec) {

        final String objectType = objectTypeFor(objectSpec);

        final Path path = new Path();
        swagger.path(String.format("/objects/%s/{objectId}", objectType), path);

        path.get(
                new Operation()
                        .tag(objectType)
                        .description(roSpec("14.1"))
                        .parameter(
                                new PathParameter()
                                        .name("objectId")
                                        .type("string"))
                        .produces("application/json")
                        .produces("application/json;profile=urn:org.restfulobjects:repr-types/object")
                        .produces("application/json;profile=urn:org.apache.isis/v1")
                        .produces("application/json;profile=urn:org.apache.isis/v1;suppress=true")
                        .response(200,
                                newResponse(Caching.TRANSACTIONAL)
                                        .description("OK")
                                        .schema(new RefProperty("#/definitions/object-" + objectType)))
        );

        final ObjectProperty serviceMembers = new ObjectProperty();
        final ModelImpl serviceRepr =
                newModel(roSpec("14.4") + ": representation of " + objectType)
                        .property("title", stringProperty())
                        .property("domainType", stringProperty()._default(objectType))
                        .property("instanceId", stringProperty())
                        .property("members", serviceMembers);

        swagger.addDefinition("object-" + objectType, serviceRepr);
        return serviceMembers;
    }

    private String objectTypeFor(final ObjectSpecification objectSpec) {
        return objectSpec.getFacet(ObjectSpecIdFacet.class).value().asString();
    }

    void appendActionTo(final ObjectProperty serviceMembers, final ObjectAction serviceAction) {
        String serviceActionId = serviceAction.getId();

        // within the services representation itself
        serviceMembers.property(serviceActionId,
                new ObjectProperty()
                        .property("id", stringPropertyEnum(serviceActionId))
                        .property("memberType", stringPropertyEnum("action"))
                        .property("links",
                                new ObjectProperty()
                                        .property("rel", stringPropertyEnum( String.format(
                                                "urn:org.restfulobjects:rels/details;action=%s", serviceActionId)))
                                        .property("href", stringPropertyEnum(String.format(
                                                "actions/%s", serviceActionId))))
                        .property("method", stringPropertyEnum("GET"))
                        .property("type", stringPropertyEnum(
                                "application/json;profile=urn:org.restfulobjects:repr-types/object-action"))
        );
    }

    void appendServiceActionInvokePath(
            final Swagger swagger,
            final ObjectAction serviceAction) {

        final ObjectSpecification serviceSpec = serviceAction.getOnType();
        final String serviceId = serviceIdFor(serviceSpec);
        final String actionId = serviceAction.getId();

        final List<ObjectActionParameter> parameters = serviceAction.getParameters();
        final Path path = new Path();
        swagger.path(String.format("/services/%s/actions/%s/invoke", serviceId, actionId), path);

        final Operation invokeOperation =
                new Operation()
                        .tag(serviceId)
                        .description(roSpec("19.1") + ": (invoke) resource of " + serviceId + "#" + actionId)
                        .produces("application/json")
                        .produces("application/json;profile=urn:org.restfulobjects:repr-types/action-result")
                        .produces("application/json;profile=urn:org.apache.isis/v1")
                        .produces("application/json;profile=urn:org.apache.isis/v1;suppress=true");

        final ActionSemantics.Of semantics = serviceAction.getSemantics();
        if(semantics.isSafeInNature()) {
            path.get(invokeOperation);

            for (final ObjectActionParameter parameter : parameters) {
                invokeOperation
                        .parameter(
                                new QueryParameter()
                                        .name(parameter.getId())
                                        .description(roSpec("2.9.1") + (!Strings.isNullOrEmpty(parameter.getDescription())? (": " + parameter.getDescription()) : ""))
                                        .required(false)
                                        .type("string")
                        );
            }
            if(!parameters.isEmpty()) {
                invokeOperation.parameter(new QueryParameter()
                        .name("x-isis-querystring")
                        .description(roSpec("2.10") + ": all (formal) arguments as base64 encoded string")
                        .required(false)
                        .type("string"));
            }

        } else {
            if (semantics.isIdempotentInNature()) {
                path.put(invokeOperation);
            } else {
                path.post(invokeOperation);
            }

            final ModelImpl bodyParam =
                    new ModelImpl()
                            .type("object");
            for (final ObjectActionParameter parameter : parameters) {

                final Property valueProperty;
                // TODO: need to switch on parameter's type and create appropriate impl of valueProperty
                // if(parameter.getSpecification().isValue()) ...
                valueProperty = stringProperty();

                bodyParam
                        .property(parameter.getId(),
                                new ObjectProperty()
                                        .property("value", valueProperty)
                        );
            }

            invokeOperation
                    .consumes("application/json")
                    .parameter(
                            new BodyParameter()
                                    .name("body")
                                    .schema(bodyParam));

        }

        invokeOperation
                .response(
                        200, new Response()
                                .description(roSpecForResponseOf(serviceAction) + ": (invoke) representation of " + serviceId + "#" + actionId)
                                .schema(new ObjectProperty())
                );
    }

    void appendObjectActionInvokePath(
            final Swagger swagger,
            final ObjectAction objectAction) {

        final ObjectSpecification objectSpec = objectAction.getOnType();
        final String objectType = objectTypeFor(objectSpec);
        final String actionId = objectAction.getId();

        final List<ObjectActionParameter> parameters = objectAction.getParameters();
        final Path path = new Path();
        swagger.path(String.format("/objects/%s/{objectId}/actions/%s/invoke", objectType, actionId), path);

        final Operation invokeOperation =
                new Operation()
                        .tag(objectType)
                        .description(roSpec("19.1") + ": (invoke) resource of " + objectType + "#" + actionId)
                        .parameter(
                                new PathParameter()
                                        .name("objectId")
                                        .type("string"))
                        .produces("application/json")
                        .produces("application/json;profile=urn:org.restfulobjects:repr-types/action-result")
                        .produces("application/json;profile=urn:org.apache.isis/v1")
                        .produces("application/json;profile=urn:org.apache.isis/v1;suppress=true");

        final ActionSemantics.Of semantics = objectAction.getSemantics();
        if(semantics.isSafeInNature()) {
            path.get(invokeOperation);

            for (final ObjectActionParameter parameter : parameters) {
                invokeOperation
                        .parameter(
                                new QueryParameter()
                                        .name(parameter.getId())
                                        .description(roSpec("2.9.1") + (!Strings.isNullOrEmpty(parameter.getDescription())? (": " + parameter.getDescription()) : ""))
                                        .required(false)
                                        .type("string")
                        );
            }
            if(!parameters.isEmpty()) {
                invokeOperation.parameter(new QueryParameter()
                        .name("x-isis-querystring")
                        .description(roSpec("2.10") + ": all (formal) arguments as base64 encoded string")
                        .required(false)
                        .type("string"));
            }

        } else {
            if (semantics.isIdempotentInNature()) {
                path.put(invokeOperation);
            } else {
                path.post(invokeOperation);
            }

            final ModelImpl bodyParam =
                    new ModelImpl()
                            .type("object");
            for (final ObjectActionParameter parameter : parameters) {

                final Property valueProperty;
                // TODO: need to switch on parameter's type and create appropriate impl of valueProperty
                // if(parameter.getSpecification().isValue()) ...
                valueProperty = stringProperty();

                bodyParam
                        .property(parameter.getId(),
                                new ObjectProperty()
                                        .property("value", valueProperty)
                        );
            }

            invokeOperation
                    .consumes("application/json")
                    .parameter(
                            new BodyParameter()
                                    .name("body")
                                    .schema(bodyParam));

        }

        invokeOperation
                .response(
                        200, new Response()
                                .description(roSpecForResponseOf(objectAction) + ": (invoke) representation of " + objectType + "#" + actionId)
                                .schema(new ObjectProperty())
                );
    }


    private String roSpecForResponseOf(final ObjectAction action) {
        final ActionSemantics.Of semantics = action.getSemantics();
        switch (semantics) {
            case SAFE_AND_REQUEST_CACHEABLE:
            case SAFE:
                return "19.2";
            case IDEMPOTENT:
            case IDEMPOTENT_ARE_YOU_SURE:
                return "19.3";
            default:
                return "19.4";
        }
    }

    private boolean isVisibleForPublic(final ObjectAction objectAction) {
        final ObjectSpecification specification = objectAction.getReturnType();
        boolean visible = isVisibleForPublic(specification);
        if(!visible) {
            return false;
        }
        List<ObjectSpecification> parameterTypes = objectAction.getParameterTypes();
        for (ObjectSpecification parameterType : parameterTypes) {
            boolean paramVisible = isVisibleForPublic(parameterType);
            if(!paramVisible) {
                return false;
            }
        }
        return true;
    }

    private boolean isVisibleForPublic(final ObjectSpecification specification) {
        if (specification == null) {
            return true;
        }
        if(specification.isViewModel()) {
            return true;
        }
        if(specification.isValue()) {
            return true;
        }
        if(specification.isParentedOrFreeCollection()) {
            TypeOfFacet typeOfFacet = specification.getFacet(TypeOfFacet.class);
            ObjectSpecification elementSpec = typeOfFacet.valueSpec();
            return isVisibleForPublic(elementSpec);
        }
        final Class<?> correspondingClass = specification.getCorrespondingClass();
        return correspondingClass == void.class || correspondingClass == Void.class;
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

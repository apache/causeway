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
import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

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

    public String generate(
            final String basePath,
            final SwaggerService.Visibility visibility,
            final SwaggerService.Format format) {

        final Swagger swagger = new Swagger();
        swagger.basePath(basePath);
        swagger.info(new Info()
                        //.version("1.0.0") // TODO: provide some way of passing the name, version etc (some sort of SPI service?)
                        .title(visibility.name() + " API")
        );

        appendRestfulObjectsSupportingPathsAndDefinitions(swagger);
        appendLinkModelDefinition(swagger);

        appendServicePathsAndDefinitions(swagger, visibility);
        appendObjectPathsAndDefinitions(swagger, visibility);

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

    void appendServicePathsAndDefinitions(final Swagger swagger, final SwaggerService.Visibility visibility) {
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

            final List<ObjectAction> serviceActions = Util.actionsOf(serviceSpec, visibility);
            if(serviceActions.isEmpty()) {
                continue;
            }
            appendServicePath(swagger, serviceSpec);

            for (final ObjectAction serviceAction : serviceActions) {
                appendServiceActionInvokePath(swagger, serviceAction);
            }
        }
    }

    void appendObjectPathsAndDefinitions(final Swagger swagger, final SwaggerService.Visibility visibility) {
        final Collection<ObjectSpecification> allSpecs = specificationLoader.allSpecifications();
        for (final ObjectSpecification objectSpec : allSpecs) {

            final DomainServiceFacet domainServiceFacet = objectSpec.getFacet(DomainServiceFacet.class);
            if (domainServiceFacet != null) {
                continue;
            }
            final MixinFacet mixinFacet = objectSpec.getFacet(MixinFacet.class);
            if (mixinFacet != null) {
                continue;
            }
            if(visibility.isPublic() && !Util.isVisibleForPublic(objectSpec)) {
                continue;
            }
            if(objectSpec.isAbstract()) {
                continue;
            }
            if(objectSpec.isValue()) {
                continue;
            }

            final List<OneToOneAssociation> objectProperties = Util.propertiesOf(objectSpec, visibility);
            final List<OneToManyAssociation> objectCollections = Util.collectionsOf(objectSpec, visibility);
            final List<ObjectAction> objectActions = Util.actionsOf(objectSpec, visibility);

            if(objectProperties.isEmpty() && objectCollections.isEmpty()) {
                continue;
            }
            final ModelImpl isisModel = appendObjectPathAndModelDefinitions(swagger, objectSpec);
            appendObjectModel(isisModel, objectProperties, objectCollections);

            for (final OneToManyAssociation objectCollection : objectCollections) {
                appendCollectionTo(swagger, objectCollection);
            }

            for (final ObjectAction objectAction : objectActions) {
                appendObjectActionInvokePath(swagger, objectAction);
            }
        }
    }

    void appendRestfulObjectsSupportingPathsAndDefinitions(final Swagger swagger) {

        final String tag = "> restful objects supporting resources";

        swagger.path("/",
                new Path()
                        .get(new Operation()
                                .tag(tag)
                                .description(Util.roSpec("5.1"))
                                .produces("application/json")
                                .produces("application/json;profile=urn:org.restfulobjects:repr-types/home-page")
                                .response(200,
                                        newResponse(Caching.NON_EXPIRING)
                                                .description("OK")
                                                .schema(new RefProperty("#/definitions/RestfulObjectsSupportingHomePageModel"))
                                )));
        swagger.addDefinition("RestfulObjectsSupportingHomePageModel",
                newModel(Util.roSpec("5.2")));

        swagger.path("/user",
                    new Path()
                            .get(new Operation()
                                    .tag(tag)
                                    .description(Util.roSpec("6.1"))
                                    .produces("application/json")
                                    .produces("application/json;profile=urn:org.restfulobjects:repr-types/user")
                                    .response(200,
                                            newResponse(Caching.USER_INFO)
                                                    .description("OK")
                                                    .schema(new RefProperty("#/definitions/RestfulObjectsSupportingUserModel"))
                                    )));
        swagger.addDefinition("RestfulObjectsSupportingUserModel",
                newModel(Util.roSpec("6.2"))
                        .property("userName", stringProperty())
                        .property("roles", arrayOfStrings())
                        .property("links", arrayOfLinks())
                        .required("userName")
                        .required("roles"));

        swagger.path("/services",
                        new Path()
                                .get(new Operation()
                                        .tag(tag)
                                        .description(Util.roSpec("7.1"))
                                        .produces("application/json")
                                        .produces("application/json;profile=urn:org.restfulobjects:repr-types/services")
                                        .response(200,
                                                newResponse(Caching.USER_INFO)
                                                        .description("OK")
                                                        .schema(new RefProperty("#/definitions/RestfulObjectsSupportingServicesModel"))
                                        )));
        swagger.addDefinition("RestfulObjectsSupportingServicesModel",
                newModel(Util.roSpec("7.2"))
                        .property("value", arrayOfLinks())
                        .required("userName")
                        .required("roles"));

        swagger.path("/version",
                        new Path()
                                .get(new Operation()
                                        .tag(tag)
                                        .description(Util.roSpec("8.1"))
                                        .produces("application/json")
                                        .produces("application/json;profile=urn:org.restfulobjects:repr-types/RestfulObjectsSupportingServicesModel")
                                        .response(200,
                                                newResponse(Caching.NON_EXPIRING)
                                                        .description("OK")
                                                        .schema(new ObjectProperty())
                                        )));
        swagger.addDefinition("RestfulObjectsSupportingServicesModel",
                newModel(Util.roSpec("8.2"))
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
                        .required("roles"));
    }

    void appendLinkModelDefinition(final Swagger swagger) {
        swagger.addDefinition("LinkModel",
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

    }

    void appendServicePath(final Swagger swagger, final ObjectSpecification objectSpec) {

        final String serviceId = serviceIdFor(objectSpec);

        final Path path = new Path();
        swagger.path(String.format("/services/%s", serviceId), path);

        final String serviceModelDefinition = serviceId + "Model";
        path.get(
                new Operation()
                        .tag(serviceId)
                        .description(Util.roSpec("15.1"))
                        .produces("application/json")
                        .produces("application/json;profile=urn:org.restfulobjects:repr-types/object")
                        .response(200,
                                newResponse(Caching.TRANSACTIONAL)
                                        .description("OK")
                                        .schema(new RefProperty("#/definitions/" + serviceModelDefinition)))
        );

        final ModelImpl model =
                newModel(Util.roSpec("15.1.2") + ": representation of " + serviceId)
                        .property("title", stringProperty())
                        .property("serviceId", stringProperty()._default(serviceId))
                        .property("members", new ObjectProperty());

        swagger.addDefinition(serviceModelDefinition, model);
    }

    ModelImpl appendObjectPathAndModelDefinitions(final Swagger swagger, final ObjectSpecification objectSpec) {

        final String objectType = objectTypeFor(objectSpec);

        final Path path = new Path();
        swagger.path(String.format("/objects/%s/{objectId}", objectType), path);

        final String isisModelDefinition = objectType + "Model";
        final String restfulObjectsModelDefinition = objectType + "RestfulObjectsModel";
        final String tag = objectType.startsWith("org.apache.isis")? "> apache isis internals": objectType;
        final Operation operation = new Operation();
        path.get(
                operation);
        operation
                .tag(tag)
                .description(Util.roSpec("14.1"))
                .parameter(
                    new PathParameter()
                        .name("objectId")
                        .type("string"))
                .produces("application/json;profile=urn:org.apache.isis/v1")
                .produces("application/json;profile=urn:org.apache.isis/v1;suppress=true")
                .produces("application/json;profile=urn:org.restfulobjects:repr-types/object");


        // per https://github.com/swagger-api/swagger-spec/issues/146, swagger 2.0 doesn't support multiple
        // modelled representations per path and response code;
        // in particular cannot associate representation/model with Accept header ('produces(...) method)
        if (false) {
            operation.response(200,
                    newResponse(Caching.TRANSACTIONAL)
                            .description("if Accept: application/json;profile=urn:org.restfulobjects:repr-types/object")
                            .schema(new RefProperty("#/definitions/" + restfulObjectsModelDefinition)));

            final ModelImpl roSpecModel =
                    newModel(Util.roSpec("14.4") + ": representation of " + objectType)
                            .property("title", stringProperty())
                            .property("domainType", stringProperty()._default(objectType))
                            .property("instanceId", stringProperty())
                            .property("members", new ObjectProperty());
            swagger.addDefinition(restfulObjectsModelDefinition, roSpecModel);
        }

        operation
                .response(200,
                        newResponse(Caching.TRANSACTIONAL)
                                .description("if Accept: application/json;profile=urn:org.apache.isis/v1")
                                .schema(new RefProperty("#/definitions/"
                                        + isisModelDefinition)));


        final ModelImpl isisModel = new ModelImpl();
        swagger.addDefinition(isisModelDefinition, isisModel);

        // return so can be appended to
        return isisModel;
    }

    // UNUSED
    void appendServiceActionPromptTo(final ObjectProperty serviceMembers, final ObjectAction action) {
        String actionId = action.getId();

        serviceMembers.property(actionId,
                new ObjectProperty()
                        .property("id", stringPropertyEnum(actionId))
                        .property("memberType", stringPropertyEnum("action"))
                        .property("links",
                                new ObjectProperty()
                                        .property("rel", stringPropertyEnum( String.format(
                                                "urn:org.restfulobjects:rels/details;action=%s", actionId)))
                                        .property("href", stringPropertyEnum(String.format(
                                                "actions/%s", actionId))))
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
                        .description(Util.roSpec("19.1") + ": (invoke) resource of " + serviceId + "#" + actionId)
                        .produces("application/json;profile=urn:org.apache.isis/v1")
                        .produces("application/json;profile=urn:org.apache.isis/v1;suppress=true")
                        .produces("application/json;profile=urn:org.restfulobjects:repr-types/action-result")
                ;

        final ActionSemantics.Of semantics = serviceAction.getSemantics();
        if(semantics.isSafeInNature()) {
            path.get(invokeOperation);

            for (final ObjectActionParameter parameter : parameters) {
                invokeOperation
                        .parameter(
                                new QueryParameter()
                                        .name(parameter.getId())
                                        .description(Util.roSpec("2.9.1") + (!Strings.isNullOrEmpty(parameter.getDescription())? (": " + parameter.getDescription()) : ""))
                                        .required(false)
                                        .type("string")
                        );
            }
            if(!parameters.isEmpty()) {
                invokeOperation.parameter(new QueryParameter()
                        .name("x-isis-querystring")
                        .description(Util.roSpec("2.10") + ": all (formal) arguments as base64 encoded string")
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
                        .description(Util.roSpec("19.1") + ": (invoke) resource of " + objectType + "#" + actionId)
                        .parameter(
                                new PathParameter()
                                        .name("objectId")
                                        .type("string"))
                        .produces("application/json;profile=urn:org.apache.isis/v1")
                        .produces("application/json;profile=urn:org.apache.isis/v1;suppress=true")
                        .produces("application/json;profile=urn:org.restfulobjects:repr-types/action-result");

        final ActionSemantics.Of semantics = objectAction.getSemantics();
        if(semantics.isSafeInNature()) {
            path.get(invokeOperation);

            for (final ObjectActionParameter parameter : parameters) {
                invokeOperation
                        .parameter(
                                new QueryParameter()
                                        .name(parameter.getId())
                                        .description(Util.roSpec("2.9.1") + (!Strings.isNullOrEmpty(parameter.getDescription())? (": " + parameter.getDescription()) : ""))
                                        .required(false)
                                        .type("string")
                        );
            }
            if(!parameters.isEmpty()) {
                invokeOperation.parameter(new QueryParameter()
                        .name("x-isis-querystring")
                        .description(Util.roSpec("2.10") + ": all (formal) arguments as base64 encoded string")
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

    void appendCollectionTo(final Swagger members, final OneToManyAssociation serviceAction) {

    }

    void appendObjectModel(
            final ModelImpl swagger,
            final List<OneToOneAssociation> objectProperties, final List<OneToManyAssociation> objectCollections) {

    }

    static String roSpecForResponseOf(final ObjectAction action) {
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

    static ModelImpl newModel(final String description) {
        return new ModelImpl()
                .description(description)
                .type("object")
                .property("links", arrayOfLinks())
                .property("extensions", new MapProperty())
                .required("links")
                .required("extensions");
    }

    // TODO: this is horrid, there ought to be a facet we can call instead...
    static String serviceIdFor(final ObjectSpecification serviceSpec) {
        Object tempServiceInstance = InstanceUtil.createInstance(serviceSpec.getCorrespondingClass());
        return ServiceUtil.id(tempServiceInstance);
    }

    static String objectTypeFor(final ObjectSpecification objectSpec) {
        return objectSpec.getFacet(ObjectSpecIdFacet.class).value().asString();
    }

    static StringProperty stringProperty() {
        return new StringProperty();
    }

    static StringProperty stringPropertyEnum(String... enumValues) {
        StringProperty stringProperty = stringProperty();
        stringProperty._enum(Arrays.asList(enumValues));
        if(enumValues.length >= 1) {
            stringProperty._default(enumValues[0]);
        }
        return stringProperty;
    }

    static ArrayProperty arrayOfLinks() {
        return new ArrayProperty()
                .items(new RefProperty("#/definitions/LinkModel"));
    }

    static ArrayProperty arrayOfStrings() {
        return new ArrayProperty().items(stringProperty());
    }

    static Response newResponse(final Caching caching) {
        return Util.withCachingHeaders(new Response(), caching);
    }

}

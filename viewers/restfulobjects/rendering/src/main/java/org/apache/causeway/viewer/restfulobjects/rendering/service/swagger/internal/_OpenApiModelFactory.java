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
package org.apache.causeway.viewer.restfulobjects.rendering.service.swagger.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.swagger.Visibility;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.util.Facets;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import lombok.val;

class _OpenApiModelFactory {

    // double quotes
    private static final String DQ = ""; // empty seems the only variant that works

    private final String basePath;
    private final Visibility visibility;
    private final SpecificationLoader specificationLoader;

    private final ValueSchemaFactory valueSchemaFactory;
    private final Tagger tagger;
    private final ClassExcluder classExcluder;

    private final Set<String> references = _Sets.newLinkedHashSet();
    private final Set<String> definitions = _Sets.newLinkedHashSet();
    private OpenAPI oa3;

    public _OpenApiModelFactory(
            final String basePath,
            final Visibility visibility,
            final SpecificationLoader specificationLoader,
            final Tagger tagger,
            final ClassExcluder classExcluder,
            final ValueSchemaFactory valuePropertyFactory) {
        this.basePath = basePath;
        this.visibility = visibility;
        this.specificationLoader = specificationLoader;
        this.tagger = tagger;
        this.classExcluder = classExcluder;
        this.valueSchemaFactory = valuePropertyFactory;
    }

    OpenAPI generate() {
        this.oa3 = new OpenAPI();

        final String swaggerVersionInfo =
                String.format("swagger.io (%s)",
                        OpenAPI.class.getPackage().getImplementationVersion()
                        );

        oa3.addServersItem(new Server()
                .url(basePath));
        oa3.info(new Info()
                .version(swaggerVersionInfo)
                .title(visibility.name() + " API")
                );
        oa3.setComponents(new Components());

        appendRestfulObjectsSupportingPathsAndDefinitions();
        appendLinkModelDefinition();
        appendServicePathsAndDefinitions();
        appendObjectPathsAndDefinitions();
        appendDefinitionsForOrphanedReferences();

        oa3.setPaths(sorted(oa3.getPaths()));

        // https://stackoverflow.com/questions/45199989/how-do-i-automatically-authorize-all-endpoints-with-swagger-ui
        oa3.getComponents().addSecuritySchemes("basicAuth", new SecurityScheme().type(Type.HTTP).scheme("basic"));

        return oa3;
    }

    private Paths sorted(final Map<String, PathItem> paths) {

        final List<Map.Entry<String, PathItem>> entries = new ArrayList<>(paths.entrySet());
        entries.sort(new Comparator<Map.Entry<String, PathItem>>() {
            @Override
            public int compare(final Map.Entry<String, PathItem> o1, final Map.Entry<String, PathItem> o2) {
                final String tag1 = tagFor(o1);
                final String tag2 = tagFor(o2);
                final int tag = tag1.compareTo(tag2);
                return tag != 0 ? tag : o1.getKey().compareTo(o2.getKey());
            }

            protected String tagFor(final Map.Entry<String, PathItem> o1) {
                return o1.getValue().readOperations().stream()
                        .findFirst()
                        .map(operation -> operation.getTags().stream().findFirst().orElse("(no tag)")).orElse("(no tag)");
            }
        });

        final LinkedHashMap<String, PathItem> sorted = new LinkedHashMap<>();
        entries.forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));

        val _paths = new Paths();
        _paths.putAll(sorted);
        return _paths;
    }

    void appendServicePathsAndDefinitions() {

        for (val spec : specificationLoader.snapshotSpecifications()) {

            if(!DomainServiceFacet.isContributing(spec)) {
                continue;
            }

            val serviceActions = _Util.actionsOf(spec, visibility, classExcluder);
            if(serviceActions.isEmpty()) {
                continue;
            }

            appendServicePath(spec);

            for (val serviceAction : serviceActions) {
                appendServiceActionInvokePath(spec, serviceAction);
            }
        }
    }

    @SuppressWarnings("unused")
    private void debugTraverseAllSpecs(final Collection<ObjectSpecification> allSpecs) {
        for (final ObjectSpecification objectSpec :  allSpecs) {
            objectSpec.streamAssociations(MixedIn.INCLUDED)
            .collect(Collectors.toList());
            objectSpec.streamAnyActions(MixedIn.INCLUDED)
            .collect(Collectors.toList());
        }
    }


    void appendObjectPathsAndDefinitions() {
        // (previously we took a protective copy to avoid a concurrent modification exception,
        // but this is now done by SpecificationLoader itself)
        for (final ObjectSpecification objectSpec : specificationLoader.snapshotSpecifications()) {

            if(Facets.domainServiceIsPresent(objectSpec)
                    || Facets.mixinIsPresent(objectSpec)) {
                continue;
            }
            if(visibility.isPublic()
                    && !_Util.isVisibleForPublic(objectSpec)) {
                continue;
            }
            if(objectSpec.isAbstract()) {
                continue;
            }
            if(objectSpec.isValue()) {
                continue;
            }
            // special cases
            if(classExcluder.exclude(objectSpec)) {
                continue;
            }

            final List<OneToOneAssociation> objectProperties = _Util.propertiesOf(objectSpec, visibility);
            final List<OneToManyAssociation> objectCollections = _Util.collectionsOf(objectSpec, visibility);
            final List<ObjectAction> objectActions = _Util.actionsOf(objectSpec, visibility, classExcluder);

            if(objectProperties.isEmpty()
                    && objectCollections.isEmpty()) {
                continue;
            }
            val causewayModel = appendObjectPathAndModelDefinitions(objectSpec);
            updateObjectModel(causewayModel, objectSpec, objectProperties, objectCollections);

            for (final OneToManyAssociation objectCollection : objectCollections) {
                appendCollectionTo(objectSpec, objectCollection);
            }

            for (final ObjectAction objectAction : objectActions) {
                appendObjectActionInvokePath(objectSpec, objectAction);
            }
        }
    }

    void appendRestfulObjectsSupportingPathsAndDefinitions() {

        final String tag = ". restful objects supporting resources";

        oa3.path("/",
                new PathItem()
                .get(
                        newOperation("home-page",
                                Caching.NON_EXPIRING,
                                newRefProperty("RestfulObjectsSupportingHomePageRepr"),
                                response->response.description("OK"))
                            .addTagsItem(tag)
                            .description(RoSpec.HOMEPAGE_GET.fqSection())));
        addDefinition("RestfulObjectsSupportingHomePageRepr", newModel(RoSpec.HOMEPAGE_REPR.fqSection()));

        oa3.path("/user",
                new PathItem()
                .get(
                        newOperation("user",
                                Caching.USER_INFO,
                                newRefProperty("RestfulObjectsSupportingUserRepr"),
                                response->response.description("OK"))
                            .addTagsItem(tag)
                            .description(RoSpec.USER_GET.fqSection())));
        addDefinition("RestfulObjectsSupportingUserRepr",
                newModel(RoSpec.USER_REPR.fqSection())
                .addProperty("userName", stringProperty())
                .addProperty("roles", arrayOfStrings())
                .addProperty("links", arrayOfLinks())
                .addRequiredItem("userName")
                .addRequiredItem("roles"));

        oa3.path("/services",
                new PathItem()
                .get(
                        newOperation("services",
                                Caching.USER_INFO,
                                newRefProperty("RestfulObjectsSupportingServicesRepr"),
                                response->response.description("OK"))
                            .addTagsItem(tag)
                            .description(RoSpec.DOMAIN_SERVICES_GET.fqSection())));
        addDefinition("RestfulObjectsSupportingServicesRepr",
                newModel(RoSpec.DOMAIN_SERVICES_REPR.fqSection())
                .addProperty("value", arrayOfLinks())
                .addRequiredItem("userName")
                .addRequiredItem("roles"));

        oa3.path("/version",
                new PathItem()
                .get(
                        newOperation("RestfulObjectsSupportingServicesRepr",
                                Caching.NON_EXPIRING,
                                new ObjectSchema(),
                                response->response.description("OK"))
                            .addTagsItem(tag)
                            .description(RoSpec.VERSION_GET.fqSection())));

        oa3.getComponents().addSchemas("RestfulObjectsSupportingServicesRepr",
                newModel(RoSpec.VERSION_REPR.fqSection())
                .addProperty("specVersion", stringProperty())
                .addProperty("implVersion", stringProperty())
                .addProperty("optionalCapabilities",
                        new ObjectSchema()
                        .addProperty("blobsClobs", stringProperty())
                        .addProperty("deleteObjects", stringProperty())
                        .addProperty("domainModel", stringProperty())
                        .addProperty("validateOnly", stringProperty())
                        .addProperty("protoPersistentObjects", stringProperty())
                        )
                .addRequiredItem("userName")
                .addRequiredItem("roles"));
    }

    void appendLinkModelDefinition() {
        oa3.getComponents().addSchemas("LinkRepr",
                new ObjectSchema()
                .addProperty("rel", stringProperty().description("the relationship of the resource to this referencing resource"))
                .addProperty("href", stringProperty().description("the hyperlink reference (URL) of the resource"))
                .addProperty("title", stringProperty().description("title to render"))
                .addProperty("method", stringPropertyEnum("GET", "POST", "PUT", "DELETE").description("HTTP verb to access"))
                .addProperty("type", stringProperty().description("Content-Type recognized by the resource (for HTTP Accept header)"))
                .addProperty("arguments", new ObjectSchema().description("Any arguments, to send as query strings or in body"))
                .addProperty("value", stringProperty().description("the representation of the link if followed"))
                .addRequiredItem("rel")
                .addRequiredItem("href")
                .addRequiredItem("method")
                );

        oa3.getComponents().addSchemas("HrefRepr",
                new ObjectSchema()
                .description("Abbreviated version of the Link resource, used primarily to reference non-value objects")
                .addProperty("href", stringProperty().description("the hyperlink reference (URL) of the resource"))
                .addRequiredItem("href")
                );

    }

    void appendServicePath(final ObjectSpecification objectSpec) {

        final String serviceId = objectSpec.getLogicalTypeName();

        final PathItem path = new PathItem();
        oa3.path(String.format("/services/%s", serviceId), path);

        final String serviceModelDefinition = serviceId + "Repr";

        final String tag = tagForlogicalTypeName(serviceId, "> services");
        path.get(
                newOperation("object",
                        Caching.TRANSACTIONAL,
                        newRefProperty(serviceModelDefinition),
                        response->response.description("OK"))
                    .addTagsItem(tag)
                    .description(RoSpec.DOMAIN_SERVICE_GET.fqSection()));

        val model =
                newModel(RoSpec.DOMAIN_SERVICE_GET_SUCCESS.fqSection("representation of " + serviceId))
                .addProperty("title", stringProperty())
                .addProperty("serviceId", stringProperty()._default(serviceId))
                .addProperty("members", new ObjectSchema());

        addDefinition(serviceModelDefinition, model);
    }

    ObjectSchema appendObjectPathAndModelDefinitions(final ObjectSpecification objectSpec) {

        final String logicalTypeName = objectSpec.getLogicalTypeName();

        val causewayModel = new ObjectSchema();
        val causewayModelDefinition = logicalTypeName + "Repr";
        addDefinition(causewayModelDefinition, causewayModel);

        final PathItem path = new PathItem();
        oa3.path(String.format("/objects/%s/{objectId}", logicalTypeName), path);

        final String tag = tagForlogicalTypeName(logicalTypeName, null);
        final Operation operation = newOperation("object",
                Caching.TRANSACTIONAL,
                newRefProperty(causewayModelDefinition),
                response->response.description(logicalTypeName
                        + " , if Accept: application/json;profile=urn:org.apache.causeway/v2")
                );
        path.get(operation);
        operation
        .addTagsItem(tag)
        .description(RoSpec.DOMAIN_OBJECT_GET.fqSection())
        .addParametersItem(
                _OpenApi.pathParameter()
                .name("objectId"));

        // per https://github.com/swagger-api/swagger-spec/issues/146, swagger 2.0 doesn't support multiple
        // modeled representations per path and response code;
        // in particular cannot associate representation/model with Accept header ('produces(...) method)
//        final String restfulObjectsModelDefinition = logicalTypeName + "RestfulObjectsRepr";
//        if (false) {
//            _OpenApi.response(operation,
//                    200,
//                    newResponse(Caching.TRANSACTIONAL, newRefProperty(restfulObjectsModelDefinition))
//                    .description("if Accept: application/json;profile=urn:org.restfulobjects:repr-types/object"));
//
//            final Schema roSpecModel =
//                    newModel(_Util.roSpec("14.4") + ": representation of " + logicalTypeName)
//                    .addProperty("title", stringProperty())
//                    .addProperty("domainType", stringProperty()._default(logicalTypeName))
//                    .addProperty("instanceId", stringProperty())
//                    .addProperty("members", new ObjectSchema());
//            swagger.getComponents().addSchemas(restfulObjectsModelDefinition, roSpecModel);
//        }

        // return so can be appended to
        return causewayModel;
    }



    // UNUSED
    void appendServiceActionPromptTo(final ObjectSchema serviceMembers, final ObjectAction action) {
        String actionId = action.getId();

        serviceMembers.addProperty(actionId,
                new ObjectSchema()
                .addProperty("id", stringPropertyEnum(actionId))
                .addProperty("memberType", stringPropertyEnum("action"))
                .addProperty("links",
                        new ObjectSchema()
                        .addProperty("rel", stringPropertyEnum( String.format(
                                "urn:org.restfulobjects:rels/details;action=%s", actionId)))
                        .addProperty("href", stringPropertyEnum(String.format(
                                "actions/%s", actionId))))
                .addProperty("method", stringPropertyEnum("GET"))
                .addProperty("type", stringPropertyEnum(
                        "application/json;profile=urn:org.restfulobjects:repr-types/object-action"))
                );
    }

    void appendServiceActionInvokePath(
            final ObjectSpecification serviceSpec,
            final ObjectAction serviceAction) {

        final String serviceId = serviceSpec.getLogicalTypeName();
        final String actionId = serviceAction.getId();

        val parameters = serviceAction.getParameters();
        final PathItem path = new PathItem();
        oa3.path(String.format("/services/%s/actions/%s/invoke", serviceId, actionId), path);

        final String tag = tagForlogicalTypeName(serviceId, "> services");
        final Operation invokeOperation =
                newOperation(List.of("object", "action-result"),
                        Caching.TRANSACTIONAL,
                        actionReturnTypeFor(serviceAction),
                        response->
                            response.description(serviceId + "#" + actionId
                                    + " , if Accept: application/json;profile=urn:org.apache.causeway/v2")
                )
                .addTagsItem(tag)
                .description(RoSpec.ACTION_INVOKE_GET.fqSection("(invoke) resource of " + serviceId + "#" + actionId));

        final SemanticsOf semantics = serviceAction.getSemantics();
        if(semantics.isSafeInNature()) {
            path.get(invokeOperation);

            for (final ObjectActionParameter parameter : parameters) {

                val describedAs = parameter.getStaticDescription().orElse(null);

                invokeOperation
                .addParametersItem(_OpenApi.queryParameter()
                        .name(parameter.getId())
                        .description(RoSpec.ARGS_SIMPLE.fqSection(describedAs))
                        .required(false));
            }
            if(!parameters.isEmpty()) {
                invokeOperation.addParametersItem(_OpenApi.queryParameter()
                        .name("x-causeway-querystring")
                        .description(RoSpec.ARGS_PASSING.fqSection("all (formal) arguments as base64 encoded string"))
                        .required(false));
            }

        } else {
            if (semantics.isIdempotentInNature()) {
                path.put(invokeOperation);
            } else {
                path.post(invokeOperation);
            }

            val bodyParam = new ObjectSchema();
            for (final ObjectActionParameter parameter : parameters) {

                // TODO: need to switch on parameter's type and create appropriate impl of valueProperty
                // if(parameter.getSpecification().isValue()) ...
                val valueProperty = stringProperty();

                bodyParam
                .addProperty(parameter.getId(),
                        new ObjectSchema()
                        .addProperty("value", valueProperty)
                        );
            }

            invokeOperation
            .requestBody(_OpenApi.requestBody("application/json", bodyParam))
            .addExtension("x-codegen-request-body-name", "body");
        }
    }

    void appendCollectionTo(
            final ObjectSpecification objectSpec,
            final OneToManyAssociation collection) {

        final String logicalTypeName = objectSpec.getLogicalTypeName();
        final String collectionId = collection.getId();

        final PathItem path = new PathItem();
        oa3.path(String.format("/objects/%s/{objectId}/collections/%s", logicalTypeName, collectionId), path);

        final String tag = tagForlogicalTypeName(logicalTypeName, null);
        final Operation collectionOperation =
                newOperation("object-collection",
                        Caching.TRANSACTIONAL,
                        arrayPropertyOf(collection.getElementType()),
                        response->response.description(logicalTypeName + "#" + collectionId
                                + " , if Accept: application/json;profile=urn:org.apache.causeway/v2"))
                .addTagsItem(tag)
                .description(RoSpec.COLLECTION_GET.fqSection("resource of " + logicalTypeName + "#" + collectionId))
                .addParametersItem(
                        _OpenApi.pathParameter()
                        .name("objectId"));

        path.get(collectionOperation);
    }

    void appendObjectActionInvokePath(
            final ObjectSpecification objectSpec,
            final ObjectAction objectAction) {

        final String logicalTypeName = objectSpec.getLogicalTypeName();
        final String actionId = objectAction.getId();

        val parameters = objectAction.getParameters();
        final PathItem path = new PathItem();
        oa3.path(String.format("/objects/%s/{objectId}/actions/%s/invoke", logicalTypeName, actionId), path);

        final String tag = tagForlogicalTypeName(logicalTypeName, null);

        final Operation invokeOperation =
                newOperation("action-result",
                        Caching.TRANSACTIONAL,
                        actionReturnTypeFor(objectAction),
                        response->response.description(logicalTypeName + "#" + actionId))
                .addTagsItem(tag)
                .description(RoSpec.ACTION_INVOKE_GET.fqSection(
                        "(invoke) resource of " + logicalTypeName + "#" + actionId))
                .addParametersItem(
                        _OpenApi.pathParameter()
                        .name("objectId"));

        final SemanticsOf semantics = objectAction.getSemantics();
        if(semantics.isSafeInNature()) {
            path.get(invokeOperation);

            for (final ObjectActionParameter parameter : parameters) {

                val describedAs = parameter.getStaticDescription().orElse(null);

                invokeOperation
                .addParametersItem(
                        _OpenApi.queryParameter()
                        .name(parameter.getId())
                        .description(RoSpec.ARGS_SIMPLE.fqSection(describedAs))
                        .required(false));
            }
            if(!parameters.isEmpty()) {
                invokeOperation.addParametersItem(
                        _OpenApi.queryParameter()
                        .name("x-causeway-querystring")
                        .description(RoSpec.ARGS_PASSING.fqSection("all (formal) arguments as base64 encoded string"))
                        .required(false));
            }

        } else {
            if (semantics.isIdempotentInNature()) {
                path.put(invokeOperation);
            } else {
                path.post(invokeOperation);
            }

            val bodyParam = new ObjectSchema();
            for (final ObjectActionParameter parameter : parameters) {

                final ObjectSpecification specification = parameter.getElementType();
                val valueProperty = specification.isValue()
                        ? schemaFor(specification)
                        : refToLinkModel() ;
                bodyParam
                .addProperty(parameter.getId(),
                        new ObjectSchema()
                        .addProperty("value", valueProperty)
                        );
            }

            invokeOperation
                .requestBody(_OpenApi.requestBody("application/json", bodyParam))
                .addExtension("x-codegen-request-body-name", "body");
        }


    }

    void appendDefinitionsForOrphanedReferences() {
        for (String reference : getReferencesWithoutDefinition()) {
            oa3.getComponents().addSchemas(reference, new Schema<>());
        }
    }

    Schema<?> actionReturnTypeFor(final ObjectAction objectAction) {
        return objectAction.getReturnType().isPlural()
                ? arrayPropertyOf(objectAction.getElementType())
                : schemaFor(objectAction.getReturnType());
    }

    @SuppressWarnings("unchecked")
    private ArraySchema arrayPropertyOf(final ObjectSpecification objectSpecification) {
        final ArraySchema arrayProperty = new ArraySchema();
        if(objectSpecification != null
                && objectSpecification.getCorrespondingClass() != Object.class) {
            arrayProperty
            .description("List of " + objectSpecification.getLogicalTypeName())
            .items(schemaFor(objectSpecification));
        } else {
            arrayProperty.items(new ObjectSchema());
        }
        return arrayProperty;
    }

    private Schema<?> schemaFor(final @Nullable ObjectSpecification specification) {
        val cls = specification!=null
                ? specification.getCorrespondingClass()
                : null;
        if(cls == null
                || void.class.equals(cls)
                || Void.class.equals(cls)
                || java.lang.Object.class.equals(cls)) {
            return new ObjectSchema();
        }
        if(specification.isPlural()) {
            val elementSpec = Facets.elementSpec(specification).orElse(null);
            if(elementSpec != null) {
                return arrayPropertyOf(elementSpec);
            }
        }
        if(cls.isEnum()) {
            return valueSchemaFactory.schemaForValue(specification).orElseThrow();
        }
        if(specification.isValue()) {
            val valueSchema = valueSchemaFactory.schemaForValue(specification);
            if(valueSchema.isPresent()) {
                return valueSchema.get();
            }
        }
        return newRefProperty(specification.getLogicalTypeName() + "Repr");
    }

    private void updateObjectModel(
            final ObjectSchema model,
            final ObjectSpecification objectSpecification,
            final List<OneToOneAssociation> objectProperties,
            final List<OneToManyAssociation> objectCollections) {

        final String logicalTypeName = objectSpecification.getLogicalTypeName();
        final String className = objectSpecification.getFullIdentifier();

        model
        .description(String.format("%s (%s)", logicalTypeName, className));

        for (OneToOneAssociation objectProperty : objectProperties) {
            model.addProperty(
                    objectProperty.getId(),
                    valueSchemaFactory.schemaForValue(objectProperty.getElementType())
                        // else assume this is a reference to an entity/view model, meaning we use an href
                        .orElseGet(()->refToHrefModel()));
        }

        for (OneToManyAssociation objectCollection : objectCollections) {
            final ObjectSpecification elementSpec = objectCollection.getElementType();
            model.addProperty(
                    objectCollection.getId(),
                    arrayPropertyOf(elementSpec)
                    );
        }
    }

    // unused
//    static String roSpecForResponseOf(final ObjectAction action) {
//        final SemanticsOf semantics = action.getSemantics();
//        switch (semantics) {
//        case SAFE_AND_REQUEST_CACHEABLE:
//        case SAFE:
//            return "19.2";
//        case IDEMPOTENT:
//        case IDEMPOTENT_ARE_YOU_SURE:
//            return "19.3";
//        default:
//            return "19.4";
//        }
//    }

    static ObjectSchema newModel(final String description) {
        return (ObjectSchema) new ObjectSchema()
                .description(description)
                .addProperty("links", arrayOfLinks())
                .addProperty("extensions", new MapSchema())
                .addRequiredItem("links")
                .addRequiredItem("extensions");
    }

    static StringSchema stringProperty() {
        return new StringSchema();
    }

    static StringSchema stringPropertyEnum(final String... enumValues) {
        final StringSchema stringProperty = stringProperty();
        stringProperty._enum(Arrays.asList(enumValues));
        if(enumValues.length >= 1) {
            stringProperty._default(enumValues[0]);
        }
        return stringProperty;
    }

    static ArraySchema arrayOfLinks() {
        return new ArraySchema()
                .items(refToLinkModel());
    }

    static Schema<?> refToLinkModel() {
        return _OpenApi.refSchema("LinkRepr");
    }

    static Schema<?> refToHrefModel() {
        return _OpenApi.refSchema("HrefRepr");
    }

    static ArraySchema arrayOfStrings() {
        return new ArraySchema().items(stringProperty());
    }

    String tagForlogicalTypeName(final String logicalTypeName, final String fallback) {
        return tagger.tagForLogicalTypeName(logicalTypeName, fallback);
    }

    private Schema<?> newRefProperty(final String model) {
        addSwaggerReference(model);
        return _OpenApi.refSchema(model);
    }

    private void addDefinition(final String key, final Schema<?> model) {
        addSwaggerDefinition(key);
        oa3.getComponents().addSchemas(key, model);
    }

    void addSwaggerReference(final String model) {
        references.add(model);
    }

    void addSwaggerDefinition(final String model) {
        definitions.add(model);
    }

    Set<String> getReferencesWithoutDefinition() {
        val referencesCopy = _Sets.<String>newLinkedHashSet(references);
        referencesCopy.removeAll(definitions);
        return referencesCopy;
    }

    // -- OPERATION FACTORIES

    private static Operation newOperation(final String reprType,
            final Caching caching,
            final Schema<?> responsRef,
            final Consumer<ApiResponse> responseRefiner) {
        return newOperation(List.of(reprType), caching, responsRef, responseRefiner);
    }

    private static Operation newOperation(final List<String> reprTypes,
            final Caching caching,
            final Schema<?> responsRef,
            final Consumer<ApiResponse> responseRefiner) {
        val operation = _OpenApi.operation(200,
                responsRef,
                supportedFormats(reprTypes),
                response->{
                    _OpenApi.withCacheControl(response, caching);
                    responseRefiner.accept(response);
                });
        return operation;
    }

    // -- CONTENT NEGOTIATION

    private static List<String> supportedFormats(final List<String> reprTypes) {
        val supportedFormats = _Lists.<String>newArrayList();
        supportedFormats.add("application/json");
        val supportsV1 = _Refs.booleanRef(false);
        reprTypes.forEach(reprType->{
            if(reprType.equals("object")
                    || reprType.equals("action-result")) {
                supportsV1.setValue(true);
            }
            supportedFormats.add(
                    "application/json;profile="
                    + DQ + "urn:org.restfulobjects:repr-types/" + reprType + DQ);
        });
        if(supportsV1.isTrue()) {
            supportedFormats.add("application/json;profile="
                    + DQ + "urn:org.apache.causeway/v2" + DQ);
            supportedFormats.add("application/json;profile="
                    + DQ + "urn:org.apache.causeway/v2;suppress=all" + DQ);
        }
        return supportedFormats;
    }

}

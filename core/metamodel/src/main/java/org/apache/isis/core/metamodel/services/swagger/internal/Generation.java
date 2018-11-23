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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

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

class Generation {

    private final String basePath;
    private final SwaggerService.Visibility visibility;
    private final SpecificationLoader specificationLoader;

    private final ValuePropertyFactory valuePropertyFactory;
    private final Tagger tagger;
    private final ClassExcluder classExcluder;

    private final Set<String> references = _Sets.newLinkedHashSet();
    private final Set<String> definitions = _Sets.newLinkedHashSet();
    private Swagger swagger;

    public Generation(
            final String basePath,
            final SwaggerService.Visibility visibility,
            final SpecificationLoader specificationLoader) {
        this.basePath = basePath;
        this.visibility = visibility;
        this.specificationLoader = specificationLoader;

        this.valuePropertyFactory = newValuePropertyFactory();
        this.tagger = newTagger();
        this.classExcluder = newClassExcluder();
    }

    protected ValuePropertyFactory newValuePropertyFactory() {
        return new ValuePropertyFactory();
    }

    protected ClassExcluder newClassExcluder() {
        return new ClassExcluder();
    }

    protected Tagger newTagger() {
        return new Tagger();
    }

    Swagger generate() {
        this.swagger = new Swagger();
        
        final String swaggerVersionInfo = 
                String.format("swagger.io (%s)", 
                        Swagger.class.getPackage().getImplementationVersion()
                        );

        swagger.basePath(basePath);
        swagger.info(new Info()
                .version(swaggerVersionInfo)
                .title(visibility.name() + " API")
                );


        appendRestfulObjectsSupportingPathsAndDefinitions();
        appendLinkModelDefinition();

        appendServicePathsAndDefinitions();
        appendObjectPathsAndDefinitions();

        appendDefinitionsForOrphanedReferences();

        return swagger;
    }

    void appendServicePathsAndDefinitions() {
        // (previously we took a protective copy to avoid a concurrent modification exception,
        // but this is now done by SpecificationLoader itself)
        for (final ObjectSpecification objectSpec : specificationLoader.allSpecifications()) {

            final DomainServiceFacet domainServiceFacet = objectSpec.getFacet(DomainServiceFacet.class);
            if (domainServiceFacet == null) {
                continue;
            }
            if (visibility.isPublic() &&
                    domainServiceFacet.getNatureOfService() != NatureOfService.VIEW_REST_ONLY) {
                continue;
            }
            if (    domainServiceFacet.getNatureOfService() != NatureOfService.VIEW_MENU_ONLY &&
                    domainServiceFacet.getNatureOfService() != NatureOfService.VIEW &&
                    domainServiceFacet.getNatureOfService() != NatureOfService.VIEW_REST_ONLY) {
                continue;
            }

            final List<ObjectAction> serviceActions = Util.actionsOf(objectSpec, visibility, classExcluder);
            if(serviceActions.isEmpty()) {
                continue;
            }
            appendServicePath(objectSpec);

            for (final ObjectAction serviceAction : serviceActions) {
                appendServiceActionInvokePath(objectSpec, serviceAction);
            }
        }
    }

    @SuppressWarnings("unused")
    private void debugTraverseAllSpecs(final Collection<ObjectSpecification> allSpecs) {
        for (final ObjectSpecification objectSpec :  allSpecs) {
            objectSpec.streamAssociations(Contributed.INCLUDED)
                .collect(Collectors.toList());
            objectSpec.streamObjectActions(Contributed.INCLUDED)
                .collect(Collectors.toList());
        }
    }

//    @SuppressWarnings("unused")
//    private void debugAllLoadedClasses(final Collection<ObjectSpecification> allSpecs) {
//        final ImmutableList<String> specs = FluentIterable.from(allSpecs)
//                .transform((final ObjectSpecification objectSpecification)->
//                        objectSpecification.getCorrespondingClass().getName())
//                .toSortedList(Ordering.natural());
//        final String all = Joiner.on(",").join(specs);
//        System .out.println(all);
//    }

    void appendObjectPathsAndDefinitions() {
        // (previously we took a protective copy to avoid a concurrent modification exception,
        // but this is now done by SpecificationLoader itself)
        for (final ObjectSpecification objectSpec : specificationLoader.allSpecifications()) {

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
            // special cases
            if(classExcluder.exclude(objectSpec)) {
                continue;
            }

            final List<OneToOneAssociation> objectProperties = Util.propertiesOf(objectSpec, visibility);
            final List<OneToManyAssociation> objectCollections = Util.collectionsOf(objectSpec, visibility);
            final List<ObjectAction> objectActions = Util.actionsOf(objectSpec, visibility, classExcluder);

            if(objectProperties.isEmpty() && objectCollections.isEmpty()) {
                continue;
            }
            final ModelImpl isisModel = appendObjectPathAndModelDefinitions(objectSpec);
            updateObjectModel(isisModel, objectSpec, objectProperties, objectCollections);

            for (final OneToManyAssociation objectCollection : objectCollections) {
                appendCollectionTo(objectSpec, objectCollection);
            }

            for (final ObjectAction objectAction : objectActions) {
                appendObjectActionInvokePath(objectSpec, objectAction);
            }
        }
    }

    void appendRestfulObjectsSupportingPathsAndDefinitions() {

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
                                .schema(newRefProperty("RestfulObjectsSupportingHomePageRepr"))
                                )));
        addDefinition("RestfulObjectsSupportingHomePageRepr", newModel(Util.roSpec("5.2")));

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
                                .schema(newRefProperty("RestfulObjectsSupportingUserRepr"))
                                )));
        addDefinition("RestfulObjectsSupportingUserRepr",
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
                                .schema(newRefProperty("RestfulObjectsSupportingServicesRepr"))
                                )));
        addDefinition("RestfulObjectsSupportingServicesRepr",
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
                        .produces("application/json;profile=urn:org.restfulobjects:repr-types/RestfulObjectsSupportingServicesRepr")
                        .response(200,
                                newResponse(Caching.NON_EXPIRING)
                                .description("OK")
                                .schema(new ObjectProperty())
                                )));
        swagger.addDefinition("RestfulObjectsSupportingServicesRepr",
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

    void appendLinkModelDefinition() {
        swagger.addDefinition("LinkRepr",
                new ModelImpl()
                .type("object")
                .property("rel", stringProperty().description("the relationship of the resource to this referencing resource"))
                .property("href", stringProperty().description("the hyperlink reference (URL) of the resource"))
                .property("title", stringProperty().description("title to render"))
                .property("method", stringPropertyEnum("GET", "POST", "PUT", "DELETE").description("HTTP verb to access"))
                .property("type", stringProperty().description("Content-Type recognized by the resource (for HTTP Accept header)"))
                .property("arguments", new ObjectProperty().description("Any arguments, to send as query strings or in body"))
                .property("value", stringProperty().description("the representation of the link if followed"))
                .required("rel")
                .required("href")
                .required("method")
                );

        swagger.addDefinition("HrefRepr",
                new ModelImpl()
                .type("object")
                .description("Abbreviated version of the Link resource, used primarily to reference non-value objects")
                .property("href", stringProperty().description("the hyperlink reference (URL) of the resource"))
                .required("href")
                );

    }

    void appendServicePath(final ObjectSpecification objectSpec) {

        final String serviceId = serviceIdFor(objectSpec);

        final Path path = new Path();
        swagger.path(String.format("/services/%s", serviceId), path);

        final String serviceModelDefinition = serviceId + "Repr";

        final String tag = tagFor(serviceId, "> services");
        path.get(
                new Operation()
                .tag(tag)
                .description(Util.roSpec("15.1"))
                .produces("application/json")
                .produces("application/json;profile=urn:org.restfulobjects:repr-types/object")
                .response(200,
                        newResponse(Caching.TRANSACTIONAL)
                        .description("OK")
                        .schema(newRefProperty(serviceModelDefinition)))
                );

        final ModelImpl model =
                newModel(Util.roSpec("15.1.2") + ": representation of " + serviceId)
                .property("title", stringProperty())
                .property("serviceId", stringProperty()._default(serviceId))
                .property("members", new ObjectProperty());

        addDefinition(serviceModelDefinition, model);
    }

    ModelImpl appendObjectPathAndModelDefinitions(final ObjectSpecification objectSpec) {

        final String objectType = objectTypeFor(objectSpec);

        final Path path = new Path();
        swagger.path(String.format("/objects/%s/{objectId}", objectType), path);

        final String tag = tagFor(objectType, null);
        final Operation operation = new Operation();
        path.get(operation);
        operation
        .tag(tag)
        .description(Util.roSpec("14.1"))
        .parameter(
                new PathParameter()
                .name("objectId")
                .type("string"))
        .produces("application/json;profile=urn:org.apache.isis/v1")
        .produces("application/json;profile=urn:org.apache.isis/v1;suppress=true")
        .produces("application/json;profile=urn:org.apache.isis/v1;suppress=all")
        .produces("application/json;profile=urn:org.restfulobjects:repr-types/object");


        // per https://github.com/swagger-api/swagger-spec/issues/146, swagger 2.0 doesn't support multiple
        // modelled representations per path and response code;
        // in particular cannot associate representation/model with Accept header ('produces(...) method)
        final String restfulObjectsModelDefinition = objectType + "RestfulObjectsRepr";
        if (false) {
            operation.response(200,
                    newResponse(Caching.TRANSACTIONAL)
                    .description("if Accept: application/json;profile=urn:org.restfulobjects:repr-types/object")
                    .schema(newRefProperty(restfulObjectsModelDefinition)));

            final ModelImpl roSpecModel =
                    newModel(Util.roSpec("14.4") + ": representation of " + objectType)
                    .property("title", stringProperty())
                    .property("domainType", stringProperty()._default(objectType))
                    .property("instanceId", stringProperty())
                    .property("members", new ObjectProperty());
            swagger.addDefinition(restfulObjectsModelDefinition, roSpecModel);
        }

        final String isisModelDefinition = objectType + "Repr";
        operation
        .response(200,
                newResponse(Caching.TRANSACTIONAL)
                .description(objectType + " , if Accept: application/json;profile=urn:org.apache.isis/v1")
                .schema(newRefProperty(isisModelDefinition)));

        final ModelImpl isisModel = new ModelImpl();
        addDefinition(isisModelDefinition, isisModel);

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
            final ObjectSpecification serviceSpec,
            final ObjectAction serviceAction) {

        final String serviceId = serviceIdFor(serviceSpec);
        final String actionId = serviceAction.getId();

        final List<ObjectActionParameter> parameters = serviceAction.getParameters();
        final Path path = new Path();
        swagger.path(String.format("/services/%s/actions/%s/invoke", serviceId, actionId), path);

        final String tag = tagFor(serviceId, "> services");
        final Operation invokeOperation =
                new Operation()
                .tag(tag)
                .description(Util.roSpec("19.1") + ": (invoke) resource of " + serviceId + "#" + actionId)
                .produces("application/json;profile=urn:org.apache.isis/v1")
                .produces("application/json;profile=urn:org.apache.isis/v1;suppress=true")
                .produces("application/json;profile=urn:org.apache.isis/v1;suppress=all")
                .produces("application/json;profile=urn:org.restfulobjects:repr-types/object")
                .produces("application/json;profile=urn:org.restfulobjects:repr-types/action-result")
                ;

        final SemanticsOf semantics = serviceAction.getSemantics();
        if(semantics.isSafeInNature()) {
            path.get(invokeOperation);

            for (final ObjectActionParameter parameter : parameters) {
                invokeOperation
                .parameter(
                        new QueryParameter()
                        .name(parameter.getId())
                        .description(Util.roSpec("2.9.1") + (!_Strings.isNullOrEmpty(parameter.getDescription())? (": " + parameter.getDescription()) : ""))
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
                .description(serviceId + "#" + actionId + " , if Accept: application/json;profile=urn:org.apache.isis/v1")
                .schema(actionReturnTypeFor(serviceAction))
                );
    }

    void appendCollectionTo(
            final ObjectSpecification objectSpec,
            final OneToManyAssociation collection) {

        final String objectType = objectTypeFor(objectSpec);
        final String collectionId = collection.getId();

        final Path path = new Path();
        swagger.path(String.format("/objects/%s/{objectId}/collections/%s", objectType, collectionId), path);

        final String tag = tagFor(objectType, null);
        final Operation collectionOperation =
                new Operation()
                .tag(tag)
                .description(Util.roSpec("17.1") + ": resource of " + objectType + "#" + collectionId)
                .parameter(
                        new PathParameter()
                        .name("objectId")
                        .type("string"))
                .produces("application/json;profile=urn:org.apache.isis/v1")
                .produces("application/json;profile=urn:org.apache.isis/v1;suppress=true")
                .produces("application/json;profile=urn:org.apache.isis/v1;suppress=all")
                .produces("application/json;profile=urn:org.restfulobjects:repr-types/object-collection");

        path.get(collectionOperation);
        collectionOperation
        .response(
                200, new Response()
                .description(objectType + "#" + collectionId + " , if Accept: application/json;profile=urn:org.apache.isis/v1")
                .schema(modelFor(collection))
                );
    }

    void appendObjectActionInvokePath(
            final ObjectSpecification objectSpec,
            final ObjectAction objectAction) {

        final String objectType = objectTypeFor(objectSpec);
        final String actionId = objectAction.getId();

        final List<ObjectActionParameter> parameters = objectAction.getParameters();
        final Path path = new Path();
        swagger.path(String.format("/objects/%s/{objectId}/actions/%s/invoke", objectType, actionId), path);

        final String tag = tagFor(objectType, null);
        final Operation invokeOperation =
                new Operation()
                .tag(tag)
                .description(Util.roSpec("19.1") + ": (invoke) resource of " + objectType + "#" + actionId)
                .parameter(
                        new PathParameter()
                        .name("objectId")
                        .type("string"))
                .produces("application/json;profile=urn:org.apache.isis/v1")
                .produces("application/json;profile=urn:org.apache.isis/v1;suppress=true")
                .produces("application/json;profile=urn:org.apache.isis/v1;suppress=all")
                .produces("application/json;profile=urn:org.restfulobjects:repr-types/action-result");

        final SemanticsOf semantics = objectAction.getSemantics();
        if(semantics.isSafeInNature()) {
            path.get(invokeOperation);

            for (final ObjectActionParameter parameter : parameters) {
                invokeOperation
                .parameter(
                        new QueryParameter()
                        .name(parameter.getId())
                        .description(Util.roSpec("2.9.1") + (!_Strings.isNullOrEmpty(parameter.getDescription())? (": " + parameter.getDescription()) : ""))
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

                final ObjectSpecification specification = parameter.getSpecification();
                final Property valueProperty = specification.isValue() ? modelFor(specification) : refToLinkModel() ;
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
                .description(objectType + "#" + actionId)
                .schema(actionReturnTypeFor(objectAction))
                );
    }

    void appendDefinitionsForOrphanedReferences() {
        final Set<String> referencesWithoutDefinition = getReferencesWithoutDefinition();
        for (String reference : referencesWithoutDefinition) {
            swagger.addDefinition(reference, new ModelImpl());
        }
    }

    Property actionReturnTypeFor(final ObjectAction objectAction) {

        final ObjectSpecification specification = objectAction.getReturnType();
        TypeOfFacet typeOfFacet = objectAction.getFacet(TypeOfFacet.class);
        if(typeOfFacet != null) {
            ObjectSpecification elementSpec = typeOfFacet.valueSpec();
            if(elementSpec != null) {
                return arrayPropertyOf(elementSpec);
            }
        }
        return modelFor(specification);
    }

    private Property modelFor(final OneToManyAssociation collection) {
        ObjectSpecification collectionSpecification = collection.getSpecification();
        return arrayPropertyOf(collectionSpecification);
    }

    private Property arrayPropertyOf(final ObjectSpecification objectSpecification) {
        final ArrayProperty arrayProperty = new ArrayProperty();
        if(objectSpecification != null && objectSpecification.getCorrespondingClass() != Object.class) {
            arrayProperty
            .description("List of " + objectTypeFor(objectSpecification))
            .items(modelFor(objectSpecification));
        } else {
            arrayProperty.items(new ObjectProperty());
        }
        return arrayProperty;
    }

    private Property modelFor(final ObjectSpecification specification) {
        if(specification == null) {
            return new ObjectProperty();
        }

        // no "simple" representation for void or values
        final Class<?> correspondingClass = specification.getCorrespondingClass();
        if(correspondingClass == void.class || correspondingClass == Void.class) {
            return new ObjectProperty();
        }
        // no "simple" representation for values
        final Property property = valuePropertyFactory.newProperty(correspondingClass);
        if(property != null) {
            // was recognized as a value
            return new ObjectProperty();
        }

        if(specification.isParentedOrFreeCollection()) {
            TypeOfFacet typeOfFacet = specification.getFacet(TypeOfFacet.class);
            if(typeOfFacet != null) {
                ObjectSpecification elementSpec = typeOfFacet.valueSpec();
                if(elementSpec != null) {
                    return arrayPropertyOf(elementSpec);
                }
            }
        }

        if(specification.getCorrespondingClass() == java.lang.Object.class) {
            return new ObjectProperty();
        }
        if(specification.getCorrespondingClass() == java.lang.Enum.class) {
            return new StringProperty();
        }
        return newRefProperty(objectTypeFor(specification) + "Repr");
    }

    void updateObjectModel(
            final ModelImpl model,
            final ObjectSpecification objectSpecification,
            final List<OneToOneAssociation> objectProperties,
            final List<OneToManyAssociation> objectCollections) {

        final String objectType = objectTypeFor(objectSpecification);
        final String className = objectSpecification.getFullIdentifier();

        model
        .type("object")
        .description(String.format("%s (%s)", objectType, className));

        for (OneToOneAssociation objectProperty : objectProperties) {
            model.property(
                    objectProperty.getId(),
                    propertyFor(objectProperty.getSpecification()));
        }

        for (OneToManyAssociation objectCollection : objectCollections) {
            final ObjectSpecification elementSpec = objectCollection.getSpecification();
            model.property(
                    objectCollection.getId(),
                    arrayPropertyOf(elementSpec)
                    );
        }
    }

    Property propertyFor(final ObjectSpecification objectSpecification) {
        final Property property =
                valuePropertyFactory.newProperty(objectSpecification.getCorrespondingClass());
        if (property != null) {
            return property;
        }
        else {
            // assume this is a reference to an entity/view model, meaning we use an href
            return refToHrefModel();
        }
    }

    // unused
    static String roSpecForResponseOf(final ObjectAction action) {
        final SemanticsOf semantics = action.getSemantics();
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

    static String serviceIdFor(final ObjectSpecification serviceSpec) {
        return ServiceUtil.idOfSpec(serviceSpec);
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
                .items(refToLinkModel());
    }

    static RefProperty refToLinkModel() {
        return new RefProperty("#/definitions/LinkRepr");
    }

    static RefProperty refToHrefModel() {
        return new RefProperty("#/definitions/HrefRepr");
    }

    static ArrayProperty arrayOfStrings() {
        return new ArrayProperty().items(stringProperty());
    }

    static Response newResponse(final Caching caching) {
        return Util.withCachingHeaders(new Response(), caching);
    }

    String tagFor(final String str, final String fallback) {
        return tagger.tagFor(str, fallback);
    }

    private Property newRefProperty(final String model) {
        addSwaggerReference(model);
        return new RefProperty("#/definitions/" + model);
    }

    private void addDefinition(final String key, final ModelImpl model) {
        addSwaggerDefinition(key);
        swagger.addDefinition(key, model);
    }

    void addSwaggerReference(final String model) {
        references.add(model);
    }

    void addSwaggerDefinition(final String model) {
        definitions.add(model);
    }

    Set<String> getReferencesWithoutDefinition() {
        LinkedHashSet<String> referencesCopy = _Sets.newLinkedHashSet(references);
        referencesCopy.removeAll(definitions);
        return referencesCopy;
    }


}

/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.UpdateNotifier;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.domainobjects.ActionResultRepresentation.ResultType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.Rel;
import org.apache.isis.viewer.json.viewer.representations.RendererFactory;
import org.apache.isis.viewer.json.viewer.representations.RendererFactoryRegistry;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;

public class ActionResultReprRenderer extends ReprRendererAbstract<ActionResultReprRenderer, ObjectAndActionInvocation> {

    private ObjectAdapterLinkTo adapterLinkTo = new DomainObjectLinkTo();
    
    private ObjectAdapter objectAdapter;
    private ObjectAction action;
    private JsonRepresentation arguments;
    private ObjectAdapter returnedAdapter;

    public static class Factory extends ReprRendererFactoryAbstract {

        public Factory() {
            super(RepresentationType.ACTION_RESULT);
        }

        @Override
        public ReprRenderer<?,?> newRenderer(ResourceContext resourceContext, LinkFollower linkFollower, JsonRepresentation representation) {
            return new ActionResultReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    private ActionResultReprRenderer(ResourceContext resourceContext, LinkFollower linkFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    @Override
    public ActionResultReprRenderer with(ObjectAndActionInvocation objectAndActionInvocation) {
        
        objectAdapter = objectAndActionInvocation.getObjectAdapter();
        action = objectAndActionInvocation.getAction();
        arguments = objectAndActionInvocation.getArguments();
        returnedAdapter = objectAndActionInvocation.getReturnedAdapter();

        adapterLinkTo.with(returnedAdapter);

        return this;
    }

    public void using(ObjectAdapterLinkTo adapterLinkTo) {
        this.adapterLinkTo = adapterLinkTo.with(objectAdapter);
    }


    public JsonRepresentation render() {

        final JsonRepresentation representation = representationWithSelfFor(action, arguments);

        addResult(representation);
        
        final JsonRepresentation extensions = JsonRepresentation.newMap();
        representation.mapPut("extensions", extensions);
        addExtensionsIsisProprietaryChangedObjects(extensions);
        
        return representation;
    }

    private void addResult(final JsonRepresentation representation) {
        final JsonRepresentation result = JsonRepresentation.newMap();

        final ResultType resultType = addResultTo(result);
        
        putResultType(representation, resultType);
        representation.mapPut("result", result);
    }

    private ResultType addResultTo(final JsonRepresentation result) {
        
        final ObjectSpecification returnedSpec = returnedAdapter.getSpecification();
        final CollectionFacet collectionFacet = returnedSpec.getFacet(CollectionFacet.class);
        final EncodableFacet encodableFacet = returnedSpec.getFacet(EncodableFacet.class);
        
        // collection
        if (collectionFacet != null) {
          
            final Collection<ObjectAdapter> collectionAdapters = collectionFacet.collection(returnedAdapter);

            final RendererFactory factory = getRendererFactoryRegistry().find(RepresentationType.LIST);
            final ListReprRenderer renderer = (ListReprRenderer) factory.newRenderer(resourceContext, null, result);
            renderer.with(collectionAdapters)
                    .withReturnType(action.getReturnType())
                    .withElementType(returnedAdapter.getElementSpecification());
            
            renderer.render();
            return ResultType.LIST;
            
        } else if(encodableFacet != null) {
            
            final RendererFactory factory = getRendererFactoryRegistry().find(RepresentationType.SCALAR_VALUE);

            ScalarValueReprRenderer renderer = (ScalarValueReprRenderer) factory.newRenderer(resourceContext, null, result);
            renderer.with(returnedAdapter)
                    .withReturnType(action.getReturnType());
            
            renderer.render();
            return ResultType.SCALAR_VALUE;
            
        } else {
            final RendererFactory factory = getRendererFactoryRegistry().find(RepresentationType.DOMAIN_OBJECT);
            final DomainObjectReprRenderer renderer = (DomainObjectReprRenderer) factory.newRenderer(resourceContext, null, result);

            renderer.with(returnedAdapter).includesSelf();
            
            renderer.render();
            return ResultType.DOMAIN_OBJECT;
        }
    }

    private void putResultType(final JsonRepresentation representation, final ResultType resultType) {
        representation.mapPut("resulttype", resultType.getValue());
    }

    private JsonRepresentation representationWithSelfFor(final ObjectAction action, final JsonRepresentation bodyArgs) {
        final JsonRepresentation links = JsonRepresentation.newArray();
        representation.mapPut("links", links);
        
        final LinkBuilder selfLinkBuilder = adapterLinkTo.memberBuilder(Rel.SELF, MemberType.ACTION, action, RepresentationType.ACTION_RESULT, "invoke");
        
        // TODO: remove duplication with AbstractObjectMember#addLinkTo
        final MemberType memberType = MemberType.of(action);
        Map<String, MutatorSpec> mutators = memberType.getMutators();
        
        final ActionSemantics semantics = ActionSemantics.determine(getResourceContext(), action);
        final String mutator = semantics.getInvokeKey();
        final MutatorSpec mutatorSpec = mutators.get(mutator);
        selfLinkBuilder.withHttpMethod(mutatorSpec.httpMethod);
        
        final JsonRepresentation selfLink = selfLinkBuilder.build();

        links.arrayAdd(selfLink);
        selfLink.mapPut("args", bodyArgs);
        return representation;
    }


    private void addExtensionsIsisProprietaryChangedObjects(final JsonRepresentation extensions) {
        final UpdateNotifier updateNotifier = getUpdateNotifier();
        
        addToExtensions(extensions, "changed", updateNotifier.getChangedObjects());
        addToExtensions(extensions, "disposed", updateNotifier.getDisposedObjects());
    }

    protected void addToExtensions(final JsonRepresentation extensions, final String key, final List<ObjectAdapter> adapters) {
        final JsonRepresentation changed = JsonRepresentation.newArray();
        extensions.mapPut(key, changed);
        for (ObjectAdapter changedAdapter : adapters) {
            DomainObjectReprRenderer.newLinkToBuilder(getResourceContext(), Rel.OBJECT, changedAdapter);
        }
    }

    protected UpdateNotifier getUpdateNotifier() {
        // TODO: yuck
        return IsisContext.getCurrentTransaction().getUpdateNotifier();
    }

    protected RendererFactoryRegistry getRendererFactoryRegistry() {
        // TODO: yuck
        return RendererFactoryRegistry.instance;
    }



}
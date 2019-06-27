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
package org.apache.isis.viewer.restfulobjects.server.resources;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.base._Tuples.Tuple2;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.spec.ManagedObjectState;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.memento.Data;
import org.apache.isis.runtime.persistence.FixturesInstalledState;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.transaction.IsisTransactionAspectSupport;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ActionResultReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainObjectLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.MemberReprMode;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAdapterLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndAction;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndCollection2;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndProperty2;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.server.ResourceContext;

import lombok.Getter;

class DomainResourceHelper {

    static class RepresentationServiceContextAdapter implements RepresentationService.Context {

        private final RendererContext rendererContext;
        @Getter private final ObjectAdapterLinkTo adapterLinkTo;
        @Getter private RepresentationService.Intent intent;

        RepresentationServiceContextAdapter(
                final RendererContext rendererContext,
                final ObjectAdapterLinkTo adapterLinkTo) {
            this.rendererContext = rendererContext;
            this.adapterLinkTo = adapterLinkTo;
            this.intent = rendererContext.getIntent();
        }

        @Override
        public String urlFor(String url) {
            return rendererContext.urlFor(url);
        }

        @Override
        public AuthenticationSession getAuthenticationSession() {
            return rendererContext.getAuthenticationSession();
        }

        @Override
        public IsisConfiguration getConfiguration() {
            return rendererContext.getConfiguration();
        }

        @Override
        public Where getWhere() {
            return rendererContext.getWhere();
        }

        @Override
        public List<List<String>> getFollowLinks() {
            return rendererContext.getFollowLinks();
        }

        @Override
        public boolean isValidateOnly() {
            return rendererContext.isValidateOnly();
        }

        @Override
        public List<MediaType> getAcceptableMediaTypes() {
            return rendererContext.getAcceptableMediaTypes();
        }

        @Override
        public boolean canEagerlyRender(ObjectAdapter objectAdapter) {
            return rendererContext.canEagerlyRender(objectAdapter);
        }

        @Override
        public boolean honorUiHints() {
            return rendererContext.honorUiHints();
        }

        @Override
        public boolean objectPropertyValuesOnly() {
            return rendererContext.objectPropertyValuesOnly();
        }

        @Override
        public boolean suppressDescribedByLinks() {
            return rendererContext.suppressDescribedByLinks();
        }

        @Override
        public boolean suppressUpdateLink() {
            return rendererContext.suppressUpdateLink();
        }

        @Override
        public boolean suppressMemberId() {
            return rendererContext.suppressMemberId();
        }

        @Override
        public boolean suppressMemberLinks() {
            return rendererContext.suppressMemberLinks();
        }

        @Override
        public boolean suppressMemberExtensions() {
            return rendererContext.suppressMemberExtensions();
        }

        @Override
        public boolean suppressMemberDisabledReason() {
            return rendererContext.suppressMemberDisabledReason();
        }

        @Override
        public InteractionInitiatedBy getInteractionInitiatedBy() {
            return rendererContext.getInteractionInitiatedBy();
        }

        @Override
        public SpecificationLoader getSpecificationLoader() {
            return rendererContext.getSpecificationLoader();
        }

        @Override
        public ServiceInjector getServiceInjector() {
            return rendererContext.getServiceInjector();
        }
        
		@Override
		public ServiceRegistry getServiceRegistry() {
			return rendererContext.getServiceRegistry();
		}

		@Override
		public ObjectAdapter adapterOfPojo(Object pojo) {
			return rendererContext.adapterOfPojo(pojo);
		}
		
		@Override
		public ObjectAdapter adapterOfMemento(ObjectSpecification spec, Oid oid, Data data) {
			return rendererContext.adapterOfMemento(spec, oid, data);
		}

		@Override
		public ObjectAdapter newTransientInstance(ObjectSpecification domainTypeSpec) {
			return rendererContext.newTransientInstance(domainTypeSpec);
		}

		@Override
		public void makePersistentInTransaction(ObjectAdapter objectAdapter) {
			rendererContext.makePersistentInTransaction(objectAdapter);
		}

		@Override
		public Object fetchPersistentPojoInTransaction(RootOid rootOid) {
			return rendererContext.fetchPersistentPojoInTransaction(rootOid);
		}

		@Override
		public ManagedObjectState stateOf(Object domainObject) {
			return rendererContext.stateOf(domainObject);
		}

		@Override
		public void logoutAuthenticationSession() {
			rendererContext.logoutAuthenticationSession();
		}

		@Override
		public Tuple2<ObjectAdapter, ObjectAction> findHomePageAction() {
			return rendererContext.findHomePageAction();
		}

		@Override
		public FixturesInstalledState getFixturesInstalledState() {
			return rendererContext.getFixturesInstalledState();
		}


    }

    private final RepresentationServiceContextAdapter representationServiceContext;
    private final RepresentationService representationService;
    private final TransactionService transactionService;

    public DomainResourceHelper(final ResourceContext resourceContext, final ObjectAdapter objectAdapter) {
        this(resourceContext, objectAdapter, new DomainObjectLinkTo());
    }

    public DomainResourceHelper(
            final ResourceContext resourceContext,
            final ObjectAdapter objectAdapter,
            final ObjectAdapterLinkTo adapterLinkTo) {

        this.resourceContext = resourceContext;
        this.objectAdapter = objectAdapter;

        representationServiceContext = new RepresentationServiceContextAdapter(resourceContext, adapterLinkTo);

        adapterLinkTo.usingUrlBase(this.resourceContext)
        .with(this.objectAdapter);

        representationService = lookupService(RepresentationService.class);
        transactionService = lookupService(TransactionService.class);
    }

    private final ResourceContext resourceContext;
    private final ObjectAdapter objectAdapter;


    // //////////////////////////////////////
    // Helpers (resource delegate here)
    // //////////////////////////////////////

    /**
     * Simply delegates to the {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to
     * render a representation of the object.
     */
    public Response objectRepresentation() {
        transactionService.flushTransaction();
        return representationService
                .objectRepresentation(representationServiceContext, objectAdapter);
    }

    /**
     * Obtains the property (checking it is visible) of the object and then delegates to the
     * {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of that property.
     */
    public Response propertyDetails(
            final String propertyId,
            final MemberReprMode memberMode) {

        ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(representationServiceContext, objectAdapter);

        final OneToOneAssociation property = accessHelper.getPropertyThatIsVisibleForIntent(propertyId, ObjectAdapterAccessHelper.Intent.ACCESS);

        transactionService.flushTransaction();
        return representationService.propertyDetails(representationServiceContext, new ObjectAndProperty2(objectAdapter, property, memberMode), memberMode);
    }


    /**
     * Obtains the collection (checking it is visible) of the object and then delegates to the
     * {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of that collection.
     */
    public Response collectionDetails(
            final String collectionId,
            final MemberReprMode memberMode) {

        ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(representationServiceContext, objectAdapter);

        final OneToManyAssociation collection = accessHelper.getCollectionThatIsVisibleForIntent(collectionId, ObjectAdapterAccessHelper.Intent.ACCESS);

        transactionService.flushTransaction();
        return representationService.collectionDetails(representationServiceContext, new ObjectAndCollection2(objectAdapter, collection, memberMode), memberMode);
    }


    /**
     * Obtains the action details (arguments etc), checking it is visible, of the object and then delegates to the
     * {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of that object's action (arguments).
     */
    public Response actionPrompt(final String actionId) {

        ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(representationServiceContext, objectAdapter);

        final ObjectAction action = accessHelper.getObjectActionThatIsVisibleForIntent(actionId, ObjectAdapterAccessHelper.Intent.ACCESS);

        transactionService.flushTransaction();
        return representationService.actionPrompt(representationServiceContext, new ObjectAndAction(objectAdapter, action));
    }

    /**
     * Invokes the action for the object  (checking it is visible) and then delegates to the
     * {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of the result of that action.
     *
     * <p>
     *     The action must have {@link SemanticsOf#isSafeInNature()} safe/request-cacheable}  semantics
     *     otherwise an error response is thrown.
     * </p>
     */
    public Response invokeActionQueryOnly(final String actionId, final JsonRepresentation arguments) {

        final ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(representationServiceContext, objectAdapter);

        final ObjectAction action = accessHelper.getObjectActionThatIsVisibleForIntent(actionId, ObjectAdapterAccessHelper.Intent.MUTATE);

        final SemanticsOf actionSemantics = action.getSemantics();
        if (! actionSemantics.isSafeInNature()) {
            throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Method not allowed; action '%s' does not have safe semantics", action.getId());
        }

        return invokeActionUsingAdapters(action, arguments, ActionResultReprRenderer.SelfLink.INCLUDED);
    }

    /**
     * Invokes the action for the object  (checking it is visible) and then delegates to the
     * {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of the result of that action.
     *
     * <p>
     *     The action must have {@link SemanticsOf#IDEMPOTENT idempotent}
     *     semantics otherwise an error response is thrown.
     * </p>
     */
    public Response invokeActionIdempotent(final String actionId, final JsonRepresentation arguments) {

        final ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(representationServiceContext, objectAdapter);

        final ObjectAction action = accessHelper.getObjectActionThatIsVisibleForIntent(actionId, ObjectAdapterAccessHelper.Intent.MUTATE);

        final SemanticsOf actionSemantics = action.getSemantics();
        if (!actionSemantics.isIdempotentInNature()) {
            throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Method not allowed; action '%s' is not idempotent", action.getId());
        }
        return invokeActionUsingAdapters(action, arguments, ActionResultReprRenderer.SelfLink.EXCLUDED);
    }

    /**
     * Invokes the action for the object  (checking it is visible) and then delegates to the
     * {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of the result of that action.
     */
    public Response invokeAction(final String actionId, final JsonRepresentation arguments) {

        ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(representationServiceContext, objectAdapter);

        final ObjectAction action = accessHelper.getObjectActionThatIsVisibleForIntent(actionId, ObjectAdapterAccessHelper.Intent.MUTATE);

        return invokeActionUsingAdapters(action, arguments, ActionResultReprRenderer.SelfLink.EXCLUDED);
    }


    private Response invokeActionUsingAdapters(
            final ObjectAction action,
            final JsonRepresentation arguments,
            final ActionResultReprRenderer.SelfLink selfLink) {

        final RepresentationService.Context rendererContext = representationServiceContext;
        final ObjectAdapter objectAdapter = this.objectAdapter;

        final ObjectActionArgHelper argHelper = new ObjectActionArgHelper(rendererContext, objectAdapter, action);

        final List<ObjectAdapter> argAdapters = argHelper.parseAndValidateArguments(arguments);

        if(rendererContext.isValidateOnly()) {
            // nothing more to do.
            // if there had been a validation error, then an exception would have been thrown above.
            return Response.noContent().build();
        }

        // invoke
        final ObjectAdapter mixedInAdapter = null; // action will automatically fill in if a mixin
        final ObjectAdapter[] argAdapterArr = argAdapters.toArray(new ObjectAdapter[argAdapters.size()]);
        final ObjectAdapter returnedAdapter = action.execute(
                objectAdapter,  mixedInAdapter, argAdapterArr,
                InteractionInitiatedBy.USER);

        final ObjectAndActionInvocation objectAndActionInvocation =
                new ObjectAndActionInvocation(objectAdapter, action, arguments, argAdapters, returnedAdapter, selfLink);

        // response
        transactionService.flushTransaction();
        return representationService.actionResult(representationServiceContext, objectAndActionInvocation, selfLink);
    }


    // //////////////////////////////////////
    // dependencies (from context)
    // //////////////////////////////////////

    private <T> T lookupService(Class<T> serviceType) {
        return resourceContext.getServiceRegistry().lookupServiceElseFail(serviceType);
    }

}


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
package org.apache.isis.viewer.wicket.model.models;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.routing.RoutingService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.applib.value.NamedWithMimeType;
import org.apache.isis.applib.value.OpenUrlStrategy;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.config.viewer.web.WebAppContextPath;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.memento.ActionMemento;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.action.form.FormUiModel;
import org.apache.isis.viewer.wicket.model.models.interaction.action.ActionInteractionModelWkt;
import org.apache.isis.viewer.wicket.model.models.interaction.action.ParameterUiModelWkt;

import lombok.NonNull;
import lombok.val;

public final class ActionModel
extends ManagedObjectModel
implements FormUiModel, FormExecutorContext, BookmarkableModel {

    private static final long serialVersionUID = 1L;

    public ActionModel copy() {
        return new ActionModel(this);
    }

    // -- FACTORY METHODS

    public static ActionModel of(final EntityModel actionOwner, final ObjectAction action) {
        return of(actionOwner, action.getMemento());
    }

    public static ActionModel of(final EntityModel actionOwner, final ActionMemento actionMemento) {
        return new ActionModel(actionOwner, actionMemento);
    }

    public static ActionModel ofPageParameters(
            final IsisAppCommonContext commonContext,
            final PageParameters pageParameters) {

        return PageParameterUtil.actionModelFor(commonContext, pageParameters);
    }


    // -- BOOKMARKABLE

    @Override
    public PageParameters getPageParametersWithoutUiHints() {
        val adapter = getOwner();
        val objectAction = getMetaModel();
        return PageParameterUtil.createPageParametersForAction(adapter, objectAction, snapshotArgs());
    }

    @Override
    public PageParameters getPageParameters() {
        return getPageParametersWithoutUiHints();
    }

    // --

    @Override
    public ObjectAction getMetaModel() {
        return actionMemento.getAction(this::getSpecificationLoader);
    }

    @Override
    public boolean hasAsRootPolicy() {
        return true;
    }

    @Override
    public EntityModel getParentUiModel() {
        return ownerModel;
    }

    // -- HELPERS

    private final EntityModel ownerModel;
    private final ActionMemento actionMemento;

    // lazy in support of serialization of this class
    private transient ActionInteractionModelWkt actionInteractionModel;

    private ActionModel(final EntityModel entityModel, final ActionMemento actionMemento) {
        super(entityModel.getCommonContext());
        this.ownerModel = entityModel;
        this.actionMemento = actionMemento;
    }

    /**
     * Copy constructor, as called by {@link #copy()}.
     */
    private ActionModel(final ActionModel actionModel) {
        super(actionModel.getCommonContext());
        this.ownerModel = actionModel.ownerModel;
        this.actionMemento = actionModel.actionMemento;
    }

    private ActionInteractionModelWkt actionInteractionModel() {
        if(actionInteractionModel==null) {
            actionInteractionModel = new ActionInteractionModelWkt(
                    getCommonContext(),
                    ActionInteraction.start(
                            ownerModel.getManagedObject(),
                            actionMemento.getIdentifier().getMemberLogicalName(),
                            WHERE_FOR_ACTION_INVOCATION));
        }
        return actionInteractionModel;
    }

    private Can<ManagedObject> snapshotArgs() {
        return actionInteractionModel().parameterNegotiationModel().get().getParamValues();
    }

    @Override
    public ManagedObject getOwner() {
        return ownerModel.load();
    }

    @Override
    protected ManagedObject load() {

        // from getObject()/reExecute
        detach(); // force re-execute

        // TODO: think we need another field to determine if args have been populated.
        val results = executeAction();

        return results;
    }

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly)
    // for any other value for Where
    public static final Where WHERE_FOR_ACTION_INVOCATION = Where.ANYWHERE;

    private ManagedObject executeAction() {

        val targetAdapter = getOwner();
        final Can<ManagedObject> arguments = snapshotArgs();
        final ObjectAction action = getMetaModel();

        val head = action.interactionHead(targetAdapter);

        val resultAdapter =
                action.executeWithRuleChecking(
                        head, arguments,
                        InteractionInitiatedBy.USER,
                        WHERE_FOR_ACTION_INVOCATION);

        val resultPojo = resultAdapter != null ? resultAdapter.getPojo() : null;

        return getServiceRegistry()
                .select(RoutingService.class)
                .stream()
                .filter(routingService->routingService.canRoute(resultPojo))
                .map(routingService->routingService.route(resultPojo))
                .filter(_NullSafe::isPresent)
                .map(super.getObjectManager()::adapt)
                .filter(_NullSafe::isPresent)
                .findFirst()
                .orElse(resultAdapter);

    }

    @Override
    public void setObject(final ManagedObject object) {
        throw new UnsupportedOperationException("target adapter for ActionModel cannot be changed");
    }

    /** Resets arguments to their fixed point default values
     * @see ActionInteractionHead#defaults()
     */
    public void clearArguments() {
        actionInteractionModel().resetParametersToDefault();
    }

    /**
     * Bookmarkable if the {@link ObjectAction action} has a {@link BookmarkPolicyFacet bookmark} policy
     * of {@link BookmarkPolicy#AS_ROOT root}, and has safe {@link ObjectAction#getSemantics() semantics}.
     */
    public boolean isBookmarkable() {
        final ObjectAction action = getMetaModel();
        final BookmarkPolicyFacet bookmarkPolicy = action.getFacet(BookmarkPolicyFacet.class);
        final boolean safeSemantics = action.getSemantics().isSafeInNature();
        return bookmarkPolicy.value() == BookmarkPolicy.AS_ROOT && safeSemantics;
    }

    // //////////////////////////////////////

    /**
     * Simply executes the action.
     *
     * Previously there was exception handling code here also, but this has now been centralized
     * within FormExecutorAbstract
     */
    public ManagedObject execute() {
        final ManagedObject resultAdapter = this.getObject();
        return resultAdapter;
    }


    // //////////////////////////////////////

    public static IRequestHandler redirectHandler(
            final Object value,
            final @NonNull OpenUrlStrategy openUrlStrategy,
            final @NonNull WebAppContextPath webAppContextPath) {

        if(value instanceof java.net.URL) {
            val url = (java.net.URL) value;
            return new RedirectRequestHandlerWithOpenUrlStrategy(url.toString());
        }
        if(value instanceof LocalResourcePath) {
            val localResourcePath = (LocalResourcePath) value;
            return new RedirectRequestHandlerWithOpenUrlStrategy(
                    localResourcePath.getEffectivePath(webAppContextPath::prependContextPath),
                    localResourcePath.getOpenUrlStrategy());
        }
        return null;
    }

    public IRequestHandler downloadHandler(final Object value) {
        if(value instanceof Clob) {
            val clob = (Clob)value;
            return handlerFor(resourceStreamFor(clob), clob);
        }
        if(value instanceof Blob) {
            val blob = (Blob)value;
            return handlerFor(resourceStreamFor(blob), blob);
        }
        return null;
    }

    private static IResourceStream resourceStreamFor(final Blob blob) {
        final IResourceStream resourceStream = new AbstractResourceStream() {

            private static final long serialVersionUID = 1L;

            @Override
            public InputStream getInputStream() throws ResourceStreamNotFoundException {
                return new ByteArrayInputStream(blob.getBytes());
            }

            @Override
            public String getContentType() {
                return blob.getMimeType().toString();
            }

            @Override
            public void close() throws IOException {
            }
        };
        return resourceStream;
    }

    private static IResourceStream resourceStreamFor(final Clob clob) {
        return new StringResourceStream(clob.getChars(), clob.getMimeType().toString());
    }

    private IRequestHandler handlerFor(
            final IResourceStream resourceStream,
            final NamedWithMimeType namedWithMimeType) {
        val handler =
                new ResourceStreamRequestHandler(resourceStream, namedWithMimeType.getName());
        handler.setContentDisposition(ContentDisposition.ATTACHMENT);

        //ISIS-1619, prevent clients from caching the response content
        return isIdempotentOrCachable()
                ? handler
                : enforceNoCacheOnClientSide(handler);
    }

    //////////////////////////////////////////////////

    @Override
    public PromptStyle getPromptStyle() {
        final ObjectAction objectAction = getMetaModel();
        final ObjectSpecification objectActionOwner = objectAction.getOnType();
        if(objectActionOwner.isManagedBean()) {
            // tried to move this test into PromptStyleFacetFallback,
            // however it's not that easy to lookup the owning type
            final PromptStyleFacet facet = getFacet(PromptStyleFacet.class);
            if (facet != null) {
                final PromptStyle promptStyle = facet.value();
                if (promptStyle.isDialog()) {
                    // could be specified explicitly.
                    return promptStyle;
                }
            }
            return PromptStyle.DIALOG;
        }
        if(objectAction.getParameterCount() == 0) {
            // a bit of a hack, the point being that the UI for dialog correctly handles no-args,
            // whereas for INLINE it would render a form with no fields
            return PromptStyle.DIALOG;
        }
        final PromptStyleFacet facet = getFacet(PromptStyleFacet.class);
        if(facet == null) {
            // don't think this can happen actually, see PromptStyleFacetFallback
            return PromptStyle.INLINE;
        }
        final PromptStyle promptStyle = facet.value();
        if (promptStyle == PromptStyle.AS_CONFIGURED) {
            // I don't think this can happen, actually...
            // when the metamodel is built, it should replace AS_CONFIGURED with one of the other prompts
            // (see PromptStyleConfiguration and PromptStyleFacetFallback)
            return PromptStyle.INLINE;
        }
        return promptStyle;
    }

    public <T extends Facet> T getFacet(final Class<T> facetType) {
        final FacetHolder facetHolder = getMetaModel();
        return facetHolder.getFacet(facetType);
    }


    //////////////////////////////////////////////////

    private InlinePromptContext inlinePromptContext;

    /**
     * Further hint, to support inline prompts...
     */
    @Override
    public InlinePromptContext getInlinePromptContext() {
        return inlinePromptContext;
    }

    public void setInlinePromptContext(final InlinePromptContext inlinePromptContext) {
        this.inlinePromptContext = inlinePromptContext;
    }

    public void setParameterValue(final ObjectActionParameter actionParameter, final ManagedObject newParamValue) {
        actionInteractionModel().parameterNegotiationModel().get().setParamValue(actionParameter.getNumber(), newParamValue);
    }

    public void clearParameterValue(final ObjectActionParameter actionParameter) {
        actionInteractionModel().parameterNegotiationModel().get().clearParamValue(actionParameter.getNumber());
    }

    @Override
    public Stream<ParameterUiModelWkt> streamPendingParamUiModels() {
        return actionInteractionModel().streamParameterUiModels();
    }

    public void reassessPendingParamUiModels(final int skipCount) {

        actionInteractionModel().streamParameterUiModels()
        .skip(skipCount)
        .forEach(paramUiModel->{

            val pendingArgs = paramUiModel.getPendingParameterModel();
            val actionParameter = paramUiModel.getMetaModel();
            val paramDefaultValue = actionParameter.getDefault(pendingArgs);

            if (ManagedObjects.isNullOrUnspecifiedOrEmpty(paramDefaultValue)) {
                clearParameterValue(actionParameter);
            } else {
                setParameterValue(actionParameter, paramDefaultValue);
            }

            // We could automatically make sure the parameter value is one of the (reassessed) choices,
            // if not then blank it out.
            // corner case: the parameter value might be non-scalar
            //     we could remove from the parameter value (collection) all that no longer
            //     conform to the available (reassessed) choices,

            //XXX HOWEVER ...
            // there are pros and cons to that depending on the situation
            // I'd rather not risk a bad user experience by blanking out values,
            // instead let the user control the situation, we have validation to signal what to do

//            val paramIsScalar = actionParameter.getSpecification().isNotCollection();
//
//            boolean shouldBlankout = false;
//
//            if(!isEmpty && paramIsScalar) {
//
//                if(hasChoices) {
//                    val choices = actionParameter
//                            .getChoices(pendingArgs, InteractionInitiatedBy.USER);
//
//                    shouldBlankout =
//                            ! isPartOfChoicesConsideringDependentArgs(paramValue, choices);
//
//                } else if(hasAutoComplete) {
//
//                    //don't blank-out, even though could fail validation later
//                    shouldBlankout = false;
//                }
//            }
//
//            if(shouldBlankout) {
//                clearParameterValue(actionParameter);
//            }

        });

    }

    // -- HELPER

    private boolean isIdempotentOrCachable() {
        return ObjectAction.Util.isIdempotentOrCachable(getMetaModel());
    }

//    private boolean isPartOfChoicesConsideringDependentArgs(
//            ManagedObject paramValue,
//            Can<ManagedObject> choices) {
//
//        val pendingValue = paramValue.getPojo();
//        return choices
//                .stream()
//                .anyMatch(choice->Objects.equals(pendingValue, choice.getPojo()));
//    }

    // -- CLIENT SIDE CACHING ASPECTS ...

    private static IRequestHandler enforceNoCacheOnClientSide(final IRequestHandler downloadHandler){
        if(downloadHandler==null) {
            return downloadHandler;
        }
        if(downloadHandler instanceof ResourceStreamRequestHandler)
            ((ResourceStreamRequestHandler) downloadHandler)
            .setCacheDuration(org.apache.wicket.util.time.Duration.seconds(0));

        return downloadHandler;
    }

}

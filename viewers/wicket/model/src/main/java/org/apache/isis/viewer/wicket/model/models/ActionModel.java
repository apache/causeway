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

import org.apache.wicket.model.ChainingModel;
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
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.applib.value.NamedWithMimeType;
import org.apache.isis.applib.value.OpenUrlStrategy;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.viewer.web.WebAppContextPath;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.memento.ActionMemento;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.action.form.FormUiModel;
import org.apache.isis.viewer.wicket.model.models.interaction.act.ActionInteractionWkt;
import org.apache.isis.viewer.wicket.model.models.interaction.act.ParameterUiModelWkt;
import org.apache.isis.viewer.wicket.model.util.PageParameterUtils;

import lombok.NonNull;
import lombok.val;

/**
 * Represents an action of an entity.
 *
 * @implSpec
 * <pre>
 * ActionModel --chained-to--> EntityModel
 * ActionModel --bound-to--> ActionInteractionWkt (delegate)
 * </pre>
 */
public final class ActionModel
extends ChainingModel<ManagedObject>
implements FormUiModel, FormExecutorContext, BookmarkableModel {

    private static final long serialVersionUID = 1L;

    // -- FACTORY METHODS

    public static ActionModel wrap(final EntityModel actionOwner, final ActionInteractionWkt delegate) {
        return new ActionModel(actionOwner, delegate);
    }

    public static ActionModel of(final EntityModel actionOwner, final ObjectAction action) {
        val delegate = new ActionInteractionWkt(
                actionOwner.bookmarkedObjectModel(),
                action.getFeatureIdentifier().getMemberLogicalName(),
                Where.ANYWHERE);
        return wrap(actionOwner, delegate);
    }

    public static ActionModel of(final EntityModel actionOwner, final ActionMemento actionMemento) {
        val delegate = new ActionInteractionWkt(
                actionOwner.bookmarkedObjectModel(),
                actionMemento.getIdentifier().getMemberLogicalName(),
                Where.ANYWHERE);
        return wrap(actionOwner, delegate);
    }

    public static ActionModel ofPageParameters(
            final IsisAppCommonContext commonContext,
            final PageParameters pageParameters) {

        return PageParameterUtils.actionModelFor(commonContext, pageParameters);
    }

    // -- CONSTRUCTION

    private final ActionInteractionWkt delegate;

    private ActionModel(final EntityModel parentEntityModel, final ActionInteractionWkt delegate) {
        super(parentEntityModel);
        this.delegate = delegate;
    }

    // --

    public void actionInteraction() {
        delegate.actionInteraction();
    }

    @Override
    public IsisAppCommonContext getCommonContext() {
        return delegate.getCommonContext();
    }

    // -- BOOKMARKABLE

    @Override
    public PageParameters getPageParametersWithoutUiHints() {
        return PageParameterUtils.createPageParametersForAction(getOwner(), getMetaModel(), snapshotArgs());
    }

    @Override
    public PageParameters getPageParameters() {
        return getPageParametersWithoutUiHints();
    }

    // --

    @Override
    public ObjectAction getMetaModel() {
        return delegate.actionInteraction().getMetamodel().get();
    }

    @Override
    public boolean hasAsRootPolicy() {
        return true;
    }

    @Override
    public EntityModel getParentUiModel() {
        return (EntityModel) super.getTarget();
    }

    public ActionModel copy() {
        return wrap(getParentUiModel(), delegate);
    }

    // -- HELPERS

    private Can<ManagedObject> snapshotArgs() {
        return delegate.parameterNegotiationModel().get().getParamValues();
    }

    @Override
    public ManagedObject getOwner() {
        return delegate.actionInteraction().getManagedActionElseFail().getOwner();
    }

    public ManagedObject executeActionAndReturnResult() {
        val pendingArgs = delegate.parameterNegotiationModel().get();
        val result = delegate.actionInteraction().invokeWithRuleChecking(pendingArgs);
        return result;
    }


    @Override
    public void setObject(final ManagedObject object) {
        throw new UnsupportedOperationException("ActionModel is a chained model - don't mess with the chain");
    }

    /** Resets arguments to their fixed point default values
     * @see ActionInteractionHead#defaults()
     */
    public void clearArguments() {
        delegate.resetParametersToDefault();
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
        final ObjectSpecification objectActionOwner = objectAction.getDeclaringType();
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
        delegate.parameterNegotiationModel().get().setParamValue(actionParameter.getNumber(), newParamValue);
    }

    public void clearParameterValue(final ObjectActionParameter actionParameter) {
        delegate.parameterNegotiationModel().get().clearParamValue(actionParameter.getNumber());
    }

    @Override
    public Stream<ParameterUiModelWkt> streamPendingParamUiModels() {
        return delegate.streamParameterUiModels();
    }

    public void reassessPendingParamUiModels(final int skipCount) {

        delegate.streamParameterUiModels()
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

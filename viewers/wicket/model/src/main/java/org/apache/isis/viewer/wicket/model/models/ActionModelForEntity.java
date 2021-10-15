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

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.NamedWithMimeType;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.interaction.act.ActionInteractionWkt;
import org.apache.isis.viewer.wicket.model.models.interaction.act.ParameterUiModelWkt;
import org.apache.isis.viewer.wicket.model.util.PageParameterUtils;

import lombok.val;

/**
 * Represents an action (a member) of an entity.
 *
 * @implSpec
 * <pre>
 * ActionModel --chained-to--> EntityModel
 * ActionModel --bound-to--> ActionInteractionWkt (delegate)
 * </pre>
 */
public final class ActionModelForEntity
extends ChainingModel<ManagedObject>
implements ActionModel {

    private static final long serialVersionUID = 1L;

    // -- FACTORY METHODS

    public static ActionModelForEntity supportingParameter(
            final ScalarParameterModel scalarParameterModel) {
        //FIXME[ISIS-2877] impl.
        // TODO Auto-generated method stub
        return null;
    }

    public static ActionModelForEntity ofEntity(
            final EntityModel actionOwner,
            final Identifier actionIdentifier,
            final ScalarModel associatedWithScalarModelIfAny,
            final EntityCollectionModel associatedWithCollectionModelIfAny) {
        val delegate = new ActionInteractionWkt(
                actionOwner.bookmarkedObjectModel(),
                actionIdentifier.getMemberLogicalName(),
                Where.ANYWHERE,
                associatedWithScalarModelIfAny,
                associatedWithCollectionModelIfAny);
        return new ActionModelForEntity(actionOwner, delegate);
    }

    public static ActionModel ofPageParameters(
            final IsisAppCommonContext commonContext,
            final PageParameters pageParameters) {

        return PageParameterUtils.actionModelFor(commonContext, pageParameters);
    }

    // -- CONSTRUCTION

    private final ActionInteractionWkt delegate;

    private ActionModelForEntity(final EntityModel parentEntityModel, final ActionInteractionWkt delegate) {
        super(parentEntityModel);
        this.delegate = delegate;
    }

    // --

    @Override
    public ActionInteraction getActionInteraction() {
        return delegate.actionInteraction();
    }

    @Override
    public IsisAppCommonContext getCommonContext() {
        return delegate.getCommonContext();
    }

    // -- BOOKMARKABLE

    @Override
    public PageParameters getPageParametersWithoutUiHints() {
        return PageParameterUtils
                .createPageParametersForAction(getParentObject(), getAction(), snapshotArgs());
    }

    @Override
    public PageParameters getPageParameters() {
        return getPageParametersWithoutUiHints();
    }

    // --

    @Override
    public boolean hasAsRootPolicy() {
        return true;
    }

    @Override
    public EntityModel getParentUiModel() {
        return (EntityModel) super.getTarget();
    }

    @Override
    public Can<ManagedObject> snapshotArgs() {
        return delegate.parameterNegotiationModel().getParamValues();
    }

    @Override
    public ManagedObject executeActionAndReturnResult() {
        val pendingArgs = delegate.parameterNegotiationModel();
        val result = delegate.actionInteraction().invokeWithRuleChecking(pendingArgs);
        return result;
    }


    @Override
    public void setObject(final ManagedObject object) {
        throw new UnsupportedOperationException("ActionModel is a chained model - don't mess with the chain");
    }

    /** Resets arguments to their fixed point default values
     * @see ActionInteractionHead#defaults(org.apache.isis.core.metamodel.interactions.managed.ManagedAction)
     */
    @Override
    public void clearArguments() {
        delegate.resetParametersToDefault();
    }

    // //////////////////////////////////////

    @Override
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
        return getAction().getSemantics().isIdempotentOrCachable()
                ? handler
                : enforceNoCacheOnClientSide(handler);
    }

    //////////////////////////////////////////////////

    @Override
    public PromptStyle getPromptStyle() {
        final ObjectAction objectAction = getAction();
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
        final FacetHolder facetHolder = getAction();
        return facetHolder.getFacet(facetType);
    }


    //////////////////////////////////////////////////

    @Override
    public InlinePromptContext getInlinePromptContext() {
        return delegate.getInlinePromptContext();
    }

    public void setParameterValue(final ObjectActionParameter actionParameter, final ManagedObject newParamValue) {
        delegate.parameterNegotiationModel().setParamValue(actionParameter.getParameterIndex(), newParamValue);
    }

    public void clearParameterValue(final ObjectActionParameter actionParameter) {
        delegate.parameterNegotiationModel().clearParamValue(actionParameter.getParameterIndex());
    }

    @Override
    public Stream<ParameterUiModelWkt> streamPendingParamUiModels() {
        return delegate.streamParameterUiModels();
    }

    @Override
    public void reassessPendingParamUiModels(final int skipCount) {

        delegate.streamParameterUiModels()
        .skip(skipCount)
        .forEach(paramUiModel->{

            val pendingArgs = paramUiModel.getParameterNegotiationModel();
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

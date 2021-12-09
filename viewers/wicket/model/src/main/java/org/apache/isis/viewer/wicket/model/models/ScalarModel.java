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

import java.util.List;
import java.util.Optional;

import org.apache.wicket.model.ChainingModel;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedValue;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.feature.ScalarUiModel;
import org.apache.isis.viewer.common.model.object.ObjectUiModel;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.HasRenderingHints;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.RenderingHint;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * Represents a scalar of an entity, either a {@link EitherParamOrProp#PROPERTY property} or
 * a {@link EitherParamOrProp#PARAMETER parameter}.
 *
 * <p>
 * Is the backing model to each of the fields that appear in forms (for entities
 * or action dialogs).
 *
 * @implSpec
 * <pre>
 * ScalarModel --chained-to--> EntityModel
 * ScalarModel --provides--> ManagedObject <--provides-- ManagedValue
 * </pre>
 */
//@Log4j2
public abstract class ScalarModel
extends ChainingModel<ManagedObject>
implements HasRenderingHints, ScalarUiModel, LinksProvider, FormExecutorContext {

    private static final long serialVersionUID = 1L;

    private enum EitherParamOrProp {
        PROPERTY,
        PARAMETER;
    }

    @Getter @NonNull private final EitherParamOrProp paramOrProp;
    public boolean isProperty() { return paramOrProp == EitherParamOrProp.PROPERTY; }
    public boolean isParameter() { return paramOrProp == EitherParamOrProp.PARAMETER; }

    private final EntityModel parentEntityModel;

    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private ScalarRepresentation mode;

    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private RenderingHint renderingHint;

    /**
     * Creates a model representing an action parameter of an action of a parent
     * object, with the {@link #getObject() value of this model} to be default
     * value (if any) of that action parameter.
     */
    protected ScalarModel(
            final EntityModel parentEntityModel) {
        this(EitherParamOrProp.PARAMETER,
                parentEntityModel, ScalarRepresentation.EDITING, RenderingHint.REGULAR);
    }

    /**
     * Creates a model representing a property of a parent object, with the
     * {@link #getObject() value of this model} to be current value of the
     * property.
     */
    protected ScalarModel(
            final EntityModel parentEntityModel,
            final ScalarRepresentation viewOrEdit,
            final ObjectUiModel.RenderingHint renderingHint) {
        this(EitherParamOrProp.PROPERTY,
                parentEntityModel, viewOrEdit, renderingHint);
    }

    private ScalarModel(
            final @NonNull EitherParamOrProp paramOrProp,
            final @NonNull EntityModel parentEntityModel,
            final @NonNull ScalarRepresentation viewOrEdit,
            final @NonNull ObjectUiModel.RenderingHint renderingHint) {

        super(parentEntityModel); // the so called target model, we are chaining us to
        this.paramOrProp = paramOrProp;
        this.parentEntityModel = parentEntityModel;
        this.mode = viewOrEdit;
        this.renderingHint = renderingHint;
    }

    /**
     * Gets the proposed value as ManagedObject.
     * (override, so we don't return the target model, we are chained to)
     */
    @Override
    public final ManagedObject getObject() {
        if(isCurrentValueAbsent()) {
            return ManagedObject.empty(proposedValue().getElementType());
        }
        return proposedValue().getValue().getValue();
    }

    /**
     * Sets given ManagedObject as new proposed value.
     * (override, so we don't return the target model, we are chained to)
     */
    @Override
    public final void setObject(final ManagedObject newValue) {

        _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
            _Debug.log("[PENDING MODEL] about to set new value: %s", newValue==null?"null":newValue.getPojo());
        });

        proposedValue().getValue().setValue(newValue);

        _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
            _Debug.log("[PENDING MODEL] new value set to property");
        });
    }

    @Override
    public final EntityModel getParentUiModel() {
        return parentEntityModel;
    }

    @Override
    public final ManagedObject getOwner() {
        return getParentUiModel().getObject();
    }

    public final boolean isEmpty() {
        return ManagedObjects.isNullOrUnspecifiedOrEmpty(proposedValue().getValue().getValue());
    }

    public final boolean isScalarTypeAnyOf(final Class<?>... requiredClass) {
        final String fullName = getScalarTypeSpec().getFullIdentifier();
        return _NullSafe.stream(requiredClass)
                .map(Class::getName)
                .anyMatch(fullName::equals);
    }

    public final boolean isScalarTypeSubtypeOf(final Class<?> requiredClass) {
        final Class<?> scalarType = getScalarTypeSpec().getCorrespondingClass();
        return _NullSafe.streamNullable(requiredClass)
                .anyMatch(x -> x.isAssignableFrom(scalarType));
    }

    /**
     * Has no meaning for <i>Parameters</i>, only has meaning for <i>Properties</i>.
     * For a <i>Property</i>
     * the current value and the initial pending (negotiated) value might differ,
     * if the current value is absent (null).
     * For <i>Parameters</i> we always evaluate {@code isCurrentValueAbsent()}
     * to false.
     */
    public boolean isCurrentValueAbsent() {
        val proposedValue = proposedValue();
        return proposedValue.isCurrentValueAbsent().booleanValue();
    }

    /** get the proposed value, subject to negotiation */
    public String getObjectAsString() {
        val proposedValue = proposedValue();
        return proposedValue.isCurrentValueAbsent().booleanValue()
                ? ""
                : proposedValue.getValueAsParsableText().getValue();
    }

    /**
     * set the proposed value, subject to negotiation, only updates the negotiation model
     * actual application of the proposed value is only applied after passing verification (not done here)
     */
    public void setObjectAsString(final String enteredText) {
        val proposedValue = proposedValue();
        proposedValue.getValueAsParsableText().setValue(enteredText);
    }

    public abstract ManagedValue proposedValue();

    public abstract boolean whetherHidden();

    public abstract String disableReasonIfAny();

    public abstract String validate(ManagedObject proposedAdapter);

    public boolean isRequired() {
        return !getMetaModel().isOptional();
    }

    public abstract String getCssClass();

    public final <T extends Facet> T getFacet(final Class<T> facetType) {
        return getMetaModel().getFacet(facetType);
    }

    public final <T extends Facet> Optional<T> lookupFacet(final Class<T> facetType) {
        return getMetaModel().lookupFacet(facetType);
    }

    /**
     * Additional links to render (if any)
     */
    private List<LinkAndLabel> linkAndLabels = _Lists.newArrayList();

    @Override
    public Can<LinkAndLabel> getLinks() {
        return Can.ofCollection(linkAndLabels);
    }

    @Override
    public final PromptStyle getPromptStyle() {
        final PromptStyleFacet facet = getFacet(PromptStyleFacet.class);
        if(facet == null) {
            // don't think this can happen actually, see PromptStyleFacetFallback
            return PromptStyle.INLINE;
        }
        PromptStyle promptStyle = facet.value();
        if (promptStyle == PromptStyle.AS_CONFIGURED) {
            // I don't think this can happen, actually...
            // when the metamodel is built, it should replace AS_CONFIGURED with one of the other prompts
            // (see PromptStyleConfiguration and PromptStyleFacetFallback)
            return PromptStyle.INLINE;
        }
        return promptStyle;
    }

    public boolean canEnterEditMode() {
        boolean editable = isEnabled();
        return editable && isViewMode();
    }

    public boolean isEnabled() {
        return disableReasonIfAny() == null;
    }

    // //////////////////////////////////////

    private InlinePromptContext inlinePromptContext;

    /**
     * Further hint, to support inline prompts...
     */
    @Override
    public InlinePromptContext getInlinePromptContext() {
        return inlinePromptContext;
    }

    public void setInlinePromptContext(final InlinePromptContext inlinePromptContext) {
        if (this.inlinePromptContext != null) {
            // otherwise the components created for an property edit inline prompt will overwrite the original
            // components on the underlying page (which we go back to if the prompt is cancelled).
            return;
        }
        this.inlinePromptContext = inlinePromptContext;
    }

    protected transient AssociatedActions associatedActions;

    public static class AssociatedActions {
        @Getter private final Optional<ObjectAction> firstAssociatedWithInlineAsIfEdit;
        @Getter private final List<ObjectAction> remainingAssociated;

        AssociatedActions(final Can<ObjectAction> allAssociated) {
            firstAssociatedWithInlineAsIfEdit = firstAssociatedActionWithInlineAsIfEdit(allAssociated);
            remainingAssociated = firstAssociatedWithInlineAsIfEdit.isPresent()
                    ? allAssociated.remove(firstAssociatedWithInlineAsIfEdit.get()).toList()
                    : allAssociated.toList();
        }

        private static Optional<ObjectAction> firstAssociatedActionWithInlineAsIfEdit(
                final Can<ObjectAction> objectActions) {
            return objectActions.stream()
            .filter(act->ObjectAction.Util.promptStyleFor(act).isInlineAsIfEdit())
            .findFirst();
        }
    }


    /**
     * Whether this model should be surfaced in the UI using a widget rendered such that it is either already in
     * edit mode (eg for a parameter), or can be switched into edit mode, eg for an editable property or an
     * associated action of a property with 'inline_as_if_edit'
     *
     * @return <tt>true</tt> if the widget for this model must be editable.
     */
    public boolean mustBeEditable() {
        return getMode() == ScalarRepresentation.EDITING
                || getParamOrProp() == EitherParamOrProp.PARAMETER
                || hasAssociatedActionWithInlineAsIfEdit();
    }

    /**
     * Similar to {@link #mustBeEditable()}, though not called from the same locations.
     *
     * My suspicion is that it amounts to more or less the same set of conditions.
     */
    @Override
    public boolean isInlinePrompt() {
        return (getPromptStyle().isInline() && canEnterEditMode())
                || hasAssociatedActionWithInlineAsIfEdit();
    }

    @Override
    public String toString() {
        return toStringOf();
    }

    protected abstract String toStringOf();

    public abstract ObjectSpecification getScalarTypeSpec();

    public abstract String getIdentifier();

    public final AssociatedActions getAssociatedActions() {
        if (associatedActions == null) {
            associatedActions = new AssociatedActions(calcAssociatedActions());
        }
        return associatedActions;
    }

    protected abstract Can<ObjectAction> calcAssociatedActions();

    public final boolean hasAssociatedActionWithInlineAsIfEdit() {
        return getAssociatedActions().getFirstAssociatedWithInlineAsIfEdit().isPresent();
    }

    public void clearPending() {
        //TODO[ISIS-2871] is this really needed? - remove once we are sure
        // was used to state that pending value is in sync with current value
        //getPendingPropertyModel().getValue().setValue(null);
    }


}

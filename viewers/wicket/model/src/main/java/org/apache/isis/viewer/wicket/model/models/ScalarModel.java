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
import java.util.OptionalInt;

import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.functional.Either;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedValue;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.object.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.util.Facets;
import org.apache.isis.viewer.commons.model.object.UiObject;
import org.apache.isis.viewer.commons.model.object.UiObject.HasRenderingHints;
import org.apache.isis.viewer.commons.model.object.UiObject.RenderingHint;
import org.apache.isis.viewer.commons.model.scalar.UiScalar;
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
implements HasRenderingHints, UiScalar, LinksProvider, FormExecutorContext {

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
            final UiObject.RenderingHint renderingHint) {
        this(EitherParamOrProp.PROPERTY,
                parentEntityModel, viewOrEdit, renderingHint);
    }

    private ScalarModel(
            final @NonNull EitherParamOrProp paramOrProp,
            final @NonNull EntityModel parentEntityModel,
            final @NonNull ScalarRepresentation viewOrEdit,
            final @NonNull UiObject.RenderingHint renderingHint) {

        super(parentEntityModel); // the so called target model, we are chaining us to
        this.paramOrProp = paramOrProp;
        this.parentEntityModel = parentEntityModel;
        this.mode = viewOrEdit;
        this.renderingHint = renderingHint;
    }

    /**
     * This instance is either a {@link ScalarParameterModel} or a {@link ScalarPropertyModel}.
     * <p>
     * Corresponds to the enum {@link #getParamOrProp()}.
     */
    public final Either<ScalarParameterModel, ScalarPropertyModel> getSpecialization() {
        switch(getParamOrProp()) {
        case PARAMETER: return Either.left((ScalarParameterModel) this);
        case PROPERTY: return Either.right((ScalarPropertyModel) this);
        default:
            throw _Exceptions.unmatchedCase(getParamOrProp());
        }
    }


    public <T> IModel<T> unwrapped(final Class<T> type) {
        return new ScalarUnwrappingModel<T>(type, this);
    }

    /**
     * Gets the proposed value as ManagedObject.
     * (override, so we don't return the target model, we are chained to)
     */
    @Override
    public final ManagedObject getObject() {
        val proposedValue = proposedValue();
        return ManagedObjects.nullToEmpty(
                proposedValue.getElementType(),
                proposedValue.getValue().getValue());
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

    public final boolean isScalarTypeAnyOf(final Can<Class<?>> requiredClasses) {
        final String fullName = getScalarTypeSpec().getFullIdentifier();
        return requiredClasses.stream()
                .map(Class::getName)
                .anyMatch(fullName::equals);
    }

    public final boolean isScalarTypeSubtypeOf(final Class<?> requiredClass) {
        final Class<?> scalarType = getScalarTypeSpec().getCorrespondingClass();
        return _NullSafe.streamNullable(requiredClass)
                .anyMatch(x -> x.isAssignableFrom(scalarType));
    }

    public abstract ManagedValue proposedValue();

    public abstract boolean whetherHidden();

    public abstract String disableReasonIfAny();

    public abstract String validate(ManagedObject proposedAdapter);

    public abstract String getCssClass();

    /**
     * Viewers should not use facets directly.
     * However, viewer extensions that provide their own facet types, will have to.
     */
    public final <T extends Facet> boolean containsFacet(final Class<T> facetType) {
        return getMetaModel().containsFacet(facetType);
    }

    /**
     * Viewers should not use facets directly.
     * However, viewer extensions that provide their own facet types, will have to.
     */
    public final <T extends Facet> T getFacet(final Class<T> facetType) {
        return getMetaModel().getFacet(facetType);
    }

    /**
     * Viewers should not use facets directly.
     * However, viewer extensions that provide their own facet types, will have to.
     */
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
        return Facets.promptStyleOrElse(getMetaModel(), PromptStyle.INLINE);
    }

    public boolean canEnterEditMode() {
        return isEnabled()
                && isViewMode();
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

    public final OptionalInt multilineNumberOfLines() {
        return Facets.multilineNumberOfLines(getMetaModel());
    }

    public final OptionalInt maxLength() {
        return Facets.maxLength(getScalarTypeSpec());
    }

    public final OptionalInt typicalLength() {
        return Facets.typicalLength(getScalarTypeSpec(), maxLength());
    }

}

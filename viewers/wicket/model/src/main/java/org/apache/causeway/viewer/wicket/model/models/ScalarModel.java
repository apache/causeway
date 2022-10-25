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
package org.apache.causeway.viewer.wicket.model.models;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.debug._Debug;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.core.metamodel.commons.ScalarRepresentation;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedValue;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.hints.HasRenderingHints;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;
import org.apache.causeway.viewer.commons.model.scalar.UiScalar;
import org.apache.causeway.viewer.wicket.model.links.LinkAndLabel;
import org.apache.causeway.viewer.wicket.model.links.LinksProvider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * Represents a scalar of an entity, either a PROPERTY or
 * a PARAMETER.
 *
 * <p>
 * Is the backing model to each of the fields that appear in forms (for entities
 * or action dialogs).
 *
 * @implSpec
 * <pre>
 * ScalarModel --chained-to--> UiObjectWkt
 * ScalarModel --provides--> ManagedObject <--provides-- ManagedValue
 * </pre>
 */
//@Log4j2
public abstract class ScalarModel
extends ChainingModel<ManagedObject>
implements HasRenderingHints, UiScalar, LinksProvider, FormExecutorContext {

    private static final long serialVersionUID = 1L;

    private final UiObjectWkt parentEntityModel;

    @Getter(onMethod_={@Override})
    @Setter(onMethod_={@Override})
    private ScalarRepresentation mode;

    @Getter(onMethod_={@Override})
    private RenderingHint renderingHint;

    /**
     * Creates a model representing an action parameter of an action of a parent
     * object, with the {@link #getObject() value of this model} to be default
     * value (if any) of that action parameter.
     */
    protected ScalarModel(
            final UiObjectWkt parentUiObject) {
        this(parentUiObject, ScalarRepresentation.EDITING, RenderingHint.REGULAR);
    }

    /**
     * Creates a model representing a property of a parent object, with the
     * {@link #getObject() value of this model} to be current value of the
     * property.
     */
    protected ScalarModel(
            final @NonNull UiObjectWkt parentEntityModel,
            final @NonNull ScalarRepresentation viewOrEdit,
            final @NonNull RenderingHint renderingHint) {

        super(parentEntityModel); // the so called target model, we are chaining us to
        this.parentEntityModel = parentEntityModel;
        this.mode = viewOrEdit;
        this.renderingHint = renderingHint;
    }

    /**
     * This instance is either a {@link ScalarParameterModel} or a {@link ScalarPropertyModel}.
     */
    public final Either<ScalarParameterModel, ScalarPropertyModel> getSpecialization() {
        return this.isParameter()
                ? Either.left((ScalarParameterModel) this)
                : Either.right((ScalarPropertyModel) this);
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
    public final UiObjectWkt getParentUiModel() {
        return parentEntityModel;
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

    public abstract String validate(ManagedObject proposedAdapter);

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
                || isParameter()
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

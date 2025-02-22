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
import org.apache.wicket.util.convert.IConverter;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.core.metamodel.commons.ViewOrEditMode;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedValue;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.attrib.UiAttribute;
import org.apache.causeway.viewer.commons.model.hints.HasRenderingHints;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;
import org.apache.causeway.viewer.wicket.model.value.ConverterBasedOnValueSemantics;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.Setter;

/**
 * We refer to both method parameters and instance fields collectively
 * as "attributes" of a class or method.
 * <p>
 * Represents an attribute of a domain object, either a PROPERTY or
 * a PARAMETER. Potentially plural in the latter case.
 * <p>
 * Is also the backing model to each of the fields that appear in forms (for entities
 * or action dialogs).
 *
 * @implSpec
 * <pre>
 * UiAttributeWkt --chained-to--> UiObjectWkt
 * UiAttributeWkt --provides--> ManagedObject <--provides-- ManagedValue
 * </pre>
 * @see UiAttribute
 */
//@Log4j2
public abstract class UiAttributeWkt
extends ChainingModel<ManagedObject>
implements HasRenderingHints, UiAttribute, FormExecutorContext {

    private static final long serialVersionUID = 1L;

    private final UiObjectWkt parentEntityModel;

    @Getter(onMethod_={@Override})
    @Setter(onMethod_={@Override})
    private ViewOrEditMode viewOrEditMode;

    @Getter(onMethod_={@Override})
    private RenderingHint renderingHint;

    /**
     * Creates a model representing an action parameter of an action of a parent
     * object, with the {@link #getObject() value of this model} to be default
     * value (if any) of that action parameter.
     */
    protected UiAttributeWkt(
            final UiObjectWkt parentUiObject,
            final ViewOrEditMode viewOrEdit) {
        this(parentUiObject, viewOrEdit, RenderingHint.REGULAR);
    }

    /**
     * Creates a model representing a property of a parent object, with the
     * {@link #getObject() value of this model} to be current value of the
     * property.
     */
    protected UiAttributeWkt(
            final @NonNull UiObjectWkt parentEntityModel,
            final @NonNull ViewOrEditMode viewOrEdit,
            final @NonNull RenderingHint renderingHint) {
        super(parentEntityModel); // the so called target model, we are chaining us to
        this.parentEntityModel = parentEntityModel;
        this.renderingHint = renderingHint;
        this.viewOrEditMode = viewOrEdit;
    }

    /**
     * This instance is either a {@link ParameterModel} or a {@link PropertyModel}.
     */
    public final Either<ParameterModel, PropertyModel> getSpecialization() {
        return this.isParameter()
                ? Either.left((ParameterModel) this)
                : Either.right((PropertyModel) this);
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
        var proposedValue = proposedValue();
        return ManagedObjects.nullToEmpty(
                proposedValue.getElementType(),
                proposedValue.getValue().getValue());
    }

    /**
     * Sets given ManagedObject as new proposed value.
     * (override, so we don't return the target model, we are chained to)
     * <p>
     * This happens during Wicket's {@code formComponent.updateModel()},
     * which updates the models first, before later calling {@code onUpdate(target)}
     * to repaint those components, that have a changed/dirty model.
     * <p>
     * in other words: changed components get a chance to participate in the partial page update
     * based on whether their models have changed
     */
    @Override
    public final void setObject(final ManagedObject newValue) {
        proposedValue().update(oldValue->{
            _Xray.onSclarModelUpdate(this, oldValue, newValue);
            return newValue;
        });
    }

    @Override
    public final UiObjectWkt getParentUiModel() {
        return parentEntityModel;
    }

    public final boolean isEmpty() {
        return ManagedObjects.isNullOrUnspecifiedOrEmpty(proposedValue().getValue().getValue());
    }

    public abstract ManagedValue proposedValue();

    public abstract String validate(@NonNull ManagedObject proposedAdapter);

    @Override
    public final PromptStyle getPromptStyle() {
        return Facets.promptStyleOrElse(getMetaModel(), PromptStyle.INLINE);
    }

    // -- CONVERSION

    public final <T> Optional<IConverter<T>> getConverter(final Class<T> requiredType) {
        final ObjectFeature propOrParam = getMetaModel();
        return Optional.of(
                new ConverterBasedOnValueSemantics<>(requiredType, propOrParam, getViewOrEditMode()));
    }

    // -- PREDICATES

    @Override
    public final boolean isInlinePrompt() {
        return hasAssociatedActionWithInlineAsIfEdit()
                || (getPromptStyle().isInline()
                        && isViewingMode()
                        && !disabledReason().isPresent());
    }

    /**
     * Whether or not to display some indication that a form field is mandatory.
     * Eg. a star in the UI.
     */
    public final boolean isShowMandatoryIndicator() {
        return isRequired()
                && !disabledReason().isPresent();
    }

    // -- INLINE PROMPT

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

    // -- ASSOCIATED ACTIONS

    public final AssociatedActions getAssociatedActions() {
        if (associatedActions == null) {
            associatedActions = AssociatedActions.of(calcAssociatedActions());
        }
        return associatedActions;
    }

    public final boolean hasAssociatedActionWithInlineAsIfEdit() {
        return getAssociatedActions().firstAssociatedWithInlineAsIfEdit().isPresent();
    }

    protected transient AssociatedActions associatedActions;

    public record AssociatedActions(
            Optional<ObjectAction> firstAssociatedWithInlineAsIfEdit,
            List<ObjectAction> remainingAssociated) {

        static AssociatedActions of(final Can<ObjectAction> allAssociated) {
            Optional<ObjectAction> firstAssociatedWithInlineAsIfEdit = firstAssociatedActionWithInlineAsIfEdit(allAssociated);
            return new AssociatedActions(
                    firstAssociatedWithInlineAsIfEdit,
                    firstAssociatedWithInlineAsIfEdit.isPresent()
                        ? allAssociated.remove(firstAssociatedWithInlineAsIfEdit.get()).toList()
                        : allAssociated.toList());
        }

        private static Optional<ObjectAction> firstAssociatedActionWithInlineAsIfEdit(
                final Can<ObjectAction> objectActions) {
            return objectActions.stream()
                .filter(act->ObjectAction.Util.promptStyleFor(act).isInlineAsIfEdit())
                .findFirst();
        }
    }

    protected abstract Can<ObjectAction> calcAssociatedActions();

    // --

    public final OptionalInt multilineNumberOfLines() {
        return Facets.multilineNumberOfLines(getMetaModel());
    }

    public final OptionalInt maxLength() {
        return Facets.maxLength(getElementType());
    }

    public final OptionalInt typicalLength() {
        return Facets.typicalLength(getElementType(), maxLength());
    }

    // -- TO STRING

    @Override
    public String toString() {
        return toStringOf();
    }

    protected abstract String toStringOf();

}

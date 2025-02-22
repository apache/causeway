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
package org.apache.causeway.viewer.wicket.ui.components.attributes;

import java.util.Collection;
import java.util.Optional;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;

import lombok.experimental.UtilityClass;

@UtilityClass
class _Util {

    /**
     * Whether to prevent tabbing into non-editable widgets.
     */
    boolean isPropertyWithEnterEditNotAvailable(final UiAttributeWkt attributeModel) {
        return attributeModel.isProperty()
                && attributeModel.isViewingMode()
                && (attributeModel.getPromptStyle().isDialogAny()
                        || !canEnterEditMode(attributeModel));
    }

    boolean canPropertyEnterInlineEditDirectly(final UiAttributeWkt attributeModel) {
        return attributeModel.getPromptStyle().isInline()
                && attributeModel.isViewingMode()
                && !attributeModel.disabledReason().isPresent();
    }

    /**
     * Parameter disabled case should already be handled earlier.
     * <p>
     * @implNote {@code !attributeModel.disabledReason().isPresent()} is not checked nor asserted here
     */
    boolean canParameterEnterNestedEdit(final UiAttributeWkt attributeModel) {
        return attributeModel.isParameter()
                && !attributeModel.hasChoices() // handled by select2 panels instead
                && lookupCompositeValueMixinForFeature(attributeModel).isPresent();
    }

    Optional<ActionModel> lookupMixinForCompositeValueUpdate(final UiAttributeWkt attributeModel) {
        return lookupCompositeValueMixinForFeature(attributeModel)
            .flatMap(compositeValueMixinForFeature->
                toActionModelWithRuleChecking(compositeValueMixinForFeature, attributeModel))
            .filter(_Util::guardAgainstInvalidCompositeMixinScenarios);
    }

    Optional<ActionModel> lookupPropertyActionForInlineEdit(final UiAttributeWkt attributeModel) {
        // not editable property, but maybe one of the actions is.
        return attributeModel.getAssociatedActions()
                .firstAssociatedWithInlineAsIfEdit()
                .flatMap(action->toActionModelWithRuleChecking(action, attributeModel));
    }

    Can<ActionModel> associatedActionModels(final UiAttributeWkt attributeModel) {
        // find associated actions for this scalar property (only properties will have any.)
        // convert those actions into UI layer widgets
        return attributeModel.getAssociatedActions()
                .remainingAssociated()
                .stream()
                .map(act->ActionModel.forPropertyOrParameter(act, attributeModel))
                .collect(Can.toCan());
    }

    IValidator<Object> createValidatorFor(final UiAttributeWkt attributeModel) {
        return new IValidator<Object>() {
            private static final long serialVersionUID = 1L;
            @Override
            public void validate(final IValidatable<Object> validatable) {
                recoverProposedValue(validatable.getValue(), attributeModel)
                .ifPresent(proposedAdapter->{
                    _Strings.nonEmpty(attributeModel.validate(proposedAdapter))
                    .ifPresent(validationFeedback->
                        validatable.error(new ValidationError(validationFeedback)));
                });
            }
        };
    }

    // -- HELPER

    private boolean canEnterEditMode(final UiAttributeWkt attributeModel) {
        return attributeModel.isViewingMode()
                && !attributeModel.disabledReason().isPresent();
    }

    private Optional<ActionModel> toActionModelNoRuleChecking(
            final @Nullable ObjectAction action,
            final UiAttributeWkt attributeModel) {
        return Optional.ofNullable(action)
        .map(act->ActionModel.forPropertyOrParameter(act, attributeModel));
    }

    private Optional<ActionModel> toActionModelWithRuleChecking(
            final @Nullable ObjectAction action,
            final UiAttributeWkt attributeModel) {
        return toActionModelNoRuleChecking(action, attributeModel)
        .filter(ActionModel::isVisible)
        .filter(ActionModel::isEnabled);
    }

    private boolean guardAgainstInvalidCompositeMixinScenarios(final ActionModel actionModel) {
        _Assert.assertNotNull(actionModel.getInlinePromptContext(), ()->String.format(
                "with feature %s, "
                + "for composite-value-type mixins to work an InlinePromptContext is required",
                actionModel.getAction().getFeatureIdentifier()));
        _Assert.assertTrue(actionModel.getPromptStyle()==PromptStyle.INLINE_AS_IF_EDIT, ()->String.format(
                "with feature %s, "
                + "for composite-value-type mixins to work PromptStyle must be INLINE_AS_IF_EDIT "
                + "yet found %s",
                actionModel.getAction().getFeatureIdentifier(),
                actionModel.getPromptStyle()));
        return true;
    }

    //XXX its rather unfortunate, that this method has to deal with 4 different cases
    private Optional<ManagedObject> recoverProposedValue(
            final Object valueObject,
            final UiAttributeWkt attributeModel){

        if(valueObject instanceof Collection) {

            if(attributeModel.isSingular()) {
                // seeing this code-path with FileUpload being wrapped in an ArrayList of size 1
                // as a more general rule of thumb, use the first element in the ArrayList if present
                var unpackedValue = ((Collection<?>)valueObject).stream()
                        .limit(1)
                        .map(v->attributeModel.getObjectManager()
                                .adapt(valueObject))
                        .findFirst();
                return unpackedValue;
            }

            var unpackedValues = ((Collection<?>)valueObject).stream()
            .map(v->attributeModel
                    .getObjectManager().demementify((ObjectMemento)v))
            .collect(Can.toCan());
            return Optional.of(ManagedObject.packed(attributeModel.getElementType(), unpackedValues));
        }

        if(valueObject instanceof ObjectMemento) {
            // seeing this code-path particularly with enum choices
            return Optional.ofNullable(
                    attributeModel
                        .getObjectManager().demementify((ObjectMemento)valueObject));
        }

        return Optional.ofNullable(
                    attributeModel
                        .getObjectManager()
                        .adapt(valueObject));
    }

    private Optional<ObjectAction> lookupCompositeValueMixinForFeature(final UiAttributeWkt attributeModel) {
        var spec = attributeModel.getElementType();
        if(!spec.isValue()) {
            return Optional.empty();
        }
        return attributeModel.getSpecialization().<Optional<ObjectAction>>fold(
                param->
                    Facets.valueCompositeMixinForParameter(
                            attributeModel.getMetaModel(),
                            param.getParameterNegotiationModel(), param.getParameterIndex()),
                prop->
                    Facets.valueCompositeMixinForProperty(
                            attributeModel.getMetaModel(),
                            prop.getManagedProperty()));
    }

}

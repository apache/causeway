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
package org.apache.causeway.viewer.wicket.ui.components.scalars;

import java.util.Collection;
import java.util.Optional;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.wicket.model.links.LinkAndLabel;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelFactory;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _Util {

    boolean canPropertyEnterInlineEditDirectly(final ScalarModel scalarModel) {
        return scalarModel.getPromptStyle().isInline()
                && scalarModel.canEnterEditMode();
    }

    boolean canParameterEnterNestedEdit(final ScalarModel scalarModel) {
        return scalarModel.isParameter()
                && !scalarModel.hasChoices() // handled by select2 panels instead
                && lookupCompositeValueMixinForFeature(scalarModel).isPresent();
    }

    Optional<LinkAndLabel> lookupMixinForCompositeValueUpdate(final ScalarModel scalarModel) {
        return lookupCompositeValueMixinForFeature(scalarModel)
            .flatMap(compositeValueMixinForFeature->
                toLinkAndLabelWithRuleChecking(compositeValueMixinForFeature, scalarModel))
            .filter(_Util::guardAgainstInvalidCompositeMixinScenarios);
    }

    Optional<LinkAndLabel> lookupPropertyActionForInlineEdit(final ScalarModel scalarModel) {
        // not editable property, but maybe one of the actions is.
        return scalarModel.getAssociatedActions()
                .getFirstAssociatedWithInlineAsIfEdit()
                .flatMap(action->toLinkAndLabelWithRuleChecking(action, scalarModel));
    }

    Can<LinkAndLabel> associatedLinksAndLabels(final ScalarModel scalarModel) {
        // find associated actions for this scalar property (only properties will have any.)
        // convert those actions into UI layer widgets
        return scalarModel.getAssociatedActions()
                .getRemainingAssociated()
                .stream()
                .map(LinkAndLabelFactory.forPropertyOrParameter(scalarModel))
                .collect(Can.toCan());
    }

    private Optional<LinkAndLabel> toLinkAndLabelNoRuleChecking(
            final @Nullable ObjectAction action,
            final ScalarModel scalarModel) {
        return Optional.ofNullable(action)
        .map(LinkAndLabelFactory.forPropertyOrParameter(scalarModel));
    }

    private Optional<LinkAndLabel> toLinkAndLabelWithRuleChecking(
            final @Nullable ObjectAction action,
            final ScalarModel scalarModel) {
        return toLinkAndLabelNoRuleChecking(action, scalarModel)
        .filter(LinkAndLabel::isVisible)
        .filter(LinkAndLabel::isEnabled);
    }

    IValidator<Object> createValidatorFor(final ScalarModel scalarModel) {
        return new IValidator<Object>() {
            private static final long serialVersionUID = 1L;
            @Override
            public void validate(final IValidatable<Object> validatable) {
                recoverProposedValue(validatable.getValue(), scalarModel)
                .ifPresent(proposedAdapter->{
                    _Strings.nonEmpty(scalarModel.validate(proposedAdapter))
                    .ifPresent(validationFeedback->
                        validatable.error(new ValidationError(validationFeedback)));
                });
            }
        };
    }

    private boolean guardAgainstInvalidCompositeMixinScenarios(final LinkAndLabel linkAndLabel) {
        return guardAgainstInvalidCompositeMixinScenarios(linkAndLabel.getActionModel());
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
            final ScalarModel scalarModel){

        if(valueObject instanceof Collection) {

            if(scalarModel.isSingular()) {
                // seeing this code-path with FileUpload being wrapped in an ArrayList of size 1
                // as a more general rule of thumb, use the first element in the ArrayList if present
                val unpackedValue = ((Collection<?>)valueObject).stream()
                        .limit(1)
                        .map(v->scalarModel.getObjectManager()
                                .adapt(valueObject))
                        .findFirst();
                return unpackedValue;
            }

            val unpackedValues = ((Collection<?>)valueObject).stream()
            .map(v->scalarModel
                    .getObjectManager().demementify((ObjectMemento)v))
            .collect(Can.toCan());
            return Optional.of(ManagedObject.packed(scalarModel.getScalarTypeSpec(), unpackedValues));
        }

        if(valueObject instanceof ObjectMemento) {
            // seeing this code-path particularly with enum choices
            return Optional.ofNullable(
                    scalarModel
                        .getObjectManager().demementify((ObjectMemento)valueObject));
        }

        return Optional.ofNullable(
                    scalarModel
                        .getObjectManager()
                        .adapt(valueObject));
    }


    // -- HELPER

    private Optional<ObjectAction> lookupCompositeValueMixinForFeature(final ScalarModel scalarModel) {
        val spec = scalarModel.getScalarTypeSpec();
        if(!spec.isValue()) {
            return Optional.empty();
        }
        return scalarModel.getSpecialization().<Optional<ObjectAction>>fold(
                param->
                    Facets.valueCompositeMixinForParameter(
                            scalarModel.getMetaModel(),
                            param.getParameterNegotiationModel(), param.getParameterIndex()),
                prop->
                    Facets.valueCompositeMixinForProperty(
                            scalarModel.getMetaModel(),
                            prop.getManagedProperty()));
    }

}

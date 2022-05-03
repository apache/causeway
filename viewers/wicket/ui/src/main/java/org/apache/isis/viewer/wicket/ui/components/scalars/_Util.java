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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.Optional;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.util.Facets;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelFactory;

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
                recoverProposedValue(validatable, scalarModel)
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

    private Optional<ManagedObject> recoverProposedValue(
            final IValidatable<Object> validatable,
            final ScalarModel scalarModel){

        return Optional.ofNullable(
                    scalarModel
                        .getObjectManager()
                        .adapt(validatable.getValue()));
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

    // -- PROBABLY NO LONGER NEEDED

//    private Optional<ManagedObject> recoverProposedValue2(
//            final IValidatable<Object> validatable,
//            final ScalarModel scalarModel){
//        return mementoForProposedValue(validatable, scalarModel)
//                .map(scalarModel.getCommonContext()::reconstructObject);
//    }
//
//    private Optional<ObjectMemento> mementoForProposedValue(
//            final IValidatable<Object> validatable,
//            final ScalarModel scalarModel) {
//        final Object proposedValueObj = validatable.getValue();
//
//        if (proposedValueObj instanceof List) {
//            @SuppressWarnings("unchecked")
//            val proposedValueObjAsList = (List<ObjectMemento>) proposedValueObj;
//            if (proposedValueObjAsList.isEmpty()) {
//                return Optional.empty();
//            }
//            val memento = proposedValueObjAsList.get(0);
//            val logicalType = memento.getLogicalType();
//            return Optional.of(ObjectMemento.pack(proposedValueObjAsList, logicalType));
//        } else {
//            return Optional.of((ObjectMemento) proposedValueObj);
//        }
//
//    }

}

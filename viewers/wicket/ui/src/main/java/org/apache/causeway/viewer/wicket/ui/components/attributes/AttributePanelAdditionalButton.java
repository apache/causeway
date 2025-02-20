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

import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.FieldFragment;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributePanel.RenderScenario;

/**
 * In order of appearance in the UI.
 * XXX refactoring hint: whether or not buttons are visible should be answered by the scalar-model (or meta-model) itself
 */
enum AttributePanelAdditionalButton {

    DISABLED_REASON {
        @Override
        boolean isVisible(
                final UiAttributeWkt attributeModel,
                final RenderScenario renderScenario,
                final FieldFragment fieldFragment) {
            var precondition = renderScenario!=RenderScenario.CAN_EDIT_INLINE_VIA_ACTION;
            return precondition
                    && attributeModel.disabledReason()
                        .map(InteractionVeto::vetoConsent)
                        .flatMap(Consent::getReason)
                        .map(VetoReason::uiHint)
                        .map(VetoReason.UiHint::isShowBanIcon)
                        .orElse(false);
        }
    },
    DISABLED_REASON_PROTOTYPING {
        @Override
        boolean isVisible(
                final UiAttributeWkt attributeModel,
                final RenderScenario renderScenario,
                final FieldFragment fieldFragment) {
            var precondition = renderScenario!=RenderScenario.CAN_EDIT_INLINE_VIA_ACTION
                    && attributeModel.getSystemEnvironment().isPrototyping()
                    && attributeModel.getConfiguration().getViewer().getWicket().isDisableReasonExplanationInPrototypingModeEnabled();
            return precondition
                    && attributeModel.disabledReason()
                        .map(InteractionVeto::vetoConsent)
                        .flatMap(Consent::getReason)
                        .map(VetoReason::uiHint)
                        // opposite of logic in DISABLED_REASON above,
                        // because DISABLED_REASON_PROTOTYPING should only ever activate when DISABLED_REASON is not active
                        .map(VetoReason.UiHint::isNoIconUnlessPrototying)
                        .orElse(false);
        }
    },
    CLEAR_FIELD {
        @Override
        boolean isVisible(
                final UiAttributeWkt attributeModel,
                final RenderScenario renderScenario,
                final FieldFragment fieldFragment) {

            // check some preconditions
            switch (fieldFragment) {
            case LINK_TO_PROMT:
                if(renderScenario==RenderScenario.CAN_EDIT_INLINE_VIA_ACTION) {
                    return false;
                }
                break; // else fall through
            case NO_LINK_VIEWING:
                return false;
            case NO_LINK_EDITING:
                // fall through
            default:
                // fall through
            }

            // hide if editing is vetoed
            if(attributeModel.disabledReason().isPresent()) {
                return false;
            }

            // visible only if feature is not required and not already cleared
            return attributeModel.getConfiguration().getViewer().getWicket().isClearFieldButtonEnabled()
                    && !attributeModel.isRequired()
                    && attributeModel.proposedValue().isPresent();

        }
    },
    COPY_TO_CLIPBOARD {
        @Override
        boolean isVisible(
                final UiAttributeWkt attributeModel,
                final RenderScenario renderScenario,
                final FieldFragment fieldFragment) {
            //XXX Future extension
            return false;
        }
    },
    ;

    abstract boolean isVisible(
            UiAttributeWkt attributeModel, RenderScenario renderScenario, FieldFragment fieldFragment);

}

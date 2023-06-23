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
package org.apache.causeway.viewer.wicket.ui.components.pops;

import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.viewer.wicket.model.models.PopModel;
import org.apache.causeway.viewer.wicket.ui.components.pops.ScalarFragmentFactory.FieldFragement;
import org.apache.causeway.viewer.wicket.ui.components.pops.ScalarPanelAbstract.RenderScenario;

/**
 * In order of appearance in the UI.
 * XXX refactoring hint: whether or not buttons are visible should be answered by the scalar-model (or meta-model) itself
 */
enum ScalarPanelAdditionalButton {

    DISABLED_REASON {
        @Override
        boolean isVisible(
                final PopModel popModel,
                final RenderScenario renderScenario,
                final FieldFragement fieldFragement) {
            return renderScenario!=RenderScenario.CAN_EDIT_INLINE_VIA_ACTION
                    && popModel.disabledReason()
                    .map(InteractionVeto::getVetoConsent)
                    .flatMap(Consent::getReason)
                    .map(VetoReason::showInUi)
                    .orElse(false);
        }
    },
    DISABLED_REASON_PROTOTYPING {
        @Override
        boolean isVisible(
                final PopModel popModel,
                final RenderScenario renderScenario,
                final FieldFragement fieldFragement) {
            return popModel.getSystemEnvironment().isPrototyping()
                    && popModel.getConfiguration().getViewer().getWicket().isDisableReasonExplanationInPrototypingModeEnabled()
                    && renderScenario!=RenderScenario.CAN_EDIT_INLINE_VIA_ACTION
                    && popModel.disabledReason()
                    .map(InteractionVeto::getVetoConsent)
                    .flatMap(Consent::getReason)
                    .map(vetoReason->!vetoReason.showInUi())
                    .orElse(false);
        }
    },
    CLEAR_FIELD {
        @Override
        boolean isVisible(
                final PopModel popModel,
                final RenderScenario renderScenario,
                final FieldFragement fieldFragement) {

            // check some preconditions
            switch (fieldFragement) {
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

            // visible only if feature is not required and not already cleared
            return popModel.getConfiguration().getViewer().getWicket().isClearFieldButtonEnabled()
                    && !popModel.isRequired()
                    && popModel.proposedValue().isPresent();

        }
    },
    COPY_TO_CLIPBOARD {
        @Override
        boolean isVisible(
                final PopModel popModel,
                final RenderScenario renderScenario,
                final FieldFragement fieldFragement) {
            //XXX Future extension
            return false;
        }
    },
    ;

    abstract boolean isVisible(
            PopModel popModel, RenderScenario renderScenario, FieldFragement fieldFragement);

}

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
package org.apache.causeway.viewer.wicket.ui.components.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.ui.observation.WicketRenderObservationBehavior;

class ActionPromptObservationTest {
    @Test void mainPromptPanelAttachesOneBehavior() {
        var tester = new WicketTester();
        try {
            var model = mock(ActionModel.class, RETURNS_DEEP_STUBS);
            when(model.getAction().getFeatureIdentifier()).thenReturn(Identifier.actionIdentifier(
                    LogicalType.eager(Object.class, "demo.Customer"), "updateName", String.class));
            var panel = new ActionParametersPanel("prompt", model, true);
            assertEquals(1, panel.getBehaviors(WicketRenderObservationBehavior.class).size());
        } finally { tester.destroy(); }
    }
}

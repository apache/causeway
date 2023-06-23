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

import java.util.stream.Collectors;

import org.apache.causeway.commons.internal.debug._XrayEvent;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.core.metamodel.object.MmDebugUtils;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;

import lombok.val;

class _Xray {

    static void onParamOrPropertyEdited(final PopPanelAbstract popPanel) {
        if(!XrayUi.isXrayEnabled()) return;

        popPanel.popModel().getSpecialization()
        .accept(
            param->{
                val data = MmDebugUtils
                        .paramUpdateDataFor(param.getParameterIndex(), param.getParameterNegotiationModel());
                _XrayEvent.event("Form Component (Parameter) update %s", data.formatted());
            },
            prop->{
                val data = MmDebugUtils
                        .propUpdateDataFor(prop.getPendingPropertyModel());
                _XrayEvent.event("Form Component (Property) update: %s", data.formatted());
            });
    }

    public static void debugRequestParams() {
        if(!XrayUi.isXrayEnabled()) return;

        val requestArgs = PageParameterUtils.streamCurrentRequestParameters()
                .map(pair->String.format("%s->%s", pair.getKey(), pair.getValue()))
                .collect(Collectors.joining(",\n"));

        _XrayEvent.event("Form Component Change Event %s%n", requestArgs);
    }

}

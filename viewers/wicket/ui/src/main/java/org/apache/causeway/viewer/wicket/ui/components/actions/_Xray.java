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

import org.apache.causeway.commons.internal.debug._XrayEvent;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.MmDebugUtil;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
final class _Xray {

    void beforeParamFormUpdate(
            final int paramIndex,
            final ParameterNegotiationModel paramNegotiationModel) {
        if(!XrayUi.isXrayEnabled()) return;

        val data = MmDebugUtil
                .paramUpdateDataFor(paramIndex, paramNegotiationModel);
        _XrayEvent.event("Param Form - about to update: %s", data.formatted());
    }

    void reassessedDefault(
            final int paramIndex,
            final ParameterNegotiationModel paramNegotiationModel) {
        if(!XrayUi.isXrayEnabled()) return;

        val data = MmDebugUtil
                .paramUpdateDataFor(paramIndex, paramNegotiationModel);
        _XrayEvent.event("Param Form - param default[%d] reassessed: %s", paramIndex, data.formatted());
    }

    void afterParamFormUpdate(
            final int paramIndex,
            final ParameterNegotiationModel paramNegotiationModel) {
        if(!XrayUi.isXrayEnabled()) return;

        val data = MmDebugUtil
                .paramUpdateDataFor(paramIndex, paramNegotiationModel);
        _XrayEvent.event("Param Form - updated: %s", data.formatted());
    }

}

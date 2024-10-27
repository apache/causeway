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

import java.util.Objects;

import org.apache.causeway.commons.internal.debug._XrayEvent;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
final class _Xray {

    static void onSclarModelUpdate(
            final ScalarModel scalarModel, final ManagedObject oldValue, final ManagedObject newValue) {
        if(!XrayUi.isXrayEnabled()) return;

        var oldPojo = MmUnwrapUtils.single(oldValue);
        var newPojo = MmUnwrapUtils.single(newValue);
        var changed = !Objects.equals(oldPojo, newPojo);

        final String updatingWhat = scalarModel.getSpecialization()
                .fold(
                    param->
                        String.format("param[index=%d,name=%s,changed=%b]",
                                param.getParameterIndex(), param.getFriendlyName(), changed)
                    ,
                    prop->
                        String.format("prop[name=%s,changed=%b]",
                                prop.getFriendlyName(), changed)
                    );

        _XrayEvent.user("%s Model - updating %s: %s -> %s",
                scalarModel.isParameter() ? "Parameter" : "Property",
                updatingWhat,
                oldPojo, newPojo);
    }

}

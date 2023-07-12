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

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.commons.model.hints.HasRenderingHints;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;

public interface ObjectAdapterModel
extends
    HasCommonContext,
    HasRenderingHints,
    IModel<ManagedObject> {

    /**
     * Used as a hint when the {@link #getRenderingHint()} is {@link RenderingHint#PARENTED_TITLE_COLUMN}.
     * Returns whether given {@code other} equals the context adapter (if any) in which case
     * the title can be shortened.
     */
    boolean isContextAdapter(ManagedObject other);

    ObjectSpecification getTypeOfSpecification();

    public default PageParameters getPageParameters() {
        //FIXME
//        return _HintPageParameterSerializer
//                .hintStoreToPageParameters(getPageParametersWithoutUiHints(), scalarModel);
        return getPageParametersWithoutUiHints();
    }

    public default PageParameters getPageParametersWithoutUiHints() {
        return PageParameterUtils.createPageParametersForObject(getObject());
    }

    boolean isHollow(); //TODO[CAUSEWAY-3522] debugging
    void refreshIfHollow(); //TODO[CAUSEWAY-3522] debugging

}

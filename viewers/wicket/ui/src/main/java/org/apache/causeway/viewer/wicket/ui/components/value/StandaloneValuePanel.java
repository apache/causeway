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
package org.apache.causeway.viewer.wicket.ui.components.value;

import java.net.URL;
import java.util.UUID;

import org.apache.causeway.applib.services.bookmark.idstringifiers.PredefinedSerializables;
import org.apache.causeway.applib.value.LocalResourcePath;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmRenderUtil;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.wicket.model.models.ValueModel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.val;

/**
 * Panel for rendering any value types that do not have their own custom
 * {@link ScalarPanelAbstract panel} to render them.
 */
public class StandaloneValuePanel
extends PanelAbstract<ManagedObject, ValueModel> {

    private static final long serialVersionUID = 1L;
    private static final String ID_STANDALONE_VALUE = "standaloneValue";

    public StandaloneValuePanel(final String id, final ValueModel valueModel) {
        super(id, valueModel);

        //XXX StandaloneValuePanel has its limitations compared to the ScalarPanel infrastructure,
        // which has far better rendering support
        // (we probably need to remove StandaloneValuePanel and utilize the ScalarPanel for standalone values instead)
        if(isProbablySimpleInlineHtml(valueModel.getObjectMember().getElementType())) {
            Wkt.markupAdd(this, ID_STANDALONE_VALUE, ()->
                MmRenderUtil.htmlStringForValueType(getModel().getObject(), getModel().getObjectMember())
            );
        } else {
            // resort to (textual) title rendering
            Wkt.labelAdd(this, ID_STANDALONE_VALUE, ()->
                getModel().getObject().getTitle());
        }
    }

    // -- HELPER

    private boolean isProbablySimpleInlineHtml(final ObjectSpecification valueSpec) {
        val cls = valueSpec.getCorrespondingClass();

        return PredefinedSerializables.isPredefinedSerializable(cls)
                || UUID.class.equals(cls)
                || URL.class.equals(cls)
                || LocalResourcePath.class.equals(cls);
    }


}

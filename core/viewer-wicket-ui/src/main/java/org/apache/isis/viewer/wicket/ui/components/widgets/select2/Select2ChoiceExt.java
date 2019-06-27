/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.widgets.select2;

import org.apache.wicket.model.IModel;
import org.wicketstuff.select2.Select2Choice;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.runtime.memento.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.EmptyChoiceProvider;

public class Select2ChoiceExt extends Select2Choice<ObjectAdapterMemento> implements ChoiceExt {

    public static Select2ChoiceExt create(
            final String id,
            final IModel<ObjectAdapterMemento> modelObject,
            final ScalarModel scalarModel) {
        return new Select2ChoiceExt(id, modelObject, scalarModel);
    }

    private final ObjectSpecId specId;

    private Select2ChoiceExt(
            final String id,
            final IModel<ObjectAdapterMemento> model,
            final ScalarModel scalarModel) {
        super(id, model, EmptyChoiceProvider.INSTANCE);
        specId = scalarModel.getTypeOfSpecification().getSpecId();

        getSettings().setCloseOnSelect(true);

        setOutputMarkupPlaceholderTag(true);
    }

    @Override
    public ObjectSpecId getSpecId() {
        return specId;
    }
}

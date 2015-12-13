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

import java.util.Collection;

import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONWriter;
import org.apache.wicket.model.IModel;
import org.wicketstuff.select2.ChoiceProvider;
import org.wicketstuff.select2.Response;
import org.wicketstuff.select2.Select2Choice;

import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

public final class Select2ChoiceUtil  {

    private Select2ChoiceUtil(){}

    public static Select2Choice<ObjectAdapterMemento> newSelect2Choice(String id, final IModel<ObjectAdapterMemento> modelObject, ScalarModel scalarModel) {
        Select2Choice<ObjectAdapterMemento> select2Choice = new Select2Choice<>(id, modelObject, EmptyChoiceProvider.INSTANCE);
        select2Choice.setRequired(scalarModel.isRequired());
        select2Choice.setOutputMarkupPlaceholderTag(true);
        return select2Choice;
    }

    private static class EmptyChoiceProvider extends ChoiceProvider<ObjectAdapterMemento> {

        private static final EmptyChoiceProvider INSTANCE = new EmptyChoiceProvider();

        @Override
        public String getDisplayValue(ObjectAdapterMemento object) {
            return null;
        }

        @Override
        public String getIdValue(ObjectAdapterMemento object) {
            return null;
        }

        @Override
        public void query(String term, int page, Response<ObjectAdapterMemento> response) {

        }

        @Override
        public void toJson(ObjectAdapterMemento choice, JSONWriter writer) throws JSONException {

        }

        @Override
        public Collection<ObjectAdapterMemento> toChoices(Collection<String> ids) {
            return null;
        }

        @Override
        public void detach() {

        }
    }
}

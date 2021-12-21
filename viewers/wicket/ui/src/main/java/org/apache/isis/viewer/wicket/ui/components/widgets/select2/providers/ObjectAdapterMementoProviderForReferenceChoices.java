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
package org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.val;

public class ObjectAdapterMementoProviderForReferenceChoices
extends ObjectAdapterMementoProviderAbstract
implements ObjectAdapterMementoProviderForChoices {

    private static final long serialVersionUID = 1L;

    public ObjectAdapterMementoProviderForReferenceChoices(
            final ScalarModel scalarModel) {
        super(scalarModel);
    }

    @Override
    public Can<ObjectMemento> getChoiceMementos() {
        val commonContext = super.getCommonContext();
        val choices = getScalarModel().getChoices(); // must not return detached entities
        val choiceMementos = choices.map(commonContext::mementoForParameter);
        return choiceMementos;
    }

    @Override
    protected Can<ObjectMemento> obtainMementos(final String term) {
        return super.obtainMementos(term, getChoiceMementos());
    }


}

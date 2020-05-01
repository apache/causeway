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

import java.util.Collection;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.Getter;
import lombok.val;

public class ObjectAdapterMementoProviderForValueChoices
extends ObjectAdapterMementoProviderAbstract 
implements ObjectAdapterMementoProviderForChoices {

    private static final long serialVersionUID = 1L;
    
    @Getter(onMethod = @__(@Override))
    private final Can<ObjectMemento> choiceMementos;

    public ObjectAdapterMementoProviderForValueChoices(
            ScalarModel scalarModel,
            Can<ObjectMemento> choicesMementos) {
        
        super(scalarModel);
        this.choiceMementos = choicesMementos;
    }

    @Override
    protected Can<ObjectMemento> obtainMementos(String term) {
        return obtainMementos(term, choiceMementos);
    }

    @Override
    public Collection<ObjectMemento> toChoices(final Collection<String> ids) {
        
        return obtainMementos(null)
        .filter((ObjectMemento input) -> {
            val id = getIdValue(input);
            return ids.contains(id);
        })
        .toList();
    }

}

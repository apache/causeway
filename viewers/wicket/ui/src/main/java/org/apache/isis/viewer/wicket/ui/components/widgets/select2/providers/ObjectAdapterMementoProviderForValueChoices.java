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
import java.util.List;
import java.util.function.Predicate;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.webapp.context.memento.ObjectMemento;

import lombok.Getter;
import lombok.val;

public class ObjectAdapterMementoProviderForValueChoices
extends ObjectAdapterMementoProviderAbstract 
implements ObjectAdapterMementoProviderForChoices {

    private static final long serialVersionUID = 1L;
    
    @Getter(onMethod = @__(@Override))
    private final List<ObjectMemento> choiceMementos;

    public ObjectAdapterMementoProviderForValueChoices(
            ScalarModel scalarModel,
            List<ObjectMemento> choicesMementos) {
        
        super(scalarModel);
        this.choiceMementos = choicesMementos;
    }

    @Override
    protected List<ObjectMemento> obtainMementos(String term) {
        return obtainMementos(term, choiceMementos);
    }

    @Override
    public Collection<ObjectMemento> toChoices(final Collection<String> ids) {
        final List<ObjectMemento> mementos = obtainMementos(null);

        final Predicate<ObjectMemento> lookupOam = (ObjectMemento input) -> {
            val id = getIdValue(input);
            return ids.contains(id);
        };
        
        return _Lists.filter(mementos, lookupOam);
    }

}

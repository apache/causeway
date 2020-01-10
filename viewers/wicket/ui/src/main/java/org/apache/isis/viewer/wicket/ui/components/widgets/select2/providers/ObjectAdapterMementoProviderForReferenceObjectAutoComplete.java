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

import java.util.List;

import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.webapp.context.memento.ObjectMemento;

import lombok.val;

public class ObjectAdapterMementoProviderForReferenceObjectAutoComplete
extends ObjectAdapterMementoProviderAbstract {

    private static final long serialVersionUID = 1L;

    public ObjectAdapterMementoProviderForReferenceObjectAutoComplete(ScalarModel model) {
        super(model);
    }

    @Override
    protected List<ObjectMemento> obtainMementos(String term) {
        val typeOfSpecification = getScalarModel().getTypeOfSpecification();
        val autoCompleteFacet = typeOfSpecification.getFacet(AutoCompleteFacet.class);
        val autoCompleteAdapters = autoCompleteFacet.execute(term,InteractionInitiatedBy.USER);
        val commonContext = super.getCommonContext();
        
        return _Lists.map(autoCompleteAdapters, commonContext::mementoFor);
    }


}

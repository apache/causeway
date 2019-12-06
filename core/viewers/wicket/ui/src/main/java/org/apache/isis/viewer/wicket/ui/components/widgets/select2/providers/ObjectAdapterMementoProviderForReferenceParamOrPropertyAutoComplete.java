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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.webapp.context.memento.ObjectMemento;

import lombok.val;

public class ObjectAdapterMementoProviderForReferenceParamOrPropertyAutoComplete
extends ObjectAdapterMementoProviderAbstract {

    private static final long serialVersionUID = 1L;

    public ObjectAdapterMementoProviderForReferenceParamOrPropertyAutoComplete(ScalarModel model) {
        super(model);
    }

    @Override
    protected List<ObjectMemento> obtainMementos(String term) {
        
        val commonContext = super.getCommonContext();
        
        val autoCompleteChoices = _Lists.<ManagedObject>newArrayList();
        if (getScalarModel().hasAutoComplete()) {
            val autoCompleteAdapters =
                    getScalarModel().getAutoComplete(term, commonContext.getAuthenticationSession());
            autoCompleteChoices.addAll(autoCompleteAdapters);
        }
        
        return _Lists.map(autoCompleteChoices, commonContext::mementoFor);
        
    }

    @Override
    public Collection<ObjectMemento> toChoices(final Collection<String> ids) {
        val commonContext = super.getCommonContext();
        
        final Function<String, ObjectMemento> function = (final String input)->{
            if(NULL_PLACEHOLDER.equals(input)) {
                return null;
            }
            val rootOid = RootOid.deString(input);
            val memento = commonContext.mementoFor(rootOid);
            return memento;
            
        };
        return _NullSafe.stream(ids).map(function).collect(Collectors.toList());
    }


}

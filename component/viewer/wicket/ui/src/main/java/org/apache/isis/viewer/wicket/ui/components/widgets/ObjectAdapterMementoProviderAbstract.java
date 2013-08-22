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
package org.apache.isis.viewer.wicket.ui.components.widgets;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.vaynberg.wicket.select2.TextChoiceProvider;

import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

public abstract class ObjectAdapterMementoProviderAbstract extends TextChoiceProvider<ObjectAdapterMemento> {
    private static final long serialVersionUID = 1L;

    public ObjectAdapterMementoProviderAbstract(){}
    
    @Override
    protected String getDisplayText(ObjectAdapterMemento choice) {
        return choice.getObjectAdapter(ConcurrencyChecking.NO_CHECK).titleString(null);
    }

    @Override
    protected Object getId(ObjectAdapterMemento choice) {
        return choice.toString();
    }

    @Override
    public void query(String term, int page, com.vaynberg.wicket.select2.Response<ObjectAdapterMemento> response) {
        
        List<ObjectAdapterMemento> mementos = obtainMementos(term);
        response.addAll(mementos);
    }

    protected abstract List<ObjectAdapterMemento> obtainMementos(String term);

    @Override
    public Collection<ObjectAdapterMemento> toChoices(Collection<String> ids) {
        Function<String, ObjectAdapterMemento> function = new Function<String, ObjectAdapterMemento>() {

            @Override
            public ObjectAdapterMemento apply(String input) {
                final RootOid oid = RootOidDefault.deString(input, ObjectAdapterMemento.getOidMarshaller());
                return ObjectAdapterMemento.createPersistent(oid);
            }
        };
        return Collections2.transform(ids, function);
    }
}
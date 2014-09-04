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

import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.wicketstuff.select2.Response;
import org.wicketstuff.select2.TextChoiceProvider;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public abstract class ObjectAdapterMementoProviderAbstract extends TextChoiceProvider<ObjectAdapterMemento> {

    private static final long serialVersionUID = 1L;
    
    protected static final String NULL_PLACEHOLDER = "$$_isis_null_$$";
    private static final String NULL_DISPLAY_TEXT = "";

    private final ScalarModel scalarModel;

    public ObjectAdapterMementoProviderAbstract(final ScalarModel scalarModel){
        this.scalarModel = scalarModel;
    }
    
    @Override
    protected String getDisplayText(ObjectAdapterMemento choice) {
        return choice != null? choice.getObjectAdapter(ConcurrencyChecking.NO_CHECK).titleString(null) : NULL_DISPLAY_TEXT;
    }

    @Override
    protected Object getId(ObjectAdapterMemento choice) {
        return choice != null? choice.asString(): NULL_PLACEHOLDER;
    }

    @Override
    public void query(String term, int page, Response<ObjectAdapterMemento> response) {
        
        final List<ObjectAdapterMemento> mementos = Lists.newArrayList(obtainMementos(term));
        // if not mandatory, and the list doesn't contain null already, then add it in.
        if(!scalarModel.isRequired() && !mementos.contains(null)) {
            mementos.add(0, null);
        }
        response.addAll(mementos);
    }

    protected abstract List<ObjectAdapterMemento> obtainMementos(String term);

    @Override
    public Collection<ObjectAdapterMemento> toChoices(Collection<String> ids) {
        Function<String, ObjectAdapterMemento> function = new Function<String, ObjectAdapterMemento>() {

            @Override
            public ObjectAdapterMemento apply(String input) {
                if(NULL_PLACEHOLDER.equals(input)) {
                    return null;
                }
                final RootOid oid = RootOidDefault.deString(input, ObjectAdapterMemento.getOidMarshaller());
                return ObjectAdapterMemento.createPersistent(oid);
            }
        };
        return Collections2.transform(ids, function);
    }
    
    protected ScalarModel getScalarModel() {
        return scalarModel;
    }
}

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
package org.apache.isis.viewer.wicket.model.models;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

class ActionArgumentCollectionModel extends ModelAbstract<ObjectAdapter> implements
        ActionArgumentModel {

    private final ActionParameterMemento parameterMemento;
    private ObjectAdapterMemento adapterMemento;

    public ActionArgumentCollectionModel(
            final ObjectAdapterMemento adapterMemento,
            final ActionParameterMemento parameterMemento) {
        this.adapterMemento = adapterMemento;
        this.parameterMemento = parameterMemento;
    }

    @Override
    public ActionParameterMemento getParameterMemento() {
        return parameterMemento;
    }

    @Override
    protected ObjectAdapter load() {
        final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(getSpecificationLoader());
        final ObjectSpecification specification = parameterMemento.getSpecification(getSpecificationLoader());

        return adapterMemento
                .getObjectAdapter(AdapterManager.ConcurrencyChecking.CHECK, getPersistenceSession(), getSpecificationLoader());
    }

    @Override
    public void setObject(final ObjectAdapter adapter) {
        super.setObject(adapter);
        adapterMemento = ObjectAdapterMemento.createOrNull(adapter);
    }

    @Override
    public void reset() {
        final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(
                getSpecificationLoader());
        final ObjectAdapter parentAdapter =
                adapterMemento.getObjectAdapter(AdapterManager.ConcurrencyChecking.NO_CHECK,
                        getPersistenceSession(), getSpecificationLoader());
        final ObjectAdapter defaultAdapter = actionParameter.getDefault(parentAdapter);
        setObject(defaultAdapter);
    }

    /**
     * transient because only temporary hint.
     */
    private transient ObjectAdapter[] actionArgsHint;

    @Override
    public void setActionArgsHint(ObjectAdapter[] actionArgsHint) {
        this.actionArgsHint = actionArgsHint;
    }

}

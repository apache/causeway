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

package org.apache.isis.viewer.wicket.model.util;

import com.google.common.base.Function;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.mementos.CollectionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;

/**
 * @deprecated - use {@link ObjectAdapterMemento.Functions}
 */
@Deprecated
public final class MementoFunctions {

    private MementoFunctions() {
    }

    /**
     * @deprecated - use {@link ObjectAdapterMemento.Functions}
     */
    @Deprecated
    public static Function<ObjectSpecification, ObjectSpecId> fromSpec() {
        return ObjectAdapterMemento.Functions.fromSpec();
    }

    /**
     * @deprecated - use {@link ObjectAdapterMemento.Functions}
     */
    @Deprecated
    public static Function<OneToOneAssociation, PropertyMemento> fromProperty() {
        return ObjectAdapterMemento.Functions.fromProperty();
    }

    /**
     * @deprecated - use {@link ObjectAdapterMemento.Functions}
     */
    @Deprecated
    public static Function<OneToManyAssociation, CollectionMemento> fromCollection() {
        return ObjectAdapterMemento.Functions.fromCollection();
    }

    /**
     * @deprecated - use {@link ObjectAdapterMemento.Functions}
     */
    @Deprecated
    public static Function<ObjectAction, ActionMemento> fromAction() {
        return ObjectAdapterMemento.Functions.fromAction();
    }

    /**
     * @deprecated - use {@link ObjectAdapterMemento.Functions}
     */
    @Deprecated
    public static Function<ObjectActionParameter, ActionParameterMemento> fromActionParameter() {
        return ObjectAdapterMemento.Functions.fromActionParameter();
    }

    /**
     * @deprecated - use {@link ObjectAdapterMemento.Functions}
     */
    @Deprecated
    public static Function<Object, ObjectAdapterMemento> fromPojo(final AdapterManager adapterManager) {
        return ObjectAdapterMemento.Functions.fromPojo(adapterManager);
    }

    /**
     * @deprecated - use {@link ObjectAdapterMemento.Functions}
     */
    @Deprecated
    public static Function<ObjectAdapter, ObjectAdapterMemento> fromAdapter() {
        return ObjectAdapterMemento.Functions.fromAdapter();
    }

}

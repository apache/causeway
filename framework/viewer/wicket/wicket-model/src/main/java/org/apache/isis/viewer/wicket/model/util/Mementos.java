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
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.mementos.CollectionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;

public final class Mementos {

    private Mementos() {
    }

    public static Function<ObjectSpecification, ObjectSpecId> fromSpec() {
        return new Function<ObjectSpecification, ObjectSpecId>() {

            @Override
            public ObjectSpecId apply(final ObjectSpecification from) {
                return from.getSpecId();
            }
        };
    }

    public static Function<OneToOneAssociation, PropertyMemento> fromProperty() {
        return new Function<OneToOneAssociation, PropertyMemento>() {
            @Override
            public PropertyMemento apply(final OneToOneAssociation from) {
                return new PropertyMemento(from);
            }
        };
    }

    public static Function<OneToManyAssociation, CollectionMemento> fromCollection() {
        return new Function<OneToManyAssociation, CollectionMemento>() {
            @Override
            public CollectionMemento apply(final OneToManyAssociation from) {
                return new CollectionMemento(from);
            }
        };
    }

    public static Function<ObjectAction, ActionMemento> fromAction() {
        return new Function<ObjectAction, ActionMemento>() {
            @Override
            public ActionMemento apply(final ObjectAction from) {
                return new ActionMemento(from);
            }
        };
    }

    public static Function<ObjectActionParameter, ActionParameterMemento> fromActionParameter() {
        return new Function<ObjectActionParameter, ActionParameterMemento>() {
            @Override
            public ActionParameterMemento apply(final ObjectActionParameter from) {
                return new ActionParameterMemento(from);
            }
        };
    }

    public static Function<Object, ObjectAdapterMemento> fromPojo() {
        return new Function<Object, ObjectAdapterMemento>() {
            @Override
            public ObjectAdapterMemento apply(final Object pojo) {
                final ObjectAdapter adapter = getAdapterManager().adapterFor(pojo);
                return ObjectAdapterMemento.createOrNull(adapter);
            }
        };
    }

    public static Function<ObjectAdapter, ObjectAdapterMemento> fromAdapter() {
        return new Function<ObjectAdapter, ObjectAdapterMemento>() {
            @Override
            public ObjectAdapterMemento apply(final ObjectAdapter adapter) {
                return ObjectAdapterMemento.createOrNull(adapter);
            }
        };
    }

    private static AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession().getAdapterManager();
    }

}

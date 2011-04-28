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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.mementos.CollectionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.mementos.SpecMemento;

import com.google.common.base.Function;

public final class Mementos {

	private Mementos(){}

	public static Function<ObjectSpecification, SpecMemento> fromSpec() {
		return new Function<ObjectSpecification, SpecMemento>() {
			
			public SpecMemento apply(ObjectSpecification from) {
				return new SpecMemento(from);
			}
		};
	}

	public static Function<OneToOneAssociation, PropertyMemento> fromProperty() {
		return new Function<OneToOneAssociation, PropertyMemento>() {
			public PropertyMemento apply(
					OneToOneAssociation from) {
				return new PropertyMemento(from);
			}
		};
	}
	
	public static Function<OneToManyAssociation, CollectionMemento> fromCollection() {
		return new Function<OneToManyAssociation, CollectionMemento>() {
			public CollectionMemento apply(
					OneToManyAssociation from) {
				return new CollectionMemento(from);
			}
		};
	}
	
	public static Function<ObjectAction, ActionMemento> fromAction() {
		return new Function<ObjectAction, ActionMemento>() {
			public ActionMemento apply(
					ObjectAction from) {
				return new ActionMemento(from);
			}
		};
	}
	
	public static Function<ObjectActionParameter, ActionParameterMemento> fromActionParameter() {
		return new Function<ObjectActionParameter, ActionParameterMemento>() {
			public ActionParameterMemento apply(
					ObjectActionParameter from) {
				return new ActionParameterMemento(from);
			}
		};
	}

	public static Function<Object, ObjectAdapterMemento> fromPojo() {
		return new Function<Object, ObjectAdapterMemento>() {
			public ObjectAdapterMemento apply(Object pojo) {
				ObjectAdapter adapter = getAdapterManager().adapterFor(pojo);
				return ObjectAdapterMemento.createOrNull(adapter);
			}
		};
	}


	   public static Function<ObjectAdapter, ObjectAdapterMemento> fromAdapter() {
	        return new Function<ObjectAdapter, ObjectAdapterMemento>() {
	            public ObjectAdapterMemento apply(ObjectAdapter adapter) {
	                return ObjectAdapterMemento.createOrNull(adapter);
	            }
	        };
	    }


	private static AdapterManager getAdapterManager() {
		return IsisContext.getPersistenceSession().getAdapterManager();
	}

}

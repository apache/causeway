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
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

import com.google.common.base.Function;

public final class ObjectAdapters {

	private ObjectAdapters(){}
	
	public static Function<Object, ObjectAdapter> fromPojo() {
		return new Function<Object, ObjectAdapter>() {
			public ObjectAdapter apply(Object pojo) {
				return getAdapterManager().getAdapterFor(pojo);
			}
		};
	}

	public static Function<ObjectAdapterMemento, ObjectAdapter> fromMemento() {
		return new Function<ObjectAdapterMemento, ObjectAdapter>() {
			public ObjectAdapter apply(ObjectAdapterMemento from) {
				return from.getObjectAdapter();
			}
		};
	}

	public static Function<Oid, ObjectAdapter> fromOid() {
		return new Function<Oid, ObjectAdapter>() {
			public ObjectAdapter apply(Oid from) {
				final ObjectAdapter adapterFor = getAdapterManager().getAdapterFor(from);
				return adapterFor;
			}
		};
	}

	private static AdapterManager getAdapterManager() {
		return getPersistenceSession().getAdapterManager();
	}

	private static PersistenceSession getPersistenceSession() {
		return IsisContext.getPersistenceSession();
	}
}


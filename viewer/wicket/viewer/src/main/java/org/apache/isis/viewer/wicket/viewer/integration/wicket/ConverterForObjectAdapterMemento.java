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


package org.apache.isis.viewer.wicket.viewer.integration.wicket;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.wicket.util.convert.IConverter;


/**
 * Implementation of a Wicket {@link IConverter} for {@link ObjectAdapterMemento}s, 
 * converting to-and-from their {@link Oid}'s string representation.
 */
public class ConverterForObjectAdapterMemento implements IConverter {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Converts {@link OidStringifier stringified} {@link Oid} to {@link ObjectAdapterMemento}.
	 */
	public Object convertToObject(String value, Locale locale) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		Oid oid = getOidStringifier().deString(value);
		ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
		return ObjectAdapterMemento.createOrNull(adapter);
	}

	/**
	 * Converts {@link ObjectAdapterMemento} to {@link OidStringifier stringified} {@link Oid}.
	 */
	public String convertToString(Object object, Locale locale) {
		if (object == null) {
			return null;
		}
		ObjectAdapterMemento memento = (ObjectAdapterMemento) object;
		Oid oid = memento.getObjectAdapter().getOid();
		if (oid == null) {
			// values don't have an Oid, but we don't support 'em
			throw new IllegalStateException("cannot convert memento to OBJECT_OID; memento's adapter is a value so has no OBJECT_OID");
		}
		return getOidStringifier().enString(oid);
	}
	
	protected AdapterManager getAdapterManager() {
		return getPersistenceSession().getAdapterManager();
	}

	protected OidStringifier getOidStringifier() {
		return getPersistenceSession().getOidGenerator().getOidStringifier();
	}

	protected PersistenceSession getPersistenceSession() {
		return IsisContext.getPersistenceSession();
	}

}

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

package org.apache.isis.viewer.html.context;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.Persistor;

public abstract class RootAdapterMappingAbstract implements RootAdapterMapping {
    
    private static final long serialVersionUID = 1L;
    
    private final String oidStr;

    public RootAdapterMappingAbstract(final ObjectAdapter adapter) {
        final Oid oid = adapter.getOid();
        oidStr = oid.enString(getOidMarshaller());
    }

    @Override
    public String getOidStr() {
        return oidStr;
    }

    RootOidDefault getOid() {
        return RootOidDefault.deString(oidStr, getOidMarshaller());
    }

    @Override
    public ObjectAdapter getObject() {
        return getPersistenceSession().loadObject(getOid());
    }


    // /////////////////////////////////////////////////////
    // version
    // /////////////////////////////////////////////////////

    @Override
    public abstract Version getVersion();

    @Override
    public abstract void checkVersion(final ObjectAdapter object);

    @Override
    public abstract void updateVersion();

    
    // /////////////////////////////////////////////////////
    // restoreToLoader
    // /////////////////////////////////////////////////////

    @Override
    public abstract void restoreToLoader();


    // /////////////////////////////////////////////////////
    // value semantics
    // /////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        return oidStr.hashCode();
    }

    @Override
    public abstract boolean equals(final Object obj);


    // /////////////////////////////////////////////////////
    // debugging, toString
    // /////////////////////////////////////////////////////

    @Override
    public abstract void debugData(final DebugBuilder debug);

    @Override
    public abstract String toString();

    // /////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////

    private static Persistor getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected OidMarshaller getOidMarshaller() {
		return IsisContext.getOidMarshaller();
	}

}

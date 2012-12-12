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
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.runtime.memento.Memento;

public class TransientRootAdapterMapping extends RootAdapterMappingAbstract {
    
    private static final long serialVersionUID = 1L;
    
    private final Memento memento;

    public TransientRootAdapterMapping(final ObjectAdapter adapter) {
        super(adapter);
        Assert.assertFalse("OID is for persistent", !adapter.getOid().isTransient());
        Assert.assertFalse("adapter is for persistent", !adapter.isTransient());
        memento = new Memento(adapter);
    }

    // /////////////////////////////////////////////////////
    // version
    // /////////////////////////////////////////////////////

    @Override
    public Version getVersion() {
        return null;
    }

    @Override
    public void checkVersion(final ObjectAdapter object) {
    }

    @Override
    public void updateVersion() {
    }

    // /////////////////////////////////////////////////////
    // restoreToLoader
    // /////////////////////////////////////////////////////

    @Override
    public void restoreToLoader() {
        memento.recreateObject();
    }

    // /////////////////////////////////////////////////////
    // value semantics
    // /////////////////////////////////////////////////////

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof TransientRootAdapterMapping)) {
            return false;
        } 
        return ((TransientRootAdapterMapping) obj).getOidStr().equals(getOidStr());
    }

    

    // /////////////////////////////////////////////////////
    // debugging, toString
    // /////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugBuilder debug) {
        memento.debug(debug);
    }

    @Override
    public String toString() {
        return "TRANSIENT : " + getOidStr() + " : " + memento;
    }


}

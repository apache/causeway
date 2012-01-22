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

package org.apache.isis.runtimes.dflt.remoting.common.client.facets;

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.runtimes.dflt.runtime.persistence.ConcurrencyException;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

public class ProxyUtil {
    private final static Logger LOG = Logger.getLogger(ProxyUtil.class);

    private ProxyUtil() {
    }

    public static ConcurrencyException concurrencyException(final ConcurrencyException e) {
        if (LOG.isInfoEnabled()) {
            LOG.info("concurrency conflict: " + e.getMessage());
        }
        final Oid source = e.getSource();
        if (source == null) {
            return e;
        }
        final ObjectAdapter failedObject = getAdapterManager().getAdapterFor(source);
        getPersistenceSession().reload(failedObject);
        return new ConcurrencyException("Object automatically reloaded: " + failedObject.titleString(), e);
    }

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private static AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

}

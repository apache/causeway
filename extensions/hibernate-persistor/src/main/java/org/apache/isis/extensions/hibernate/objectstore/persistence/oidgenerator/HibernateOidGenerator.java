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


package org.apache.isis.extensions.hibernate.objectstore.persistence.oidgenerator;

import org.apache.log4j.Logger;
import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.commons.lang.ToString;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.runtime.persistence.oidgenerator.OidGeneratorAbstract;


public class HibernateOidGenerator extends OidGeneratorAbstract {

    private static final Logger LOG = Logger.getLogger(HibernateOidGenerator.class);
    private static long transientId = 0;


    ////////////////////////////////////////////////////////////////
    // Name
    ////////////////////////////////////////////////////////////////

    public String name() {
        return "Hibernate Oids";
    }

    ////////////////////////////////////////////////////////////////
    // main API
    ////////////////////////////////////////////////////////////////

    public synchronized HibernateOid createTransientOid(final Object object) {
        final HibernateOid oid = HibernateOid.createTransient(object.getClass(), transientId++);
        if (LOG.isDebugEnabled()) {
            LOG.debug("created OID " + oid + " for " + new ToString(object));
        }
        return oid;
    }


    /**
     * {@inheritDoc}
     * 
     * <p>
     * The call to this method should be preceded by updating the 
     * {@link HibernateOid}, using {@link HibernateOid#setHibernateId(java.io.Serializable)}.
     */
    public void convertTransientToPersistentOid(final Oid oid) {
    	if (!(oid instanceof HibernateOid)) {
    		throw new IllegalArgumentException("Oid is not a HibernateOid");
    	}
		HibernateOid hibernateOid = (HibernateOid) oid;
        hibernateOid.makePersistent();
        if (LOG.isDebugEnabled()) {
            LOG.debug("converted transient OID to persistent " + oid);
        }
    }


    ////////////////////////////////////////////////////////////////
    // Debugging
    ////////////////////////////////////////////////////////////////

    public void debugData(final DebugString debug) {}

    public String debugTitle() {
        return null;
    }
    


}

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


package org.apache.isis.runtime.testsystem;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManagerPersist;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManagerTestSupport;
import org.apache.isis.runtime.session.IsisSession;
import org.apache.isis.runtime.transaction.IsisTransactionManager;


public abstract class ProxyJunit3TestCase extends TestCase {

    protected TestProxySystem system;

    @Override
    protected void setUp() throws Exception {
        createSystem();
        initSystem();
    }

    protected void initSystem() {
        system.init();
    }

    protected void createSystem() {
        Logger.getRootLogger().setLevel(Level.OFF);
        system = new TestProxySystem();
    }

    
    protected static IsisSession getSession() {
        return IsisContext.getSession();
    }

    protected static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected static AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }
    
    protected static AdapterManagerTestSupport getAdapterManagerTestSupport() {
        return (AdapterManagerTestSupport) IsisContext.getPersistenceSession().getAdapterManager();
    }

    protected static AdapterManagerPersist getAdapterManagerPersist() {
        return (AdapterManagerPersist) IsisContext.getPersistenceSession().getAdapterManager();
    }

    protected static IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    
    

}


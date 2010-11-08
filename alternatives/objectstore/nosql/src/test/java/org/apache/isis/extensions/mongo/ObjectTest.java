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


package org.apache.isis.extensions.mongo;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.extensions.nosql.ExampleValuePojo;
import org.apache.isis.extensions.nosql.TrialObjects;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SerialOid;

import static org.junit.Assert.assertEquals;


public class ObjectTest {

    @Test
    public void hasInstances() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        TrialObjects testObjects = new TrialObjects();
        
        SerialOid oid = SerialOid.createTransient(1);
        ExampleValuePojo pojo = new ExampleValuePojo();
        
        ObjectAdapter adapter = testObjects.createAdapter(pojo, oid);
        
        
        ObjectSpecification loadSpecification = testObjects.loadSpecification(ExampleValuePojo.class);
        

        assertEquals(loadSpecification, adapter.getSpecification());
        assertEquals(oid, adapter.getOid());
        assertEquals(pojo, adapter.getObject());
        assertEquals(ResolveState.TRANSIENT, adapter.getResolveState());
    }

 }



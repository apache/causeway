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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.apache.isis.metamodel.encoding.DataOutputStreamExtended;


public class HibernateOidEncodeability {

    private PipedInputStream pipedInputStream;
    private PipedOutputStream pipedOutputStream;
    private DataOutputStreamExtended outputImpl;

    private HibernateOid oid;

    @Before
    public void setUp() throws IOException {
        pipedInputStream = new PipedInputStream();
        pipedOutputStream = new PipedOutputStream(pipedInputStream);
        outputImpl = new DataOutputStreamExtended(pipedOutputStream);
    }

    @Test
    public void shouldBeAbleToEncodeHibernateOids() throws IOException {
        oid = HibernateOid.createTransient(java.lang.Object.class, 123);
        oid.encode(outputImpl);
    }

}


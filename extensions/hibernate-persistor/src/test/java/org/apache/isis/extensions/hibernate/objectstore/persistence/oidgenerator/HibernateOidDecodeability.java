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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.apache.isis.metamodel.encoding.DataInputStreamExtended;
import org.apache.isis.metamodel.encoding.DataOutputStreamExtended;


@RunWith(Parameterized.class)
public class HibernateOidDecodeability {

    private PipedInputStream pipedInputStream;
    private PipedOutputStream pipedOutputStream;
    private DataOutputStreamExtended outputImpl;
    private DataInputStreamExtended inputImpl;

    private HibernateOid oid, oid2;

    @Parameters
    public static Collection<?> data() {
        return Arrays.asList(new Object[][] { { java.lang.Object.class, 123, -1 }, { java.lang.Object.class, 123, 456 }, });
    }

    private final Class<?> clazz;
    private final Long primaryKey;
    private final Long hibernateId;

    public HibernateOidDecodeability(final Class<?> clazz, final long primaryKey, final long hibernateId) {
        this.clazz = clazz;
        this.primaryKey = primaryKey;
        this.hibernateId = hibernateId;
    }

    @Before
    public void setUp() throws IOException {
        pipedInputStream = new PipedInputStream();
        pipedOutputStream = new PipedOutputStream(pipedInputStream);
        outputImpl = new DataOutputStreamExtended(pipedOutputStream);
        inputImpl = new DataInputStreamExtended(pipedInputStream);
    }

    @Test
    public void shouldBeAbleToDecodeHibernateOids() throws IOException {
        oid = HibernateOid.createTransient(clazz, primaryKey);
        if (hibernateId != -1) {
            oid.setHibernateId(hibernateId);
            oid.makePersistent();
        }
        oid.encode(outputImpl);

        oid2 = new HibernateOid(inputImpl);

        assertThat(oid2.getClassName(), is(equalTo(oid.getClassName())));
        assertThat(oid2.getHibernateId(), is(equalTo(oid.getHibernateId())));
        assertThat(oid2.getPrevious(), is(equalTo(oid.getPrevious())));
        assertThat(oid2.getPrimaryKey(), is(equalTo(oid.getPrimaryKey())));
        assertThat(oid2.isTransient(), is(equalTo(oid.isTransient())));
    }

}


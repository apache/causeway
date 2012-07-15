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
package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.awt.Image;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.scalars.JdkValuedEntity;
import org.apache.isis.tck.dom.scalars.JdkValuedEntityRepository;

public class Persistence_persist_jdkValuedEntity {

    private JdkValuedEntityRepository repo = new JdkValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("JDKVALUEDENTITY"))
        .withServices(repo)
        .build();

    @Test
    public void persistTwo() throws Exception {
        iswf.beginTran();
        repo.newEntity().setStringProperty("1");
        repo.newEntity().setStringProperty("2");
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        List<JdkValuedEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }

    @Test
    public void persistAllValues() throws Exception {
        iswf.beginTran();
        JdkValuedEntity entity = repo.newEntity();
        entity.setStringProperty("1");
        entity.setBigDecimalProperty(BigDecimal.valueOf(123456789012345L, 2));
        entity.setBigIntegerProperty(BigInteger.valueOf(543210987654321L));
        Image image = null; // TODO
        entity.setImageProperty(image);
        entity.setJavaSqlDateProperty(new java.sql.Date(Utils.toMillis(2010, 5, 13)));
        entity.setJavaSqlTimeProperty(new java.sql.Time(Utils.toMillis(1970, 1, 2, 5, 10, 15))); // date portion is unimportant, but is preserved
        entity.setJavaSqlTimestampProperty(new Timestamp(Utils.toMillis(2010, 5, 13, 10, 15, 20)));
        entity.setJavaUtilDateProperty(new java.util.Date(Utils.toMillis(2010, 5, 13, 20, 15, 10)));
        
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        JdkValuedEntity entityRetrieved = repo.list().get(0);
        assertThat(entityRetrieved.getStringProperty(), is("1"));
        assertThat(entityRetrieved.getBigDecimalProperty(), is(BigDecimal.valueOf(123456789012345L, 2)));
        assertThat(entityRetrieved.getBigIntegerProperty(), is(BigInteger.valueOf(543210987654321L)));
        assertThat(entityRetrieved.getImageProperty(), is(nullValue())); // TODO
        assertThat(entityRetrieved.getJavaSqlDateProperty(), is(new java.sql.Date(Utils.toMillis(2010, 5, 13))));
        assertThat(entityRetrieved.getJavaSqlTimeProperty(), is(new java.sql.Time(Utils.toMillis(1970, 1, 2, 5, 10, 15))));
        assertThat(entityRetrieved.getJavaSqlTimestampProperty(), is(new Timestamp(Utils.toMillis(2010, 5, 13, 10, 15, 20))));
        assertThat(entityRetrieved.getJavaUtilDateProperty(), is(new java.util.Date(Utils.toMillis(2010, 5, 13, 20, 15, 10))));
        
        iswf.commitTran();
    }
}

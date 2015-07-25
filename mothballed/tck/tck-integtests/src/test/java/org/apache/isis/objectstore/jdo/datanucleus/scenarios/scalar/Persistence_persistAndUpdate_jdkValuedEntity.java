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
package org.apache.isis.objectstore.jdo.datanucleus.scenarios.scalar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.tck.dom.scalars.JdkValuedEntity;
import org.apache.isis.core.tck.dom.scalars.JdkValuedEntityRepository;
import org.apache.isis.core.tck.dom.scalars.MyEnum;
import org.apache.isis.objectstore.jdo.datanucleus.Utils;

public class Persistence_persistAndUpdate_jdkValuedEntity {

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
    public void persist_then_update() throws Exception {
        iswf.beginTran();
        JdkValuedEntity entity = repo.newEntity();
        entity.setStringProperty("1");
        entity.setBigDecimalProperty(BigDecimal.valueOf(543210987654321L, 0)); // mssqlserver can cope with scale>0, but hsqldb cannot
        entity.setBigIntegerProperty(BigInteger.valueOf(123456789012345L));
        entity.setJavaSqlDateProperty(new java.sql.Date(Utils.toMillis(2009, 6, 11)));
        entity.setJavaSqlTimeProperty(new java.sql.Time(Utils.toMillis(1970, 1, 1, 0, 5, 10))); // date portion is unimportant, is preserved on mssqlserver but not on hsqldb
        entity.setJavaSqlTimestampProperty(new Timestamp(Utils.toMillis(2010, 5, 13, 20, 25, 30)));
        entity.setJavaUtilDateProperty(new java.util.Date(Utils.toMillis(2010, 5, 13, 22, 17, 12)));
        entity.setMyEnum(MyEnum.GREEN);
        
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        entity = repo.list().get(0);

        assertThat(entity.getStringProperty(), is("1"));
        assertThat(entity.getBigDecimalProperty(), is(BigDecimal.valueOf(543210987654321L, 0)));
        assertThat(entity.getBigIntegerProperty(), is(BigInteger.valueOf(123456789012345L)));
        assertThat(entity.getJavaSqlDateProperty(), is(new java.sql.Date(Utils.toMillis(2009, 6, 11))));
        assertThat(entity.getJavaSqlTimeProperty(), is(new java.sql.Time(Utils.toMillis(1970, 1, 1, 0, 5, 10))));
        assertThat(entity.getJavaSqlTimestampProperty(), is(new Timestamp(Utils.toMillis(2010, 5, 13, 20, 25, 30))));
        assertThat(entity.getJavaUtilDateProperty(), is(new java.util.Date(Utils.toMillis(2010, 5, 13, 22, 17, 12))));
        assertThat(entity.getMyEnum(), is(MyEnum.GREEN));
        

        entity.setBigDecimalProperty(BigDecimal.valueOf(123456789012345L, 0));
        entity.setBigIntegerProperty(BigInteger.valueOf(543210987654321L));
        entity.setJavaSqlDateProperty(new java.sql.Date(Utils.toMillis(2010, 5, 13)));
        entity.setJavaSqlTimeProperty(new java.sql.Time(Utils.toMillis(1970, 1, 1, 5, 10, 15))); 
        entity.setJavaSqlTimestampProperty(new Timestamp(Utils.toMillis(2010, 5, 13, 10, 15, 20)));
        entity.setJavaUtilDateProperty(new java.util.Date(Utils.toMillis(2010, 5, 13, 20, 15, 10)));
        entity.setMyEnum(MyEnum.BLUE);
        
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        entity = repo.list().get(0);
        assertThat(entity.getBigDecimalProperty(), is(BigDecimal.valueOf(123456789012345L, 0)));  
        assertThat(entity.getBigIntegerProperty(), is(BigInteger.valueOf(543210987654321L)));
        assertThat(entity.getJavaSqlDateProperty(), is(new java.sql.Date(Utils.toMillis(2010, 5, 13))));
        assertThat(entity.getJavaSqlTimeProperty(), is(new java.sql.Time(Utils.toMillis(1970, 1, 1, 5, 10, 15))));
        assertThat(entity.getJavaSqlTimestampProperty(), is(new Timestamp(Utils.toMillis(2010, 5, 13, 10, 15, 20))));
        assertThat(entity.getJavaUtilDateProperty(), is(new java.util.Date(Utils.toMillis(2010, 5, 13, 20, 15, 10))));
        assertThat(entity.getMyEnum(), is(MyEnum.BLUE));
        
        iswf.commitTran();
    }
}

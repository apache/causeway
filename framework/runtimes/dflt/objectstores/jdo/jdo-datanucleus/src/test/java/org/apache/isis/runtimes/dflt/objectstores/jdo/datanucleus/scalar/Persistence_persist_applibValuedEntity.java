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
package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.scalar;

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

import org.apache.isis.applib.value.Date;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.Utils;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.scalars.ApplibValuedEntity;
import org.apache.isis.tck.dom.scalars.ApplibValuedEntityRepository;
import org.apache.isis.tck.dom.scalars.JdkValuedEntity;
import org.apache.isis.tck.dom.scalars.JdkValuedEntityRepository;
import org.apache.isis.tck.dom.scalars.MyEnum;

public class Persistence_persist_applibValuedEntity {

    private ApplibValuedEntityRepository repo = new ApplibValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("APPLIBVALUEDENTITY"))
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
        List<ApplibValuedEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }

    @Test
    public void persistAllValues() throws Exception {
        iswf.beginTran();
        
        ApplibValuedEntity entity = repo.newEntity();
        entity.setStringProperty("1");

        Date date = new Date();
        entity.setDateProperty(date);
        
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        ApplibValuedEntity entityRetrieved = repo.list().get(0);
        assertThat(entityRetrieved.getDateProperty().dateValue(), is(date.dateValue()));
        
        iswf.commitTran();
    }
}

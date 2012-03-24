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

package org.apache.isis.tck.dom.simples;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.apache.isis.applib.annotation.Idempotent;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.QueryOnly;

@Named("Simples")
public interface SimpleEntityRepository {

    @QueryOnly
    @MemberOrder(sequence = "1")
    public List<SimpleEntity> list();

    @QueryOnly
    @MemberOrder(sequence = "2")
    public int count();

    @MemberOrder(sequence = "3")
    public SimpleEntity newTransientEntity();

    @Idempotent
    @MemberOrder(sequence = "4")
    public SimpleEntity newPersistentEntity(@Named("name") String name, @Named("flag") Boolean flag);

    @MemberOrder(sequence = "5")
    public void toggle(@Named("object") SimpleEntity object);

    @MemberOrder(sequence = "5")
    public SimpleEntity update(@Named("object") SimpleEntity object, String name, boolean flag, Boolean anotherBoolean, int anInt, Integer anotherInt, long aLong, Long anotherLong, double aDouble, Double anotherDouble, BigInteger aBigInteger, BigDecimal aBigDecimal);

}

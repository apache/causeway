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

package org.apache.isis.core.tck.fixture.scalars;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import javax.inject.Inject;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.core.tck.dom.scalars.JdkValuedEntity;
import org.apache.isis.core.tck.dom.scalars.JdkValuedEntityRepository;
import org.apache.isis.core.tck.dom.scalars.MyEnum;

public class JdkValuedEntityFixture extends AbstractFixture {

    @Override
    public void install() {
        createEntity();
        createEntity();
        createEntity();
        createEntity();
        createEntity();
    }

    private JdkValuedEntity createEntity() {
        final JdkValuedEntity entity = jdkValuesEntityRepository.newEntity();
        entity.setBigDecimalProperty(new BigDecimal("12345678901234567890.1234567890"));
        entity.setBigDecimalProperty2(new BigDecimal("123.45"));
        entity.setBigIntegerProperty(new BigInteger("123456789012345678"));
        entity.setBigIntegerProperty2(new BigInteger("12345"));
        entity.setJavaSqlDateProperty(asSqlDate("2014-04-24"));
        entity.setJavaSqlTimeProperty(asSqlTime("1970-01-01T12:34:45Z"));
        entity.setJavaSqlTimestampProperty(new Timestamp(1234567890));
        entity.setJavaUtilDateProperty(asDateTime("2013-05-25T12:34:45Z"));
        entity.setMyEnum(MyEnum.RED);
        return entity;
    }

    private final static DateTimeFormatter yyyyMMdd = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC();
    private final static DateTimeFormatter yyyyMMddTHHmmssZ = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").withZoneUTC();

    private static Date asDate(final String text) {
        return new java.util.Date(yyyyMMdd.parseDateTime(text).getMillis());
    }

    private static Date asDateTime(final String text) {
        return new java.util.Date(yyyyMMddTHHmmssZ.parseDateTime(text).getMillis());
    }

    private static java.sql.Date asSqlDate(final String text) {
        return new java.sql.Date(yyyyMMdd.parseDateTime(text).getMillis());
    }

    private static java.sql.Time asSqlTime(final String text) {
        return new java.sql.Time(yyyyMMddTHHmmssZ.parseDateTime(text).getMillis());
    }

    @Inject
    private JdkValuedEntityRepository jdkValuesEntityRepository;

}

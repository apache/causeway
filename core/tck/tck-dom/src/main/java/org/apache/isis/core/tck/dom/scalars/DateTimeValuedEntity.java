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

package org.apache.isis.core.tck.dom.scalars;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Title;

/**
 * TODO: delete ... OVERLAPS WITH ApplibValuedEntity, JdkValuedEntity, JodaValuedEntity
 *
 * @deprecated
 */
@javax.jdo.annotations.PersistenceCapable
@javax.jdo.annotations.Discriminator("APLV")
@javax.jdo.annotations.Query(
        name="dtmv_findByStringProperty", language="JDOQL",  
        value="SELECT FROM org.apache.isis.tck.dom.scalars.DateTimeValuedEntity WHERE stringProperty == :i")
@ObjectType("APLV")
@Deprecated
public class DateTimeValuedEntity extends AbstractDomainObject {

    
    // {{ StringProperty (also title, pk)
    private String stringProperty;

    @javax.jdo.annotations.PrimaryKey
    @Title
    @Optional
    @MemberOrder(sequence = "1")
    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(final String stringProperty) {
        this.stringProperty = stringProperty;
    }
    // }}

    
    // {{ JavaUtilDate (property)
    private java.util.Date javaUtilDate;

    @Optional
    @MemberOrder(name="dates", sequence = "1")
    public java.util.Date getJavaUtilDate() {
        return javaUtilDate;
    }

    public void setJavaUtilDate(final java.util.Date javaUtilDate) {
        this.javaUtilDate = javaUtilDate;
    }
    // }}

    // {{ JavaSqlDate (property)
    private java.sql.Date javaSqlDate;

    @javax.jdo.annotations.Persistent
    @Optional
    @MemberOrder(name="dates", sequence = "1")
    public java.sql.Date getJavaSqlDate() {
        return javaSqlDate;
    }

    public void setJavaSqlDate(final java.sql.Date javaSqlDate) {
        this.javaSqlDate = javaSqlDate;
    }

    public DateTimeValuedEntity updateJavaSqlDate(
            @Optional final java.sql.Date javaSqlDate) {
        setJavaSqlDate(javaSqlDate);
        return this;
    }
    // }}

    // {{ ApplibDate (property)
    private org.apache.isis.applib.value.Date applibDate;

    @javax.jdo.annotations.Persistent
    @Optional
    @MemberOrder(name="dates", sequence = "1")
    public org.apache.isis.applib.value.Date getApplibDate() {
        return applibDate;
    }

    public void setApplibDate(final org.apache.isis.applib.value.Date applibDate) {
        this.applibDate = applibDate;
    }
    // }}

    // {{ ApplibDateTime (property)
    private org.apache.isis.applib.value.DateTime applibDateTime;

    @javax.jdo.annotations.Persistent
    @Optional
    @MemberOrder(name="dates", sequence = "1")
    public org.apache.isis.applib.value.DateTime getApplibDateTime() {
        return applibDateTime;
    }

    public void setApplibDateTime(final org.apache.isis.applib.value.DateTime applibDateTime) {
        this.applibDateTime = applibDateTime;
    }
    // }}

    // {{ JodaLocalDateTime (property)
    private org.joda.time.LocalDateTime jodaLocalDateTime;

    @javax.jdo.annotations.Persistent
    @Optional
    @MemberOrder(name="dates", sequence = "1")
    public org.joda.time.LocalDateTime getJodaLocalDateTime() {
        return jodaLocalDateTime;
    }
    public void setJodaLocalDateTime(final org.joda.time.LocalDateTime jodaLocalDateTime) {
        this.jodaLocalDateTime = jodaLocalDateTime;
    }
    // }}

    // {{ JodaLocalDate (property)
    private org.joda.time.LocalDate jodaLocalDate;

    @javax.jdo.annotations.Persistent
    @Optional
    @MemberOrder(name="dates", sequence = "1")
    public org.joda.time.LocalDate getJodaLocalDate() {
        return jodaLocalDate;
    }

    public void setJodaLocalDate(final org.joda.time.LocalDate jodaLocalDate) {
        this.jodaLocalDate = jodaLocalDate;
    }
    // }}


    // {{ JodaDateTime (property)
    private org.joda.time.DateTime jodaDateTime;

    @javax.jdo.annotations.Persistent
    @Optional
    @MemberOrder(name="dates", sequence = "1")
    public org.joda.time.DateTime getJodaDateTime() {
        return jodaDateTime;
    }

    public void setJodaDateTime(final org.joda.time.DateTime jodaDateTime) {
        this.jodaDateTime = jodaDateTime;
    }
    // }}
    

}

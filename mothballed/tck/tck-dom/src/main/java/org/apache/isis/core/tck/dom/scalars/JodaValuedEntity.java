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

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Title;

@javax.jdo.annotations.PersistenceCapable
@javax.jdo.annotations.Discriminator("JODA")
@javax.jdo.annotations.Query(
        name="joda_findByStringProperty", language="JDOQL",  
        value="SELECT FROM org.apache.isis.tck.dom.scalars.JdkValuedEntity WHERE stringProperty == :i")
@ObjectType("JODA")
public class JodaValuedEntity extends AbstractDomainObject {

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

    public JodaValuedEntity updateStringProperty(
            @Optional final String stringProperty) {
        setStringProperty(stringProperty);
        return this;
    }

    // }}

    // {{ LocalDateProperty
    private LocalDate localDateProperty;

    @Optional
    @MemberOrder(sequence = "1")
    public LocalDate getLocalDateProperty() {
        return localDateProperty;
    }

    public void setLocalDateProperty(final LocalDate localDateProperty) {
        this.localDateProperty = localDateProperty;
    }

    public JodaValuedEntity updateLocalDateProperty(
            @Optional final LocalDate localDateProperty) {
        setLocalDateProperty(localDateProperty);
        return this;
    }
    // }}


    // {{ LocalDateTimeProperty
    private LocalDateTime localDateTimeProperty;

    @Optional
    @MemberOrder(sequence = "1")
    public LocalDateTime getLocalDateTimeProperty() {
        return localDateTimeProperty;
    }

    public void setLocalDateTimeProperty(final LocalDateTime localDateTimeProperty) {
        this.localDateTimeProperty = localDateTimeProperty;
    }

    public JodaValuedEntity updateLocalDateTimeProperty(
            @Optional final LocalDateTime localDateTimeProperty) {
        setLocalDateTimeProperty(localDateTimeProperty);
        return this;
    }
    // }}
    
    
    // {{ DateTimeProperty
    private DateTime dateTimeProperty;
    
    @Optional
    @MemberOrder(sequence = "1")
    public DateTime getDateTimeProperty() {
        return dateTimeProperty;
    }
    
    public void setDateTimeProperty(final DateTime dateTimeProperty) {
        this.dateTimeProperty = dateTimeProperty;
    }

    public JodaValuedEntity updateDateTimeProperty(
            @Optional final DateTime dateTimeProperty) {
        setDateTimeProperty(dateTimeProperty);
        return this;
    }
    // }}



}

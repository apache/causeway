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

package org.apache.isis.tck.dom.scalars;

import java.awt.Image;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Optional;

@ObjectType("JDKV")
public class JdkValuedEntity extends AbstractDomainObject {

    // {{ Title
    public String title() {
        return getStringProperty();
    }

    // }}

    // {{ StringProperty
    private String stringProperty;

    @Optional
    @MemberOrder(sequence = "1")
    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(final String description) {
        this.stringProperty = description;
    }

    // }}

    // {{ JavaUtilDateProperty
    private Date javaUtilDateProperty;

    @Optional
    @MemberOrder(sequence = "1")
    public Date getJavaUtilDateProperty() {
        return javaUtilDateProperty;
    }

    public void setJavaUtilDateProperty(final Date javaUtilDateProperty) {
        this.javaUtilDateProperty = javaUtilDateProperty;
    }

    // }}

    // {{ JavaSqlDateProperty
    private java.sql.Date javaSqlDateProperty;

    @Optional
    @MemberOrder(sequence = "1")
    public java.sql.Date getJavaSqlDateProperty() {
        return javaSqlDateProperty;
    }

    public void setJavaSqlDateProperty(final java.sql.Date javaSqlDateProperty) {
        this.javaSqlDateProperty = javaSqlDateProperty;
    }

    // }}

    // {{ JavaSqlTimestampProperty
    private java.sql.Timestamp javaSqlTimestampProperty;

    @Optional
    @MemberOrder(sequence = "1")
    public java.sql.Timestamp getJavaSqlTimestampProperty() {
        return javaSqlTimestampProperty;
    }

    public void setJavaSqlTimestampProperty(final java.sql.Timestamp javaSqlTimestampProperty) {
        this.javaSqlTimestampProperty = javaSqlTimestampProperty;
    }

    // }}

    // {{ BigIntegerProperty
    private BigInteger bigIntegerProperty;

    @Optional
    @MemberOrder(sequence = "1")
    public BigInteger getBigIntegerProperty() {
        return bigIntegerProperty;
    }

    public void setBigIntegerProperty(final BigInteger bigIntegerProperty) {
        this.bigIntegerProperty = bigIntegerProperty;
    }

    // }}

    // {{ BigDecimalProperty
    private BigDecimal bigDecimalProperty;

    @Optional
    @MemberOrder(sequence = "1")
    public BigDecimal getBigDecimalProperty() {
        return bigDecimalProperty;
    }

    public void setBigDecimalProperty(final BigDecimal bigDecimalProperty) {
        this.bigDecimalProperty = bigDecimalProperty;
    }

    // }}

    // {{ ImageProperty
    private Image imageProperty;

    @Optional
    @MemberOrder(sequence = "1")
    public Image getImageProperty() {
        return imageProperty;
    }

    public void setImageProperty(final Image imageProperty) {
        this.imageProperty = imageProperty;
    }
    // }}

}

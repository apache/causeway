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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import javax.validation.constraints.Digits;
import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Title;

@javax.jdo.annotations.PersistenceCapable
@javax.jdo.annotations.Discriminator("JDKV")
@javax.jdo.annotations.Query(
        name="jdkv_findByStringProperty", language="JDOQL",  
        value="SELECT FROM org.apache.isis.tck.dom.scalars.JdkValuedEntity WHERE stringProperty == :i")
@ObjectType("JDKV")
public class JdkValuedEntity extends AbstractDomainObject {

    // {{ StringProperty (also title, pk)
    private String stringProperty;

    @javax.jdo.annotations.PrimaryKey
    @Title
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

    @javax.jdo.annotations.Persistent() // since not persistent by default
    @Optional
    @MemberOrder(sequence = "1")
    public java.sql.Date getJavaSqlDateProperty() {
        return javaSqlDateProperty;
    }

    public void setJavaSqlDateProperty(final java.sql.Date javaSqlDateProperty) {
        this.javaSqlDateProperty = javaSqlDateProperty;
    }

    // }}

    // {{ JavaSqlTimeProperty (property)
    @javax.jdo.annotations.Persistent() // since not persistent by default
    private java.sql.Time javaSqlTimeProperty;

    @Optional
    @MemberOrder(sequence = "1")
    public java.sql.Time getJavaSqlTimeProperty() {
        return javaSqlTimeProperty;
    }

    public void setJavaSqlTimeProperty(final java.sql.Time javaSqlTimeProperty) {
        this.javaSqlTimeProperty = javaSqlTimeProperty;
    }
    // }}


    
    // {{ JavaSqlTimestampProperty
    @javax.jdo.annotations.Persistent() // since not persistent by default
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

    // {{ BigIntegerProperty (to hold values that are larger than a long)
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

    // {{ BigIntegerProperty2 (to hold values that can also fit into a long)
    private BigInteger bigIntegerProperty2;

    @Optional
    @MemberOrder(sequence = "1")
    public BigInteger getBigIntegerProperty2() {
        return bigIntegerProperty2;
    }

    public void setBigIntegerProperty2(final BigInteger bigIntegerProperty2) {
        this.bigIntegerProperty2 = bigIntegerProperty2;
    }

    // }}

    // {{ BigDecimalProperty (to hold values that are larger than a double)
    private BigDecimal bigDecimalProperty;

    @Digits(integer=20,fraction = 10) // corresponds to big-decimal(30,10)
    @Optional
    @MemberOrder(sequence = "1")
    public BigDecimal getBigDecimalProperty() {
        return bigDecimalProperty;
    }

    public void setBigDecimalProperty(final BigDecimal bigDecimalProperty) {
        this.bigDecimalProperty = bigDecimalProperty;
    }

    // }}


    // {{ BigDecimalProperty (to hold values that are larger than a double)
    private BigDecimal bigDecimalProperty2;

    @Optional
    @MemberOrder(sequence = "1")
    public BigDecimal getBigDecimalProperty2() {
        return bigDecimalProperty2;
    }

    public void setBigDecimalProperty2(final BigDecimal bigDecimalProperty2) {
        this.bigDecimalProperty2 = bigDecimalProperty2;
    }

    // }}


    // {{ MyEnum (property)
    private MyEnum myEnum;

    @javax.jdo.annotations.Persistent
    @Optional
    @MemberOrder(sequence = "1")
    public MyEnum getMyEnum() {
        return myEnum;
    }

    public void setMyEnum(final MyEnum myEnum) {
        this.myEnum = myEnum;
    }
    // }}



}

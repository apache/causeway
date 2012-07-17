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

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;

@javax.jdo.annotations.PersistenceCapable
@javax.jdo.annotations.Discriminator("PRMV")
@javax.jdo.annotations.Query(
        name="prmv_findByIntProperty", language="JDOQL",  
        value="SELECT FROM org.apache.isis.tck.dom.scalars.PrimitiveValuedEntity WHERE intProperty == :i")
@javax.persistence.Entity                    
@javax.persistence.DiscriminatorValue("PRMV")
@ObjectType("PRMV")
public class PrimitiveValuedEntity extends AbstractDomainObject {

    // {{ Id (Integer)
    @javax.jdo.annotations.PrimaryKey
    private Integer id;

    @javax.persistence.Id
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }
    // }}

    // {{ Title
    public String title() {
        return null;
    }

    // }}

    // {{ BooleanProperty
    private boolean booleanProperty;

    @MemberOrder(sequence = "3")
    public boolean getBooleanProperty() {
        return booleanProperty;
    }

    public void setBooleanProperty(final boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    // }}

    // {{ ByteProperty
    private byte byteProperty;

    @MemberOrder(sequence = "1")
    public byte getByteProperty() {
        return byteProperty;
    }

    public void setByteProperty(final byte byteProperty) {
        this.byteProperty = byteProperty;
    }

    // }}

    // {{ ShortProperty
    private short shortProperty;

    @MemberOrder(sequence = "1")
    public short getShortProperty() {
        return shortProperty;
    }

    public void setShortProperty(final short shortProperty) {
        this.shortProperty = shortProperty;
    }

    // }}

    // {{ IntProperty
    private int intProperty;

    @MemberOrder(sequence = "1")
    public int getIntProperty() {
        return intProperty;
    }

    public void setIntProperty(final int intProperty) {
        this.intProperty = intProperty;
    }

    // }}

    // {{ LongProperty
    private long longProperty;

    @MemberOrder(sequence = "1")
    public long getLongProperty() {
        return longProperty;
    }

    public void setLongProperty(final long longProperty) {
        this.longProperty = longProperty;
    }

    // }}

    // {{ FloatProperty
    private float floatProperty;

    @MemberOrder(sequence = "1")
    public float getFloatProperty() {
        return floatProperty;
    }

    public void setFloatProperty(final float floatProperty) {
        this.floatProperty = floatProperty;
    }

    // }}

    // {{ DoubleProperty
    private double doubleProperty;

    @MemberOrder(sequence = "1")
    public double getDoubleProperty() {
        return doubleProperty;
    }

    public void setDoubleProperty(final double doubleProperty) {
        this.doubleProperty = doubleProperty;
    }
    // }}

    
    // {{ CharProperty
    @javax.jdo.annotations.Column(jdbcType="char", length=1) // for hsqldb
    //@javax.jdo.annotations.Column(jdbcType="char", length=1, sqlType="char") // for mssqlserver
    private char charProperty;

    @MemberOrder(sequence = "1")
    public char getCharProperty() {
        return charProperty;
    }

    public void setCharProperty(final char charProperty) {
        this.charProperty = charProperty;
    }
    // }}


}

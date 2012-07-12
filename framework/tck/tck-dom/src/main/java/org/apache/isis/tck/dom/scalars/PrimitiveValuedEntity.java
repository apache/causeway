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

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;

@PersistenceCapable         // for jdo object store
@Discriminator("PRMV")      // for jdo object store
@Entity                     // for jpa object store
@DiscriminatorValue("PRMV") // for jpa object store
@ObjectType("PRMV")
public class PrimitiveValuedEntity extends AbstractDomainObject {

    // {{ Id (Integer)
    private Integer id;

    @Id         // for jpa object store
    @PrimaryKey // for jdo object store
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

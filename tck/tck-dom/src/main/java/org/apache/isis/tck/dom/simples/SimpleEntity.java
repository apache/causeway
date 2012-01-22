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

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optional;

public class SimpleEntity extends AbstractDomainObject {

    // {{ Name (string)
    private String name;

    @MemberOrder(sequence = "1")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // }}

    // {{ Flag (boolean)
    private boolean flag;

    @MemberOrder(sequence = "1")
    public boolean getFlag() {
        return flag;
    }

    public void setFlag(final boolean flag) {
        this.flag = flag;
    }

    // }}

    // {{ AnotherBoolean (Boolean)
    private Boolean anotherBoolean;

    @MemberOrder(sequence = "1")
    @Optional
    public Boolean getAnotherBoolean() {
        return anotherBoolean;
    }

    public void setAnotherBoolean(final Boolean anotherBoolean) {
        this.anotherBoolean = anotherBoolean;
    }

    // }}

    // {{ AnInt (int)
    private int anInt;

    @MemberOrder(sequence = "1")
    public int getAnInt() {
        return anInt;
    }

    public void setAnInt(final int anInt) {
        this.anInt = anInt;
    }

    // }}

    // {{ AnotherInt (Integer)
    private Integer anotherInt;

    @MemberOrder(sequence = "1")
    @Optional
    public Integer getAnotherInt() {
        return anotherInt;
    }

    public void setAnotherInt(final Integer anotherInt) {
        this.anotherInt = anotherInt;
    }

    // }}

    // {{ ALong (long)
    private long aLong;

    @MemberOrder(sequence = "1")
    public long getALong() {
        return aLong;
    }

    public void setALong(final long aLong) {
        this.aLong = aLong;
    }

    // }}

    // {{ AnotherLong
    private Long anotherLong;

    @MemberOrder(sequence = "1")
    @Optional
    public Long getAnotherLong() {
        return anotherLong;
    }

    public void setAnotherLong(final Long anotherLong) {
        this.anotherLong = anotherLong;
    }

    // }}

    // {{ ADouble (double)
    private double aDouble;

    @MemberOrder(sequence = "1")
    public double getADouble() {
        return aDouble;
    }

    public void setADouble(final double aDouble) {
        this.aDouble = aDouble;
    }

    // }}

    // {{ AnotherDouble (Double)
    private Double anotherDouble;

    @MemberOrder(sequence = "1")
    @Optional
    public Double getAnotherDouble() {
        return anotherDouble;
    }

    public void setAnotherDouble(final Double anotherDouble) {
        this.anotherDouble = anotherDouble;
    }

    // }}

    // {{ ABigInteger
    private BigInteger aBigInteger;

    @MemberOrder(sequence = "1")
    @Optional
    public BigInteger getABigInteger() {
        return aBigInteger;
    }

    public void setABigInteger(final BigInteger aBigInteger) {
        this.aBigInteger = aBigInteger;
    }

    // }}

    // {{ ABigDecimal
    private BigDecimal aBigDecimal;

    @MemberOrder(sequence = "1")
    @Optional
    public BigDecimal getABigDecimal() {
        return aBigDecimal;
    }

    public void setABigDecimal(final BigDecimal bigDecimal) {
        this.aBigDecimal = bigDecimal;
    }
    // }}

}

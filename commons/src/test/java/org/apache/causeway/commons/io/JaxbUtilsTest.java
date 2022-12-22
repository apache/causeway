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
package org.apache.causeway.commons.io;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

class JaxbUtilsTest {

    @XmlRootElement(name = "type-a")
    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
    @EqualsAndHashCode
    public static class A {

        @XmlElement(required = false)
        @Getter @Setter private B nested;
    }

    @XmlRootElement(name = "type-b")
    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
    @EqualsAndHashCode
    public static class B {

        @XmlElement(required = false)
        @Getter @Setter private String string;
    }

    /** Works for arbitrary {@link XmlRootElement#name()} combinations,
     * except you cannot use the same {@code name="root"} say on both {@link A} and {@link B}. */
    @Test
    void typesafeUnmarshallingFromAmbiguousContext() {

        // given
        val b = new B();
        b.setString("b-string");
        val a = new A();
        a.setNested(b);

        // when
        val aXml = JaxbUtils.toStringUtf8(a);
        val bXml = JaxbUtils.toStringUtf8(b);

        val aRecovered = JaxbUtils.tryRead(A.class, aXml).ifFailureFail().ifAbsentFail().getValue().get();
        val bRecovered = JaxbUtils.tryRead(B.class, bXml).ifFailureFail().ifAbsentFail().getValue().get();

        assertEquals(a, aRecovered);
        assertEquals(b, bRecovered);

    }

}

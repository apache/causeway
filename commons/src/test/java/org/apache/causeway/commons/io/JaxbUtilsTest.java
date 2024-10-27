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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.TransformerFactory;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.JaxbUtils.JaxbOptions.JaxbOptionsBuilder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

class JaxbUtilsTest {

    @XmlRootElement(name = "type-a")
    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
    @EqualsAndHashCode
    static class A {

        @XmlElement(required = false)
        @Getter @Setter private B nested;
    }

    @XmlRootElement(name = "type-b")
    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
    @EqualsAndHashCode
    static class B {

        @XmlElement(required = false)
        @Getter @Setter private String string;
    }

    private A a;
    private B b;

    @BeforeEach
    void setup() {
        // given
        b = new B();
        b.setString("b-string");
        a = new A();
        a.setNested(b);
    }

    @Nested
    class tryRead {

        /**
         * Works for arbitrary {@link XmlRootElement#name()} combinations,
         * except you cannot use the same {@code name="root"} say on both {@link A} and {@link B}.
         * <p>
         * As {@link A} contains {@link B}, the {@link JAXBContext} for {@link A} should also bind type {@link B}.
         * We are testing whether type-safe recovery especially for type {@link A} works as desired.
         */
        @Test
        void typesafeUnmarshallingFromAmbiguousContext() {

            // when ... doing a round trip
            var aXml = JaxbUtils.toStringUtf8(a);
            var bXml = JaxbUtils.toStringUtf8(b);

            assertTrue(_Strings.isNotEmpty(aXml));
            assertTrue(_Strings.isNotEmpty(bXml));

            var aRecovered = JaxbUtils.tryRead(A.class, aXml).valueAsNonNullElseFail();
            var bRecovered = JaxbUtils.tryRead(B.class, bXml).valueAsNonNullElseFail();

            // then
            assertEquals(a, aRecovered);
            assertEquals(b, bRecovered);

        }
    }

    @Test
    void toStringUtf8_with_no_options() {
        var aXml = JaxbUtils.toStringUtf8(a);
        Approvals.verify(aXml);
    }

    @Test
    void toStringUtf8_with_no_formatted_output() {
        var aXml = JaxbUtils.toStringUtf8(a, opt -> {
            opt.formattedOutput(false);
            return opt;
        });
        Approvals.verify(aXml);
    }

    @Test
    void toStringUtf8_with_indent_number_overridden() {
        var aXml = JaxbUtils.toStringUtf8(a, new JaxbUtils.TransformerFactoryCustomizer() {
            @Override
            public void apply(final TransformerFactory transformerFactory) {
                transformerFactory.setAttribute("indent-number", 2);
            }

            @Override
            public JaxbOptionsBuilder apply(final JaxbOptionsBuilder jaxbOptionsBuilder) {
                return jaxbOptionsBuilder;
            }

        });
        Approvals.verify(aXml);
    }

}

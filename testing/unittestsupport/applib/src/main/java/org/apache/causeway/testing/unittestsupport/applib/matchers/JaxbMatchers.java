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
package org.apache.causeway.testing.unittestsupport.applib.matchers;

import java.io.CharArrayWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import lombok.val;

/**
 * Hamcrest {@link Matcher} implementations for JAXB XML elements.
 *
 * @since 2.0 {@index}
 */
public class JaxbMatchers {

    private JaxbMatchers(){}

    /**
     * Performs an equality comparison of a {@link javax.xml.bind.annotation.XmlRootElement}-annotated class
     * to another by converting into XML first.
     */
    public static <T> Matcher<? super T> isEquivalentTo(final T expected) {
        return new TypeSafeMatcher<T>() {
            @Override
            protected boolean matchesSafely(final T item) {
                final String expectedXml = JaxbUtil2.toXml(expected);
                final String itemXml = JaxbUtil2.toXml(item);
                return Objects.equals(expectedXml, itemXml);
            }

            @Override
            public void describeTo(final org.hamcrest.Description description) {
                final String expectedXml = JaxbUtil2.toXml(expected);
                description.appendText("is equivalent to ").appendValue(expectedXml);
            }
        };
    }

}
class JaxbUtil2 {

    private JaxbUtil2(){}

    static <T> String toXml(final T dto) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(dto, caw);
        return caw.toString();
    }

    static <T> void toXml(final T dto, final Writer writer) {
        try {
            final Class<?> aClass = dto.getClass();
            val m = jaxbContextFor(aClass).createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(dto, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<Class<?>, JAXBContext> jaxbContextByClass = new ConcurrentHashMap<>();

    static <T> JAXBContext jaxbContextFor(final Class<T> dtoClass)  {
        JAXBContext jaxbContext = jaxbContextByClass.get(dtoClass);
        if(jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(dtoClass);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
            jaxbContextByClass.put(dtoClass, jaxbContext);
        }
        return jaxbContext;
    }
}

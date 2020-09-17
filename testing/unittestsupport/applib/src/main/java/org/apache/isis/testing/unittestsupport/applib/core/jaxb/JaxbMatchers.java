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
package org.apache.isis.testing.unittestsupport.applib.core.jaxb;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.resources._Resources;

/**
 * <p>
 *     Used by domain apps only.
 * </p>
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

    public static <T> T fromXml(
            final Reader reader,
            final Class<T> dtoClass) {
        Unmarshaller un = null;
        try {
            un = jaxbContextFor(dtoClass).createUnmarshaller();
            return _Casts.uncheckedCast( un.unmarshal(reader) );
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromXml(
            final Class<?> contextClass,
            final String resourceName,
            final Charset charset,
            final Class<T> dtoClass) throws IOException {
        final String s = _Resources.loadAsString(contextClass, resourceName, charset);
        return fromXml(new StringReader(s), dtoClass);
    }

    public static <T> String toXml(final T dto) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(dto, caw);
        return caw.toString();
    }

    public static <T> void toXml(final T dto, final Writer writer) {
        Marshaller m = null;
        try {
            final Class<?> aClass = dto.getClass();
            m = jaxbContextFor(aClass).createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(dto, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<Class<?>, JAXBContext> jaxbContextByClass = new ConcurrentHashMap<>();

    public static <T> JAXBContext jaxbContextFor(final Class<T> dtoClass)  {
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

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
package org.apache.isis.applib.util;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.commons.internal.resources._Resources;

import lombok.val;


/**
 * Helper methods for converting {@link javax.xml.bind.annotation.XmlRootElement}-annotated class to-and-from XML.
 * Intended primarily for test use only (the {@link JAXBContext} is not cached).
 *
 * <p>
 * For example usage, see <a href="https://github.com/isisaddons/isis-module-publishmq">Isis addons' publishmq module</a>
 * (non-ASF)
 * </p>
 */
public class JaxbUtil {

    private JaxbUtil(){}

    // -- READ

    public static <T> T fromXml(
            final Reader reader,
            final Class<T> dtoClass) {
        Unmarshaller un = null;
        try {
            un = jaxbContextFor(dtoClass).createUnmarshaller();
            return _Casts.uncheckedCast(un.unmarshal(reader));
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromXml(
            final Class<?> contextClass,
            final String resourceName,
            final Class<T> dtoClass) throws IOException {

        val s = _Resources.loadAsStringUtf8(contextClass, resourceName);
        return fromXml(new StringReader(s), dtoClass);
    }

    // -- WRITE

    public static <T> String toXml(final T dto) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(dto, caw);
        return caw.toString();
    }

    public static <T> void toXml(final T dto, final Writer writer) {
        try {
            final Class<?> aClass = dto.getClass();
            final Marshaller m = jaxbContextFor(aClass).createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(dto, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<Class<?>, JAXBContext> jaxbContextByClass = _Maps.newConcurrentHashMap();

    public static <T> JAXBContext jaxbContextFor(final Class<T> dtoClass)  {
        return jaxbContextByClass.computeIfAbsent(dtoClass, JaxbUtil::contextOf );
    }

    private static <T> JAXBContext contextOf(final Class<T> dtoClass) {
        try {
            return JAXBContext.newInstance(dtoClass);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}

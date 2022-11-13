/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.executionoutbox.restclient.api;

import java.io.CharArrayWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;



/**
 * Helper methods for converting {@link jakarta.xml.bind.annotation.XmlRootElement}-annotated class to-and-from XML.  Intended primarily for
 * test use only (the {@link JAXBContext} is not cached).
 *
 * <p>
 * For example usage, see <a href="https://github.com/causewayaddons/causeway-module-publishmq">Causeway addons' publishmq module</a> (non-ASF)
 * </p>
 */
class _Jaxb {

    private _Jaxb(){}

    static <T> T fromXml(
            final Reader reader,
            final Class<T> dtoClass) {
        Unmarshaller un = null;
        try {
            un = jaxbContextFor(dtoClass).createUnmarshaller();
            return (T) un.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> String toXml(final T dto) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(dto, caw);
        return caw.toString();
    }

    static <T> void toXml(final T dto, final Writer writer) {
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

    private static <T> JAXBContext jaxbContextFor(final Class<T> dtoClass)  {
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

/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.util;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.common.io.Resources;

/**
 * Helper methods for converting {@link javax.xml.bind.annotation.XmlRootElement}-annotated class to-and-from XML.  Intended primarily for
 * test use only (the {@link JAXBContext} is not cached).
 *
 * <p>
 * For example usage, see <a href="https://github.com/isisaddons/isis-module-publishmq">Isis addons' publishmq module</a> (non-ASF)
 * </p>
 */
public class JaxbUtil {

    private JaxbUtil(){}

    public static <T> T fromXml(
            final Reader reader,
            final Class<T> dtoClass) {
        Unmarshaller un = null;
        try {
            un = getJaxbContext(dtoClass).createUnmarshaller();
            return (T) un.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromXml(
            final Class<?> contextClass,
            final String resourceName,
            final Charset charset,
            final Class<T> dtoClass) throws IOException {
        final URL url = Resources.getResource(contextClass, resourceName);
        final String s = Resources.toString(url, charset);
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
            m = getJaxbContext(aClass).createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(dto, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private static JAXBContext getJaxbContext(Class<?> dtoClass) {
        try {
            return JAXBContext.newInstance(dtoClass);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}

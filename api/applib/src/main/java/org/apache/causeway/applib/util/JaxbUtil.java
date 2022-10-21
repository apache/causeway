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
package org.apache.causeway.applib.util;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.commons.internal.resources._Xml;
import org.apache.causeway.commons.internal.resources._Xml.ReadOptions;
import org.apache.causeway.commons.internal.resources._Xml.WriteOptions;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;


/**
 * Helper methods for converting {@link javax.xml.bind.annotation.XmlRootElement}-annotated class to-and-from XML.
 *
 * <p>
 * For example usage, see <a href="https://github.com/causewayaddons/causeway-module-publishmq">Causeway addons' publishmq module</a>
 * (non-ASF)
 * </p>
 * @since 2.0 {@index}
 */
@UtilityClass
public class JaxbUtil {

    // -- READ

    private static <T> T _fromXml(
            final @NonNull Reader reader,
            final @NonNull Class<T> dtoClass) {

        return _Xml._readXml(dtoClass, reader, ReadOptions.builder()
                .useContextCache(true)
                .build());
    }

    public static <T> Try<T> fromXml(
            final @NonNull Reader reader,
            final @NonNull Class<T> dtoClass) {

        return Try.call(()->_fromXml(reader, dtoClass));
    }

    private static <T> T _fromXml(
            final @NonNull Class<?> contextClass,
            final @NonNull String resourceName,
            final @NonNull Class<T> dtoClass) throws IOException {

        val xmlString = _Resources.loadAsStringUtf8(contextClass, resourceName);
        return _fromXml(new StringReader(xmlString), dtoClass);
    }

    public static <T> Try<T> fromXml(
            final @NonNull Class<?> contextClass,
            final @NonNull String resourceName,
            final @NonNull Class<T> dtoClass) throws IOException {

        return Try.call(()->_fromXml(contextClass, resourceName, dtoClass));
    }

    // -- WRITE

    public static Try<String> toXml(final @NonNull Object dto) {
        return Try.call(()->{
            val caw = new CharArrayWriter();
            toXml(dto, caw);
            return caw.toString();
        });
    }

    public static <T> void toXml(
            final @NonNull T dto,
            final @NonNull Writer writer) throws JAXBException {
        _Xml.writeXml(dto, writer, WriteOptions.builder()
                .useContextCache(true)
                .formattedOutput(true)
                .build());
    }

    // -- CACHING

    public static JAXBContext jaxbContextFor(final @NonNull Class<?> dtoClass) {
        val useCache = true;
        return _Xml.jaxbContextFor(dtoClass, useCache);
    }

}

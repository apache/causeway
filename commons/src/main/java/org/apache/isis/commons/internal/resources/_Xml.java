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
package org.apache.isis.commons.internal.resources;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.codec._DocumentFactories;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.reflection._Annotations;

import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Utilities for the XML format.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package!
 * <br/>
 * These may be changed or removed without notice!
 * @since 2.0
 */
public final class _Xml {
    
    // -- OPTIONS
    
    @Value @Builder
    public static class ReadOptions {
        private final @Builder.Default boolean useContextCache = false;
        private final @Builder.Default boolean allowMissingRootElement = false;
        
        public static ReadOptions defaults() {
            return ReadOptions.builder().build();
        }
    }
    
    @Value @Builder
    public static class WriteOptions {
        private final @Builder.Default boolean useContextCache = false;
        private final @Builder.Default boolean formattedOutput = false;
        private final @Builder.Default boolean allowMissingRootElement = false;
        
        public static WriteOptions defaults() {
            return WriteOptions.builder().build();
        }
    }
    
    // -- READ

    @SneakyThrows
    public static <T> T readXml(
            final @NonNull Class<T> dtoClass,
            final @NonNull Reader reader,
            final @NonNull ReadOptions readOptions) {
        
        val unmarshaller = jaxbContextFor(dtoClass, readOptions.isUseContextCache()).createUnmarshaller();
        
        if(readOptions.isAllowMissingRootElement()
                && !_Annotations.isPresent(dtoClass, XmlRootElement.class)) {
            val xsr = _DocumentFactories.xmlInputFactory().createXMLStreamReader(reader);
            final JAXBElement<T> userElement = unmarshaller.unmarshal(xsr, dtoClass);
            return userElement.getValue();            
        }
        
        return _Casts.uncheckedCast(unmarshaller.unmarshal(reader));
    }

    // -- WRITE

    public static <T> String writeXml(
            final @NonNull T dto,
            final @NonNull WriteOptions writeOptions) {
        val writer = new StringWriter();
        writeXml(dto, writer, writeOptions);
        return writer.toString();
    }

    @SneakyThrows
    public static <T> void writeXml(
            final @NonNull T dto, 
            final @NonNull Writer writer,
            final @NonNull WriteOptions writeOptions) {
        
        val dtoClass = _Casts.<Class<T>>uncheckedCast(dto.getClass());
        val marshaller = jaxbContextFor(dtoClass, writeOptions.useContextCache).createMarshaller();
        if(writeOptions.isFormattedOutput()) {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        }
        if(writeOptions.isAllowMissingRootElement()
            && !_Annotations.isPresent(dtoClass, XmlRootElement.class)) {
            val qName = new QName("", dtoClass.getSimpleName());
            val jaxbElement = new JAXBElement<T>(qName, dtoClass, null, dto);
            marshaller.marshal(jaxbElement, writer);
        } else {
            marshaller.marshal(dto, writer);    
        }
    }

    // -- CLONE
    public static <T> T clone(final @Nullable T dto) {
        if(dto==null) {
            return dto;
        }
        val type = _Casts.<Class<T>>uncheckedCast(dto.getClass());
        val writer = new StringWriter();
        writeXml(dto, writer,  WriteOptions.builder()
                .useContextCache(true)
                .formattedOutput(false)
                .allowMissingRootElement(true)
                .build());
        val reader = new StringReader(writer.toString());
        return readXml(type, reader, ReadOptions.builder()
                .useContextCache(true)
                .allowMissingRootElement(true)
                .build());
    }
    
    
    // -- ENHANCE EXCEPTION MESSAGE IF POSSIBLE
    
    public static Exception verboseException(String doingWhat, @Nullable Class<?> dtoClass, Exception e) {
        
        val dtoClassName = Optional.ofNullable(dtoClass).map(Class::getName).orElse("unknown");
        
        if(isIllegalAnnotationsException(e)) {
            // report a better error if possible
            // this is done reflectively because on JDK 8 this exception type is only provided by Oracle JDK 
            try {
                
                val errors = _Casts.<List<? extends Exception>>uncheckedCast(
                        e.getClass().getMethod("getErrors").invoke(e));

                if(_NullSafe.size(errors)>0) {
                    
                    return _Exceptions.unrecoverable(String.format("Error %s, "
                            + "due to illegal annotations on object class '%s'; "
                            + "%d error(s) reported: %s",
                            doingWhat,
                            dtoClassName,
                            errors.size(),
                            errors.stream()
                                .map(Exception::getMessage)
                                .collect(Collectors.joining("; "))),
                            e);
                }

            } catch (Exception ex) {
                // just fall through if we hit any issues
            }
        }

        return _Exceptions.unrecoverable(String.format("Error %s; "
                + "object class is '%s'", doingWhat, dtoClassName), e);
    }
    
    private static boolean isIllegalAnnotationsException(Exception e) {
        /*sonar-ignore-on*/
        return "com.sun.xml.bind.v2.runtime.IllegalAnnotationsException".equals(e.getClass().getName());
        /*sonar-ignore-off*/
    }
    
    // -- JAXB CONTEXT CACHE

    private static Map<Class<?>, JAXBContext> jaxbContextByClass = _Maps.newConcurrentHashMap();

    public static <T> JAXBContext jaxbContextFor(final Class<T> dtoClass, final boolean useCache)  {
        return useCache
                ? jaxbContextByClass.computeIfAbsent(dtoClass, _Xml::contextOf)
                : contextOf(dtoClass);
    }

    @SneakyThrows
    private static <T> JAXBContext contextOf(final Class<T> dtoClass) {
        try {
            return JAXBContext.newInstance(dtoClass);
        } catch (Exception e) {
            throw verboseException("obtaining JAXBContext for class", dtoClass, e);
        }
    }

}

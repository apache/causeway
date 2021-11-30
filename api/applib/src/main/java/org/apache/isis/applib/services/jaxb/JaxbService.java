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
package org.apache.isis.applib.services.jaxb;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.resources._Xml;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

/**
 * Allows instances of JAXB-annotated classes to be marshalled to XML and
 * unmarshalled from XML back into domain objects.
 *
 * <p>
 *     The default implementation automatically caches the JAXB marshallers
 *     by target class.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface JaxbService {

    /**
     * unmarshalls the XML into an instance of the class, using
     * the provided {@link JAXBContext}.
     *
     * @param jaxbContext  - configured for the expected target class
     * @param xml
     */
    default Object fromXml(
            final JAXBContext jaxbContext,
            final String xml) {
        return fromXml(jaxbContext, xml, null);
    }

    /**
     * unmarshalls the XML into an instance of the class, using the
     * provided {@link JAXBContext} and the additional properties.
     *
     * @param jaxbContext - configured for the expected target class
     * @param xml
     * @param unmarshallerProperties
     */
    Object fromXml(
            JAXBContext jaxbContext,
            String xml,
            @Nullable Map<String,Object> unmarshallerProperties);

    /**
     * Unmarshalls the XML to the specified domain class.
     */
    default <T> T fromXml(final Class<T> domainClass, final String xml) {
        return fromXml(domainClass, xml, null);
    }

    /**
     * Unmarshalls the XML to the specified domain class, with additional
     * properties passed through to the {@link JAXBContext} used to performed
     * the unmarshalling.
     */
    <T> T fromXml(
            Class<T> domainClass,
            String xml,
            @Nullable Map<String,Object> unmarshallerProperties);

    /**
     * Marshalls the object into XML (using a {@link JAXBContext} for the
     * object's class).
     */
    default String toXml(final Object domainObject) {
        return toXml(domainObject, null);
    }

    /**
     * Marshalls the object into XML specifying additional properties (passed
     * to the {@link JAXBContext} used for the object's class).
     */
    String toXml(
            Object domainObject,
            @Nullable Map<String,Object> marshallerProperties);

    /**
     * Generates a map of each of the schemas referenced; the key is the
     * schema namespace, the value is the XML of the schema itself.
     *
     * <p>
     *     A JAXB-annotated domain object will live in its own XSD namespace
     *     and may reference multiple other XSD schemas.
     *     In particular, many JAXB domain objects will reference the common
     *     isis schemas.  The {@link IsisSchemas} paramter indicates whether
     *     these schemas should be included or excluded from the map.
     * </p>
     *
     * @param domainObject
     * @param isisSchemas
     */
    Map<String, String> toXsd(
            Object domainObject,
            IsisSchemas isisSchemas);


    class Simple implements JaxbService {

        @Override
        @SneakyThrows
        @Nullable
        public final Object fromXml(
                final @NonNull JAXBContext jaxbContext,
                final @Nullable String xml,
                final @Nullable Map<String, Object> unmarshallerProperties) {
            try {
                return internalFromXml(jaxbContext, xml, unmarshallerProperties);
            } catch (Exception e) {
                throw _Xml.verboseException("unmarshalling XML", null, e);
            }
        }

        @Override
        @SneakyThrows
        @Nullable
        public final <T> T fromXml(
                final @NonNull Class<T> domainClass,
                final @Nullable String xml,
                final @Nullable Map<String, Object> unmarshallerProperties) {

            try {
                val jaxbContext = jaxbContextForClass(domainClass);
                return _Casts.uncheckedCast(internalFromXml(jaxbContext, xml, unmarshallerProperties));
            } catch (Exception e) {
                throw _Xml.verboseException("unmarshalling XML", domainClass, e);
            }
        }

        @Override
        @SneakyThrows
        public final String toXml(
                final @NonNull Object domainObject,
                final @Nullable Map<String, Object> marshallerProperties) {

            val domainClass = domainObject.getClass();
            val jaxbContext = jaxbContextForObject(domainObject);
            try {
                val marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                for (val entry : _NullSafe.entrySet(marshallerProperties)) {
                    marshaller.setProperty(entry.getKey(), entry.getValue());
                }

                configure(marshaller);

                val writer = new StringWriter();
                marshaller.marshal(domainObject, writer);
                val xml = writer.toString();

                return xml;

            } catch (Exception e) {
                throw _Xml.verboseException("marshalling domain object to XML", domainClass, e);
            }
        }

        /**
         * Optional hook
         */
        protected JAXBContext jaxbContextForObject(final @NonNull Object domainObject) {
            val useCache = true;
            return _Xml.jaxbContextFor(domainObject.getClass(), useCache);
        }

        /**
         * Optional hook
         */
        protected JAXBContext jaxbContextForClass(final @NonNull Class<?> domainObjectClass) {
            val useCache = true;
            return _Xml.jaxbContextFor(domainObjectClass, useCache);
        }

        /**
         * Optional hook
         */
        protected void configure(final Unmarshaller unmarshaller) {
        }

        /**
         * Optional hook
         */
        protected void configure(final Marshaller marshaller) {
        }

        @Nullable
        protected Object internalFromXml(
                final @NonNull JAXBContext jaxbContext,
                final @Nullable String xml,
                final @Nullable Map<String, Object> unmarshallerProperties) throws JAXBException {

            if (xml == null) {
                return null;
            }

            val unmarshaller = jaxbContext.createUnmarshaller();

            for (val entry : _NullSafe.entrySet(unmarshallerProperties)) {
                unmarshaller.setProperty(entry.getKey(), entry.getValue());
            }

            configure(unmarshaller);

            val pojo = unmarshaller.unmarshal(new StringReader(xml));
            return pojo;
        }

        @Override
        @SneakyThrows
        public final Map<String, String> toXsd(
                final @NonNull Object domainObject,
                final @NonNull IsisSchemas isisSchemas) {

            val jaxbContext = jaxbContextForObject(domainObject);

            val outputResolver = new CatalogingSchemaOutputResolver(isisSchemas);
            jaxbContext.generateSchema(outputResolver);

            return outputResolver.asMap();
        }

    }
}

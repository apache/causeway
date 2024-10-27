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
package org.apache.causeway.applib.services.jaxb;

import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.domain.DomainObjectList;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.io.JaxbUtils;

import lombok.NonNull;
import lombok.SneakyThrows;

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
     *     causeway schemas.  The {@link CausewaySchemas} paramter indicates whether
     *     these schemas should be included or excluded from the map.
     * </p>
     *
     * @param domainObject
     * @param causewaySchemas
     */
    Map<String, String> toXsd(
            Object domainObject,
            CausewaySchemas causewaySchemas);

    /** 'Simple' because no injection point resolving or advanced {@link DomainObjectList} handling. */
    class Simple implements JaxbService {

        @Override
        @Nullable
        public final <T> T fromXml(
                final @NonNull Class<T> domainClass,
                final @Nullable String xml,
                final @Nullable Map<String, Object> unmarshallerProperties) {

            if (xml == null) {
                return null;
            }
            return JaxbUtils.tryRead(domainClass, xml, opts->{
                for (var entry : _NullSafe.entrySet(unmarshallerProperties)) {
                    opts.property(entry.getKey(), entry.getValue());
                }
                opts.unmarshallerConfigurer(this::configure);
                return opts;
            })
            .ifFailureFail()
            .getValue().orElse(null);
        }

        @Override
        public final String toXml(
                final @NonNull Object domainObject,
                final @Nullable Map<String, Object> marshallerProperties) {

            var jaxbContext = domainObject instanceof DomainObjectList
                    ? jaxbContextForList((DomainObjectList)domainObject)
                    : JaxbUtils.jaxbContextFor(domainObject.getClass(), true);

            return Try.call(()->JaxbUtils.toStringUtf8(domainObject, opts->{
                for (var entry : _NullSafe.entrySet(marshallerProperties)) {
                    opts.property(entry.getKey(), entry.getValue());
                }
                opts.marshallerConfigurer(this::configure);
                opts.jaxbContextOverride(jaxbContext);
                return opts;
            }))
            .ifFailureFail()
            .getValue().orElse(null);
        }

        /**
         * Optional hook
         */
        protected JAXBContext jaxbContextForList(final @NonNull DomainObjectList list) {
            return JaxbUtils.jaxbContextFor(DomainObjectList.class, true);
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

        @Override
        @SneakyThrows
        public final Map<String, String> toXsd(
                final @NonNull Object domainObject,
                final @NonNull CausewaySchemas causewaySchemas) {

            var jaxbContext = JaxbUtils.jaxbContextFor(domainObject.getClass(), true);

            var outputResolver = new CatalogingSchemaOutputResolver(causewaySchemas);
            jaxbContext.generateSchema(outputResolver);

            return outputResolver.asMap();
        }

    }
}

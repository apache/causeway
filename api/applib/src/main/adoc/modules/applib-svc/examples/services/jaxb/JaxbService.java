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

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.isis.applib.mixins.dto.Dto_downloadXsd;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.resources._Xml;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

// tag::refguide[]
public interface JaxbService {

    default Object fromXml(                             // <.>
            JAXBContext jaxbContext,
            String xml) {
        return fromXml(jaxbContext, xml, null);
    }

    Object fromXml(                                     // <.>
            JAXBContext jaxbContext,
            String xml,
            @Nullable Map<String,Object> unmarshallerProperties);

    // end::refguide[]
    /**
     * As {@link #fromXml(JAXBContext, String)}, but downcast to a specific type.
     */
    // tag::refguide[]
    default <T> T fromXml(Class<T> domainClass, String xml) { // <.>
        return fromXml(domainClass, xml, null);
    }

    // end::refguide[]
    /**
     * As {@link #fromXml(JAXBContext, String, Map)}, but downcast to a specific type.
     */
    // tag::refguide[]
    <T> T fromXml(                                      // <.>
            Class<T> domainClass,
            String xml,
            @Nullable Map<String,Object> unmarshallerProperties);

    default String toXml(Object domainObject) {         // <.>
        return toXml(domainObject, null);
    }
        
    String toXml(                                       // <.>
            Object domainObject,
            @Nullable Map<String,Object> marshallerProperties);

    // end::refguide[]
    /**
     * Controls whether, when generating {@link #toXsd(Object, IsisSchemas) XML schemas},
     * any of the common Isis schemas (in the namespace <code>http://org.apache.isis.schema</code>) should be included
     * or just ignored (and therefore don't appear in the returned map).
     *
     * <p>
     *     The practical benefit of this is that for many DTOs there will only be one other
     *     schema, that of the DTO itself.  The {@link Dto_downloadXsd} mixin uses this to return that single XSD,
     *     rather than generating a ZIP of two schemas (the Isis schema and the one for the DTO), as it would otherwise;
     *     far more convenient when debugging and so on.  The Isis schemas can always be
     *     <a href="http://isis.apache.org/schema">downloaded</a> from the Isis website.
     * </p>
     */
    // tag::refguide[]
    enum IsisSchemas {
        INCLUDE,
        IGNORE;
        // end::refguide[]

        /**
         * Implementation note: not using subclasses, otherwise the key in translations.po becomes more complex.
         */
        public boolean shouldIgnore(final String namespaceUri) {
            if(this == INCLUDE) {
                return false;
            } else {
                return namespaceUri.matches(".*isis\\.apache\\.org.*");
            }
        }
        // tag::refguide[]
    }

    Map<String, String> toXsd(                 // <.>
            Object domainObject, 
            IsisSchemas isisSchemas);

    // end::refguide[]
    class Simple implements JaxbService {

        @Override @SneakyThrows @Nullable
        public Object fromXml(
                final @NonNull JAXBContext jaxbContext, 
                final @Nullable String xml, 
                final @Nullable Map<String, Object> unmarshallerProperties) {
            try {
                return internalFromXml(jaxbContext, xml, unmarshallerProperties);
            } catch (Exception e) {
                throw _Xml.verbose("unmarshalling XML", null, e);
            }
        }

        @Override @SneakyThrows @Nullable
        public <T> T fromXml(
                final @NonNull Class<T> domainClass, 
                final @Nullable String xml, 
                final @Nullable Map<String, Object> unmarshallerProperties) {
            
            try {
                val jaxbContext = jaxbContextForClass(domainClass);
                return _Casts.uncheckedCast(internalFromXml(jaxbContext, xml, unmarshallerProperties));
            } catch (Exception e) {
                throw _Xml.verbose("unmarshalling XML", domainClass, e);
            }
        }

        @Override @SneakyThrows
        public String toXml(
                final @NonNull Object domainObject, 
                final @Nullable Map<String, Object> marshallerProperties)  {

            val domainClass = domainObject.getClass();
            val jaxbContext = jaxbContextForObject(domainObject);
            try {
                val marshaller = jaxbContext.createMarshaller();

                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                if(!_NullSafe.isEmpty(marshallerProperties)) {
                    for (val entry : marshallerProperties.entrySet()) {
                        marshaller.setProperty(entry.getKey(), entry.getValue());
                    }
                }

                configure(marshaller);

                val writer = new StringWriter();
                marshaller.marshal(domainObject, writer);
                val xml = writer.toString();

                return xml;

            } catch (Exception e) {
                throw _Xml.verbose("marshalling domain object to XML", domainClass, e);
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
            
            if(xml==null) {
                return null;
            }
            
            val unmarshaller = jaxbContext.createUnmarshaller();
            if(!_NullSafe.isEmpty(unmarshallerProperties)) {
                for (val entry : unmarshallerProperties.entrySet()) {
                    unmarshaller.setProperty(entry.getKey(), entry.getValue());
                }
            }
            configure(unmarshaller);

            val pojo = unmarshaller.unmarshal(new StringReader(xml));
            return pojo;
        }
        
        @Override @SneakyThrows
        public Map<String,String> toXsd(
                final @NonNull Object domainObject, 
                final @NonNull IsisSchemas isisSchemas) {

            val jaxbContext = jaxbContextForObject(domainObject);

            val outputResolver = new CatalogingSchemaOutputResolver(isisSchemas);
            jaxbContext.generateSchema(outputResolver);

            return outputResolver.asMap();
        }
        
    }

    // tag::refguide[]
}
// end::refguide[]

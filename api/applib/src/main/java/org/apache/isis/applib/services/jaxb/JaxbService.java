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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.mixins.dto.Dto_downloadXsd;
import org.apache.isis.applib.util.JaxbUtil;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Maps;

import lombok.NonNull;

// tag::refguide[]
public interface JaxbService {

    Object fromXml(                                     // <.>
            JAXBContext jaxbContext,
            String xml);

    Object fromXml(                                     // <.>
            JAXBContext jaxbContext,
            String xml,
            Map<String,Object> unmarshallerProperties);

    // end::refguide[]
    /**
     * As {@link #fromXml(JAXBContext, String)}, but downcast to a specific type.
     */
    // tag::refguide[]
    <T> T fromXml(Class<T> domainClass, String xml);    // <.>

    // end::refguide[]
    /**
     * As {@link #fromXml(JAXBContext, String, Map)}, but downcast to a specific type.
     */
    // tag::refguide[]
    <T> T fromXml(                                      // <.>
            Class<T> domainClass,
            String xml,
            Map<String,Object> unmarshallerProperties);

    String toXml(final Object domainObject);            // <.>

    String toXml(                                       // <.>
            final Object domainObject,
            Map<String,Object> marshallerProperties);

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

    Map<String, String> toXsd(                          // <.>
            final Object domainObject,
            final IsisSchemas isisSchemas);

    // end::refguide[]
    class Simple implements JaxbService {

        @Override
        public Object fromXml(final JAXBContext jaxbContext, final String xml) {
            return fromXml(jaxbContext, xml, _Maps.newHashMap());
        }

        @Override
        public Object fromXml(final JAXBContext jaxbContext, final String xml, final Map<String, Object> unmarshallerProperties) {
            try {

                return internalFromXml(jaxbContext, xml, unmarshallerProperties);

            } catch (final JAXBException ex) {
                throw new NonRecoverableException("Error unmarshalling XML", ex);
            }
        }

        protected Object internalFromXml(
                @NonNull final JAXBContext jaxbContext,
                @Nullable final String xml,
                @Nullable final Map<String, Object> unmarshallerProperties) throws JAXBException {
            
            if(xml==null) {
                return null;
            }
            
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            if(unmarshallerProperties!=null) {
                for (Map.Entry<String, Object> entry : unmarshallerProperties.entrySet()) {
                    unmarshaller.setProperty(entry.getKey(), entry.getValue());
                }
            }

            configure(unmarshaller);

            return unmarshaller.unmarshal(new StringReader(xml));
        }

        @Override
        public <T> T fromXml(final Class<T> domainClass, final String xml) {
            return fromXml(domainClass, xml, _Maps.newHashMap());
        }

        @Override
        public <T> T fromXml(final Class<T> domainClass, final String xml, final Map<String, Object> unmarshallerProperties) {
            final JAXBContext context = jaxbContextFor(domainClass);
            return _Casts.uncheckedCast(fromXml(context, xml, unmarshallerProperties));
        }

        private static <T> JAXBContext jaxbContextFor(final Class<T> clazz)  {
            try {
                return JaxbUtil.jaxbContextFor(clazz);
            } catch (RuntimeException e) {
                throw new NonRecoverableException("Error obtaining JAXBContext for class '" + clazz + "'", e.getCause());
            }
        }

        @Override
        public String toXml(final Object domainObject) {
            return toXml(domainObject, _Maps.newHashMap());
        }

        @Override
        public String toXml(final Object domainObject, final Map<String, Object> marshallerProperties)  {

            final Class<?> domainClass = domainObject.getClass();
            final JAXBContext context = jaxbContextFor(domainObject);
            try {
                final Marshaller marshaller = context.createMarshaller();

                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                for (Map.Entry<String, Object> entry : marshallerProperties.entrySet()) {
                    marshaller.setProperty(entry.getKey(), entry.getValue());
                }

                configure(marshaller);

                final StringWriter sw = new StringWriter();
                marshaller.marshal(domainObject, sw);
                final String xml = sw.toString();

                return xml;

            } catch (final JAXBException ex) {
                final Class<? extends JAXBException> exClass = ex.getClass();

                final String name = exClass.getName();
                if(name.equals("com.sun.xml.bind.v2.runtime.IllegalAnnotationsException")) {
                    // report a better error if possible
                    // this is done reflectively so as to not have to bring in a new Maven dependency
                    List<? extends Exception> errors = null;
                    String annotationExceptionMessages = null;
                    try {
                        final Method getErrorsMethod = exClass.getMethod("getErrors");
                        errors = _Casts.uncheckedCast(getErrorsMethod.invoke(ex));

                        annotationExceptionMessages = ": " +
                                _NullSafe.stream(errors)
                        .map(Exception::getMessage)
                        .collect(Collectors.joining("; "));

                    } catch (Exception e) {
                        // fall through if we hit any snags, and instead throw the more generic error message.
                    }
                    if(errors != null) {
                        throw new NonRecoverableException(
                                "Error marshalling domain object to XML, due to illegal annotations on domain object class '"
                                        + domainClass.getName() + "'; " + errors.size() + " error"
                                        + (errors.size() == 1? "": "s")
                                        + " reported" + (!errors
                                                .isEmpty() ? annotationExceptionMessages : ""), ex);
                    }
                }

                throw new NonRecoverableException("Error marshalling domain object to XML; domain object class is '" + domainClass.getName() + "'", ex);
            }
        }

        /**
         * Optional hook
         */
        protected JAXBContext jaxbContextFor(final Object domainObject) {
            final Class<?> domainClass = domainObject.getClass();
            return jaxbContextFor(domainClass);
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
        public Map<String,String> toXsd(final Object domainObject, final IsisSchemas isisSchemas) {

            try {
                final Class<?> domainClass = domainObject.getClass();
                final JAXBContext context = jaxbContextFor(domainClass);

                final CatalogingSchemaOutputResolver outputResolver = new CatalogingSchemaOutputResolver(isisSchemas);
                context.generateSchema(outputResolver);

                return outputResolver.asMap();
            } catch (final IOException ex) {
                throw new ApplicationException(ex);
            }
        }
    }

    // tag::refguide[]
}
// end::refguide[]

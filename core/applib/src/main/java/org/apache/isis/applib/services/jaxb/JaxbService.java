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
package org.apache.isis.applib.services.jaxb;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.dto.Dto_downloadXsd;

public interface JaxbService {

    @Programmatic
    Object fromXml(JAXBContext jaxbContext, String xml);

    @Programmatic
    Object fromXml(JAXBContext jaxbContext, String xml, Map<String,Object> unmarshallerProperties);

    /**
     * As {@link #fromXml(JAXBContext, String)}, but downcast to a specific type.
     */
    @Programmatic
    <T> T fromXml(Class<T> domainClass, String xml);

    /**
     * As {@link #fromXml(JAXBContext, String, Map)}, but downcast to a specific type.
     */
    @Programmatic
    <T> T fromXml(Class<T> domainClass, String xml, Map<String,Object> unmarshallerProperties);

    @Programmatic
    String toXml(final Object domainObject);

    @Programmatic
    String toXml(final Object domainObject, Map<String,Object> marshallerProperties);


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
    enum IsisSchemas {
        INCLUDE,
        IGNORE;

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
    }

    @Programmatic
    Map<String, String> toXsd(final Object domainObject, final IsisSchemas isisSchemas);


    public static class Simple implements JaxbService {

        @Override
        public Object fromXml(final JAXBContext jaxbContext, final String xml) {
            return fromXml(jaxbContext, xml, Maps.<String,Object>newHashMap());
        }
        @Override
        public Object fromXml(final JAXBContext jaxbContext, final String xml, final Map<String, Object> unmarshallerProperties) {
            try {

                final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                for (Map.Entry<String, Object> entry : unmarshallerProperties.entrySet()) {
                    unmarshaller.setProperty(entry.getKey(), entry.getValue());
                }

                configure(unmarshaller);

                final Object unmarshal = unmarshaller.unmarshal(new StringReader(xml));
                return unmarshal;

            } catch (final JAXBException ex) {
                throw new NonRecoverableException("Error unmarshalling XML", ex);
            }
        }

        @Override
        public <T> T fromXml(final Class<T> domainClass, final String xml) {
            return fromXml(domainClass, xml, Maps.<String,Object>newHashMap());
        }
        @Override
        public <T> T fromXml(final Class<T> domainClass, final String xml, final Map<String, Object> unmarshallerProperties) {
            try {
                final JAXBContext context = JAXBContext.newInstance(domainClass);
                return (T) fromXml(context, xml, unmarshallerProperties);

            } catch (final JAXBException ex) {
                throw new NonRecoverableException("Error unmarshalling XML to class '" + domainClass.getName() + "'", ex);
            }
        }

        @Override
        public String toXml(final Object domainObject) {
            return toXml(domainObject, Maps.<String,Object>newHashMap());
        }

        @Override
        public String toXml(final Object domainObject, final Map<String, Object> marshallerProperties)  {

            final Class<?> domainClass = domainObject.getClass();
            try {
                final JAXBContext context = JAXBContext.newInstance(domainClass);

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
                        errors = (List<? extends Exception>) getErrorsMethod.invoke(ex);
                        annotationExceptionMessages = ": " + Joiner.on("; ").join(
                                Iterables.transform(errors, new Function<Exception, String>() {
                                    @Override public String apply(final Exception e) {
                                        return e.getMessage();
                                    }
                                }));
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
        protected void configure(final Unmarshaller unmarshaller) {
        }

        /**
         * Optional hook
         */
        protected void configure(final Marshaller marshaller) {
        }

        public Map<String,String> toXsd(final Object domainObject, final IsisSchemas isisSchemas) {

            try {
                final Class<?> domainClass = domainObject.getClass();
                final JAXBContext context = JAXBContext.newInstance(domainClass);

                final CatalogingSchemaOutputResolver outputResolver = new CatalogingSchemaOutputResolver(isisSchemas);
                context.generateSchema(outputResolver);

                return outputResolver.asMap();
            } catch (final JAXBException | IOException ex) {
                throw new ApplicationException(ex);
            }
        }


        @javax.inject.Inject
        DomainObjectContainer container;
    }


}
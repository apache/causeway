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
package org.apache.isis.core.runtime.services.jaxb;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.runtime.services.jaxb.util.CatalogingSchemaOutputResolver;
import org.apache.isis.schema.utils.jaxbadapters.PersistentEntityAdapter;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class JaxbServiceDefault implements JaxbService {

    /**
     * This boolean flag controls whether, when generating {@link #toXsd(Object) XML schemas},
     * any of the common Isis schemas (in the namespace <code>http://org.apache.isis.schema</code>) should be included or just ignored (and therefore don't appear in the ZIP file).
     *
     * <p>
     *     The practical benefit of this is that for many DTOs there will only be one other
     *     schema, that of the DTO itself.  Rather than generating a ZIP of two schemas (the Isis
     *     schema and the one for the DTO), the {@link #toXsd(Object) toXsd} method will instead
     *     return a single XSD file; far more convenient when debugging and so on.
     *     The Isis schemas meanwhile can always be <a href="http://isis.apache.org/schema">downloaded from the website </a>.
     * </p>
     */
    public static final String INCLUDE_ISIS_SCHEMA = "isis.services.jaxb.includeIsisSchema";

    private boolean includeIsisSchema;

    @PostConstruct
    public void init(Map<String,String> props) {
        final String prop = props.get(INCLUDE_ISIS_SCHEMA);
        this.includeIsisSchema = prop != null && Boolean.parseBoolean(prop);

    }

    @Override
    public <T> T fromXml(final Class<T> domainClass, final String xml) {
        try {
            final JAXBContext context = JAXBContext.newInstance(domainClass);

            final PersistentEntityAdapter adapter = new PersistentEntityAdapter();
            container.injectServicesInto(adapter);

            final Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setAdapter(PersistentEntityAdapter.class, adapter);

            final Object unmarshal = unmarshaller.unmarshal(new StringReader(xml));
            return (T) unmarshal;

        } catch (final JAXBException ex) {
            throw new NonRecoverableException("Error unmarshalling domain object from XML; domain object class is '" + domainClass.getName() + "'", ex);
        }
    }

    @Override
    public String toXml(final Object domainObject)  {

        final Class<?> domainClass = domainObject.getClass();
        try {
            final JAXBContext context = JAXBContext.newInstance(domainClass);

            final PersistentEntityAdapter adapter = new PersistentEntityAdapter();
            container.injectServicesInto(adapter);

            final Marshaller marshaller = context.createMarshaller();
            marshaller.setAdapter(PersistentEntityAdapter.class, adapter);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

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

    public Map<String,String> toXsd(final Object domainObject) {

        try {
            final Class<?> domainClass = domainObject.getClass();
            final JAXBContext context = JAXBContext.newInstance(domainClass);

            final CatalogingSchemaOutputResolver outputResolver = new CatalogingSchemaOutputResolver(includeIsisSchema);
            context.generateSchema(outputResolver);

            return outputResolver.asMap();
        } catch (final JAXBException | IOException ex) {
            throw new ApplicationException(ex);
        }
    }


    @Inject
    DomainObjectContainer container;
}


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
import java.util.Map;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.runtime.services.jaxb.util.CatalogingSchemaOutputResolver;
import org.apache.isis.schema.utils.jaxbadapters.PersistentEntityAdapter;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class JaxbServiceDefault implements JaxbService {

    @Override
    public <T> T fromXml(final Class<T> domainClass, final String memento) {
        try {
            final JAXBContext context = JAXBContext.newInstance(domainClass);

            final PersistentEntityAdapter adapter = new PersistentEntityAdapter();
            container.injectServicesInto(adapter);

            final Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setAdapter(PersistentEntityAdapter.class, adapter);

            final Object unmarshal = unmarshaller.unmarshal(new StringReader(memento));
            return (T) unmarshal;

        } catch (final JAXBException ex) {
            throw new ApplicationException(ex);
        }
    }

    @Override
    public String toXml(final Object domainObject)  {

        try {
            final Class<?> domainClass = domainObject.getClass();
            final JAXBContext context = JAXBContext.newInstance(domainClass);

            final PersistentEntityAdapter adapter = new PersistentEntityAdapter();
            container.injectServicesInto(adapter);

            final Marshaller marshaller = context.createMarshaller();
            marshaller.setAdapter(PersistentEntityAdapter.class, adapter);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            final StringWriter sw = new StringWriter();
            marshaller.marshal(domainObject, sw);
            return sw.toString();

        } catch (final JAXBException ex) {
            throw new ApplicationException(ex);
        }
    }

    public Map<String,String> toXsd(final Object domainObject) {

        try {
            final Class<?> domainClass = domainObject.getClass();
            final JAXBContext context = JAXBContext.newInstance(domainClass);

            final CatalogingSchemaOutputResolver outputResolver = new CatalogingSchemaOutputResolver();
            context.generateSchema(outputResolver);

            return outputResolver.asMap();
        } catch (final JAXBException | IOException ex) {
            throw new ApplicationException(ex);
        }
    }


    @Inject
    DomainObjectContainer container;
}


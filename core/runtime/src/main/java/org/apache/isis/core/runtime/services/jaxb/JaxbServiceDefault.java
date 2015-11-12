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
import java.io.StringWriter;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.jaxb.Dto;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.runtime.services.jaxb.util.CatalogingSchemaOutputResolver;
import org.apache.isis.core.runtime.services.jaxb.util.PersistentEntityAdapter;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class JaxbServiceDefault implements JaxbService {

    public Map<String,String> toXsd(final Dto dto) {

        try {
            final Class<? extends Dto> dtoClass = dto.getClass();
            final JAXBContext context = JAXBContext.newInstance(dtoClass);

            final CatalogingSchemaOutputResolver outputResolver = new CatalogingSchemaOutputResolver();
            context.generateSchema(outputResolver);

            return outputResolver.asMap();
        } catch (final JAXBException | IOException ex) {
            throw new ApplicationException(ex);
        }
    }

    public String toXml(final Dto dto)  {

        try {
            final Class<? extends Dto> dtoClass = dto.getClass();
            final JAXBContext context = JAXBContext.newInstance(dtoClass);

            final PersistentEntityAdapter adapter = new PersistentEntityAdapter();
            container.injectServicesInto(adapter);

            final Marshaller marshaller = context.createMarshaller();
            marshaller.setAdapter(PersistentEntityAdapter.class, adapter);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            final StringWriter sw = new StringWriter();
            marshaller.marshal(dto, sw);
            return sw.toString();

        } catch (final JAXBException ex) {
            throw new ApplicationException(ex);
        }

    }

    @Inject
    DomainObjectContainer container;
}


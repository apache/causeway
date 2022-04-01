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
package org.apache.isis.schema.services.jaxb;

import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.domain.DomainObjectList;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.metamodel.MetaModelService5;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.schema.utils.jaxbadapters.PersistentEntitiesAdapter;
import org.apache.isis.schema.utils.jaxbadapters.PersistentEntityAdapter;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
)
public class JaxbServiceDefault extends JaxbService.Simple {

    @Override
    public Object fromXml(final JAXBContext jaxbContext, final String xml, final Map<String, Object> unmarshallerProperties) {
        try {
            Object pojo = internalFromXml(jaxbContext, xml, unmarshallerProperties);

            if(pojo instanceof DomainObjectList) {

                // go around the loop again, so can properly deserialize the contents
                DomainObjectList list = (DomainObjectList) pojo;
                JAXBContext jaxbContextForList = jaxbContextFor(list);

                return internalFromXml(jaxbContextForList, xml, unmarshallerProperties);
            }

            return pojo;

        } catch (final JAXBException ex) {
            throw new NonRecoverableException("Error unmarshalling XML", ex);
        }
    }

    @Override
    protected JAXBContext jaxbContextFor(final Object domainObject) {
        final Class<?> domainClass = domainObject.getClass();
        if(domainObject instanceof DomainObjectList) {
            DomainObjectList list = (DomainObjectList) domainObject;
            try {
                final String elementObjectType = list.getElementObjectType();
                final Class<?> elementType = metaModelService5.fromObjectType(elementObjectType);
                if (elementType.getAnnotation(XmlJavaTypeAdapter.class) == null) {
                    return JAXBContext.newInstance(domainClass, elementType);
                } else {
                    return JAXBContext.newInstance(domainClass);
                }
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        return super.jaxbContextFor(domainObject);
    }


    @Override
    protected void configure(final Unmarshaller unmarshaller) {
        unmarshaller.setAdapter(PersistentEntityAdapter.class,
                serviceRegistry.injectServicesInto(new PersistentEntityAdapter()));
        unmarshaller.setAdapter(PersistentEntitiesAdapter.class,
                serviceRegistry.injectServicesInto(new PersistentEntitiesAdapter()));

        org.apache.isis.applib.jaxb.PersistentEntityAdapter pe = new org.apache.isis.applib.jaxb.PersistentEntityAdapter();
        serviceRegistry.injectServicesInto(pe);
        unmarshaller.setAdapter(org.apache.isis.applib.jaxb.PersistentEntityAdapter.class, pe);

        org.apache.isis.applib.jaxb.PersistentEntitiesAdapter pes = new org.apache.isis.applib.jaxb.PersistentEntitiesAdapter();
        serviceRegistry.injectServicesInto(pes);
        unmarshaller.setAdapter(org.apache.isis.applib.jaxb.PersistentEntitiesAdapter.class, pes);

    }

    @Override
    protected void configure(final Marshaller marshaller) {
        marshaller.setAdapter(PersistentEntityAdapter.class,
                serviceRegistry.injectServicesInto(new PersistentEntityAdapter()));
        marshaller.setAdapter(PersistentEntitiesAdapter.class,
                serviceRegistry.injectServicesInto(new PersistentEntitiesAdapter()));

        org.apache.isis.applib.jaxb.PersistentEntityAdapter pe = new org.apache.isis.applib.jaxb.PersistentEntityAdapter();
        serviceRegistry.injectServicesInto(pe);
        marshaller.setAdapter(org.apache.isis.applib.jaxb.PersistentEntityAdapter.class, pe);

        org.apache.isis.applib.jaxb.PersistentEntitiesAdapter pes = new org.apache.isis.applib.jaxb.PersistentEntitiesAdapter();
        serviceRegistry.injectServicesInto(pes);
        marshaller.setAdapter(org.apache.isis.applib.jaxb.PersistentEntitiesAdapter.class, pes);

    }


    @javax.inject.Inject
    ServiceRegistry serviceRegistry;

    @javax.inject.Inject
    MetaModelService5 metaModelService5;
}


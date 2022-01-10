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
package org.apache.isis.core.runtimeservices.jaxb;

import java.util.Map;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.domain.DomainObjectList;
import org.apache.isis.applib.jaxb.PersistentEntitiesAdapter;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.jaxb.JaxbService.Simple;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.resources._Xml;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@Service
@Named("isis.runtimeservices.JaxbServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class JaxbServiceDefault extends Simple {

    private final ServiceInjector serviceInjector;
    private final SpecificationLoader specLoader;

    @Override @SneakyThrows
    protected JAXBContext jaxbContextForObject(final @NonNull Object domainObject) {
        if(domainObject instanceof DomainObjectList) {
            val domainClass = domainObject.getClass();
            val domainObjectList = (DomainObjectList) domainObject;
            try {
                val elementType = specLoader
                        .specForType(_Context.loadClass(domainObjectList.getElementTypeFqcn()))
                        .map(ObjectSpecification::getCorrespondingClass)
                        .orElse(null);
                if (elementType!=null
                        && elementType.getAnnotation(XmlJavaTypeAdapter.class) == null) {

                    return JAXBContext.newInstance(domainClass, elementType);
                } else {
                    return JAXBContext.newInstance(domainClass);
                }
            } catch (Exception e) {
                throw _Xml.verboseException("obtaining JAXBContext for a DomainObjectList", domainClass, e);
            }
        }
        return super.jaxbContextForObject(domainObject);
    }

    @Override
    protected Object internalFromXml(
            final @NonNull JAXBContext jaxbContext,
            final String xml,
            final Map<String, Object> unmarshallerProperties) throws JAXBException {

        val pojo = super.internalFromXml(jaxbContext, xml, unmarshallerProperties);
        if(pojo instanceof DomainObjectList) {

            // go around the loop again, so can properly deserialize the contents
            val domainObjectList = (DomainObjectList) pojo;
            val jaxbContextForList = jaxbContextForObject(domainObjectList);
            return super.internalFromXml(jaxbContextForList, xml, unmarshallerProperties);
        }
        return pojo;
    }

    @Override
    protected void configure(final Unmarshaller unmarshaller) {
        unmarshaller.setAdapter(PersistentEntityAdapter.class,
                serviceInjector.injectServicesInto(new PersistentEntityAdapter()));
        unmarshaller.setAdapter(PersistentEntitiesAdapter.class,
                serviceInjector.injectServicesInto(new PersistentEntitiesAdapter()));
    }

    @Override
    protected void configure(final Marshaller marshaller) {

//debug
//        marshaller.setListener(new Marshaller.Listener() {
//            @Override
//            public void beforeMarshal(final Object source) {
//                System.err.printf("beforeMarshal %s%n", source);
//            }
//        });

        marshaller.setAdapter(PersistentEntityAdapter.class,
                serviceInjector.injectServicesInto(new PersistentEntityAdapter()));
        marshaller.setAdapter(PersistentEntitiesAdapter.class,
                serviceInjector.injectServicesInto(new PersistentEntitiesAdapter()));
    }


}


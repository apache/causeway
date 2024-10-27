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
package org.apache.causeway.core.runtimeservices.jaxb;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.services.jaxb.JaxbService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.domain.DomainObjectList;
import org.apache.causeway.applib.jaxb.PersistentEntitiesAdapter;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.jaxb.JaxbService.Simple;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Default implementation of {@link JaxbService}.
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".JaxbServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class JaxbServiceDefault extends Simple {

    private final ServiceInjector serviceInjector;
    private final SpecificationLoader specLoader;

    @SneakyThrows
    @Override
    protected JAXBContext jaxbContextForList(@NonNull final DomainObjectList domainObjectList) {
        var elementType = specLoader
                .specForType(_Context.loadClass(domainObjectList.getElementTypeFqcn()))
                .map(ObjectSpecification::getCorrespondingClass)
                .orElse(null);
        if (elementType!=null
                && elementType.getAnnotation(XmlJavaTypeAdapter.class) == null) {
            return JaxbUtils.jaxbContextFor(DomainObjectList.class, elementType);
        } else {
            return JaxbUtils.jaxbContextFor(DomainObjectList.class, true);
        }
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

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

import java.util.Map;

import jakarta.annotation.Priority;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.domain.DomainObjectList;
import org.apache.causeway.applib.jaxb.PersistentEntitiesAdapter;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.jaxb.CausewaySchemas;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

/**
 * Default implementation of {@link JaxbService}.
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".JaxbServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public record JaxbServiceDefault(
    JaxbService delegate) implements JaxbService {

    @Autowired
    public JaxbServiceDefault(
            ServiceInjector serviceInjector,
            Provider<SpecificationLoader> specLoaderProvider) {
        this(new JaxbService.JaxbServiceInternal(new JaxbService.JaxbServiceInternal.Config(
            marshaller->{
                marshaller.setAdapter(PersistentEntityAdapter.class,
                    serviceInjector.injectServicesInto(new PersistentEntityAdapter()));
                marshaller.setAdapter(PersistentEntitiesAdapter.class,
                        serviceInjector.injectServicesInto(new PersistentEntitiesAdapter()));
            },
            unmarshaller->{
                unmarshaller.setAdapter(PersistentEntityAdapter.class,
                    serviceInjector.injectServicesInto(new PersistentEntityAdapter()));
                unmarshaller.setAdapter(PersistentEntitiesAdapter.class,
                        serviceInjector.injectServicesInto(new PersistentEntitiesAdapter()));
            },
            domainObjectList->{
                var elementCls = Try.call(()->_Context.loadClass(domainObjectList.getElementTypeFqcn()))
                    .getValue() // silently ignore class loading issues
                    .orElse(null);
                var elementType = specLoaderProvider.get()
                    .specForType(elementCls)
                    .map(ObjectSpecification::getCorrespondingClass)
                    .orElse(null);
                if (elementType!=null
                        && elementType.getAnnotation(XmlJavaTypeAdapter.class) == null) {
                    return JaxbUtils.jaxbContextFor(DomainObjectList.class, elementType);
                } else {
                    return JaxbUtils.jaxbContextFor(DomainObjectList.class, true);
                }
            })));
    }

    @Override
    public <T> T fromXml(Class<T> domainClass, String xml, @Nullable Map<String, Object> unmarshallerProperties) {
        return delegate.fromXml(domainClass, xml, unmarshallerProperties);
    }

    @Override
    public String toXml(Object domainObject, @Nullable Map<String, Object> marshallerProperties) {
        return delegate.toXml(domainObject, marshallerProperties);
    }

    @Override
    public Map<String, String> toXsd(Object domainObject, CausewaySchemas causewaySchemas) {
        return delegate.toXsd(domainObject, causewaySchemas);
    }

}

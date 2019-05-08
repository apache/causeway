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

package org.apache.isis.core.metamodel.services.registry;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.commons.ioc.BeanAdapter;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

/**
 * @since 2.0.0-M3
 */
final class ServiceRegistryDefault_validate  {
    
    static void validateUniqueDomainServiceId(final Stream<BeanAdapter> domainServiceAdapters) {

        final ListMultimap<String, BeanAdapter> servicesById = _Multimaps.newListMultimap();
        
        domainServiceAdapters.forEach(serviceBeanAdapter->{
            String id = serviceBeanAdapter.getId();
            servicesById.putElement(id, serviceBeanAdapter);
        });
        

        final String errorMsg = servicesById.entrySet().stream()
                .filter(entry->entry.getValue().size()>1) // filter for duplicates
                .map(entry->{
                    String serviceId = entry.getKey();
                    List<BeanAdapter> duplicateServiceEntries = entry.getValue();
                    return String.format("serviceId '%s' is declared by domain services %s",
                            serviceId, classNamesFor(duplicateServiceEntries));
                })
                .collect(Collectors.joining(", "));

        if(_Strings.isNotEmpty(errorMsg)) {
            throw new IllegalStateException("Service ids must be unique! "+errorMsg);
        }
    }
    
    // -- HELPER - VALIDATE
    
    private static String classNamesFor(Collection<BeanAdapter> serviceBeans) {
        return stream(serviceBeans)
                .map(BeanAdapter::getBeanClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }

    
}

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

package org.apache.isis.core.runtime.services;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Sets;

final class ServicesInstallerUtils  {

    private static final Logger LOG = LoggerFactory.getLogger(ServicesInstallerFromConfiguration.class);

    private static final char DELIMITER = '#';

    private ServicesInstallerUtils (){}

    static <V extends Comparable<V>> LinkedHashSet<V> flatten(SortedMap<String, SortedSet<V>> positionedServices) {
        final LinkedHashSet<V> serviceList = _Sets.newLinkedHashSet();
        final Set<String> keys = positionedServices.keySet();
        for (String position : keys) {
            final SortedSet<V> list = positionedServices.get(position);
            serviceList.addAll(list);
        }
        return serviceList;
    }

    static <V> void appendInPosition(SortedMap<String, SortedSet<String>> positionedServices, String position, String service) {
        if(service == null) {
            return;
        }
        SortedSet<String> serviceList = positionedServices.get(position);
        if(serviceList == null) {
            serviceList = _Sets.newTreeSet();
            positionedServices.put(position, serviceList);
        }
        serviceList.add(service);
    }

    static Object instantiateService(String serviceName, ServiceInstantiator serviceInstantiator) {
        final int pos = serviceName.indexOf(DELIMITER);
        if( pos == 0) {
            // a commented out line, in other words...
            return null;
        }

        final String type;
        if (pos != -1) {
            type = serviceName.substring(0, pos);
            // disregard, assume the stuff after the delimiter (#) was a comment
        } else {
            type = serviceName;
        }

        return serviceInstantiator.createInstance(type);
    }

    static List<Object> instantiateServicesFrom(SortedMap<String, SortedSet<String>> positionedServices, final ServiceInstantiator serviceInstantiator) {
        final LinkedHashSet<String> serviceNameList = flatten(positionedServices);

        return _NullSafe.stream(serviceNameList)
        .map(instantiator(serviceInstantiator))
        .filter(_NullSafe::isPresent)
        .collect(Collectors.toList());
    }

    private static Function<String, Object> instantiator(final ServiceInstantiator serviceInstantiator) {
        return new Function<String, Object>() {
            @Override
            public Object apply(String serviceName) {
                return instantiateService(serviceName, serviceInstantiator);
            }
        };
    }

}

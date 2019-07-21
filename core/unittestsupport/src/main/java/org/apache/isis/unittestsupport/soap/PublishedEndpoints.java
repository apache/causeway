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
package org.apache.isis.unittestsupport.soap;

import java.util.List;
import java.util.Map;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Maps;

/**
 * Collection of SOAP endpoints that have been published; will automatically assign a unique address to any
 * that have not been {@link SoapEndpoint}s whose {@link SoapEndpointSpec} does not specify an {@link SoapEndpointSpec#getEndpointAddress() address}.
 */
class PublishedEndpoints {

    private int port = SoapEndpointPublishingRule.INITIAL_PORT;
    private Map<Class<?>, SoapEndpoint> soapEndpointByType = _Maps.newLinkedHashMap();

    void publishEndpointIfRequired(final List<SoapEndpointSpec> soapEndpointSpecs) {
        // merge in any new endpoints to static cache
        for (SoapEndpointSpec soapEndpointSpec : soapEndpointSpecs) {
            final Class<?> endpointClass = soapEndpointSpec.getEndpointClass();
            SoapEndpoint soapEndpoint = this.soapEndpointByType.get(endpointClass);
            if (soapEndpoint == null) {
                // instantiate and publish,automatically assigning an address to any that don't specify one
                soapEndpoint = new SoapEndpoint(soapEndpointSpec);
                soapEndpointByType.put(endpointClass, soapEndpoint);
                port = soapEndpoint.publish(port) + 1;
            }
        }
    }

    String getEndpointAddress(Class<?> endpointClass) {
        return soapEndpointByType.get(endpointClass).getSpec().getEndpointAddress();
    }

    <T> T getEndpointImplementor(Class<T> endpointClass) {
        return _Casts.uncheckedCast( soapEndpointByType.get(endpointClass).getImplementor() );
    }
}

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
package org.apache.isis.testing.unittestsupport.applib.core.soap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Maps;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import lombok.val;

/**
 * Collection of SOAP endpoints that have been published; will automatically assign a unique address to any
 * that have not been {@link SoapEndpoint}s whose {@link SoapEndpointSpec} does not specify an {@link SoapEndpointSpec#getEndpointAddress() address}.
 *
 * <p>
 *     Intended to be used as a singleton, reused across multiple tests.  If necessary the singleton can be
 *     {@link #dispose() dispose}d of.
 * </p>
 */
public class SoapPublishedEndpoints {

    /**
     * For any endpoints where the address is not specified, ports are assigned starting from this.
     */
    public static final int INITIAL_PORT_DEFAULT = 54345;

    /**
     * Lazily instantiates the singleton, using the {@link #INITIAL_PORT_DEFAULT default initial port}.
     */
    public static SoapPublishedEndpoints instance() {
        if(instance == null) {
            return new SoapPublishedEndpoints();
        }
        return instance;
    }

    /**
     * Lazily instantiates the singleton, on specified port.
     *
     * <p>
     *     If called again with a different port, then will {@link #dispose() discard} the singleton and start over.
     * </p>
     */
    public static SoapPublishedEndpoints instance(final int initialPort) {
        if (instance != null) {
            if (instance.port != initialPort) {
                dispose();
            }
        }
        if (instance == null) {
            instance = new SoapPublishedEndpoints(initialPort);
        }
        return instance;
    }

    /**
     * Dispose of the singleton.
     */
    public static void dispose() {
        instance = null;
    }

    private static SoapPublishedEndpoints instance;

    SoapPublishedEndpoints(){
        this(INITIAL_PORT_DEFAULT);
    }
    SoapPublishedEndpoints(int initialPort){
        this.initialPort = initialPort;
        this.port = this.initialPort;
    }
    private final int initialPort;
    private int port;
    private final Map<Class<?>, SoapEndpoint> soapEndpointByType = _Maps.newLinkedHashMap();


    public SoapPublishedEndpoints publishIfRequired(final Class<?> endpointClass, final String endpointAddress) {
        return publishIfRequired(new SoapEndpointSpec(endpointClass, endpointAddress));
    }

    public SoapPublishedEndpoints publishIfRequired(Class<?>... endpointClasses) {
        val soapEndpointSpecs = stream(endpointClasses)
                .map(SoapEndpointSpec::asSoapEndpointSpec)
                .collect(Collectors.toCollection(ArrayList::new));
        return publishIfRequired(soapEndpointSpecs);
    }

    public SoapPublishedEndpoints publishIfRequired(final List<Class<?>> endpointClasses) {
        val soapEndpointSpecs = stream(endpointClasses)
                .map(SoapEndpointSpec::asSoapEndpointSpec)
                .collect(Collectors.toCollection(ArrayList::new));
        return publishIfRequired(soapEndpointSpecs);
    }

    public SoapPublishedEndpoints publishIfRequired(SoapEndpointSpec... soapEndpointSpecs) {
        val soapEndpointSpecs2 = stream(soapEndpointSpecs)
                .collect(Collectors.toCollection(ArrayList::new));
        return instance.publishIfRequired(soapEndpointSpecs2);
    }

    public SoapPublishedEndpoints publishIfRequired(final Iterable<SoapEndpointSpec> soapEndpointSpecs) {
        val soapEndpointSpecs2 = stream(soapEndpointSpecs)
                .collect(Collectors.toCollection(ArrayList::new));
        return instance.publishIfRequired(soapEndpointSpecs2);
    }

    public SoapPublishedEndpoints publishEndpointIfRequired(final List<SoapEndpointSpec> soapEndpointSpecs) {
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
        return this;
    }

    public String getEndpointAddress(Class<?> endpointClass) {
        return soapEndpointByType.get(endpointClass).getSpec().getEndpointAddress();
    }

    public <T> T getEndpointImplementor(Class<T> endpointClass) {
        return _Casts.uncheckedCast( soapEndpointByType.get(endpointClass).getImplementor() );
    }
}

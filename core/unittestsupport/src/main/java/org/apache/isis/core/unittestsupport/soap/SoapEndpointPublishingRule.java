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
package org.apache.isis.core.unittestsupport.soap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.xml.ws.Endpoint;

import com.google.common.collect.Maps;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


/**
 * For example usage, see <a href="https://github.com/isisaddons/isis-module-publishmq">Isis addons' publishmq module</a> (non-ASF)
 */
public class SoapEndpointPublishingRule implements TestRule {

    private static Map<Class<?>,SoapEndpoint> cachedSoapEndpointByType = Maps.newLinkedHashMap();

    public static class SoapEndpointSpec {
        private final Class<?> endpointClass;
        private final String endpointAddress;
        private final Supplier<?> endpointImplementorFactory;

        public SoapEndpointSpec(final Class<?> endpointClass, final String endpointAddress) {
            this(endpointClass, endpointAddress, new SupplierUsingDefaultConstructor(endpointClass));
        }
        public SoapEndpointSpec(final Class<?> endpointClass, final String endpointAddress, final Supplier<?> endpointImplementorFactory) {
            this.endpointClass = endpointClass;
            this.endpointAddress = endpointAddress;
            this.endpointImplementorFactory = endpointImplementorFactory;
        }

    }

    private static class SoapEndpoint {
        private final SoapEndpointSpec spec;
        /**
         * lazily populated first time
         */
        private Object implementor;
        public SoapEndpoint(final SoapEndpointSpec spec) {
            this.spec = spec;
        }
    }

    private final Map<Class<?>,SoapEndpoint> soapEndpointByType = Maps.newLinkedHashMap();

    public SoapEndpointPublishingRule(final Class<?> endpointClass, final String endpointAddress) {
        this(new SoapEndpointSpec(endpointClass, endpointAddress, new SupplierUsingDefaultConstructor(endpointClass)));
    }

    public SoapEndpointPublishingRule(SoapEndpointSpec... soapEndpointSpecs) {
        this(Arrays.asList(soapEndpointSpecs));
    }

    public SoapEndpointPublishingRule(final List<SoapEndpointSpec> soapEndpointSpecs) {
        for (SoapEndpointSpec soapEndpointSpec : soapEndpointSpecs) {
            soapEndpointByType.put(soapEndpointSpec.endpointClass, new SoapEndpoint(soapEndpointSpec));
        }
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        publishEndpointIfRequired();
        return base;
    }

    private void publishEndpointIfRequired() {
        // merge in any new endpoints to static cache
        for (Class<?> type : soapEndpointByType.keySet()) {
            final SoapEndpoint soapEndpoint = soapEndpointByType.get(type);
            final Class<?> endpointClass = soapEndpoint.spec.endpointClass;
            final SoapEndpoint cachedSoapEndpoint = cachedSoapEndpointByType.get(endpointClass);
            if(cachedSoapEndpoint == null) {
                cachedSoapEndpointByType.put(endpointClass, soapEndpoint);
            }
        }
        // ensure all endpoints (including any new ones) are instantiated and published
        for (Class<?> type : cachedSoapEndpointByType.keySet()) {
            final SoapEndpoint cachedSoapEndpoint = cachedSoapEndpointByType.get(type);
            if(cachedSoapEndpoint.implementor == null) {
                cachedSoapEndpoint.implementor = cachedSoapEndpoint.spec.endpointImplementorFactory.get();
                Endpoint.publish(cachedSoapEndpoint.spec.endpointAddress, cachedSoapEndpoint.implementor);
            }
        }
    }

    public String getEndpointAddress(Class<?> endpointClass) {
        return cachedSoapEndpointByType.get(endpointClass).spec.endpointAddress;
    }

    public <T> T getEndpointImplementor(Class<T> endpointClass) {
        return (T)cachedSoapEndpointByType.get(endpointClass).implementor;
    }


    private static class SupplierUsingDefaultConstructor implements Supplier<Object> {
        private final Class<?> endpointClass;

        public SupplierUsingDefaultConstructor(final Class<?> endpointClass) {
            this.endpointClass = endpointClass;
        }

        @Override
        public Object get() {
            try {
                return endpointClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

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

import java.util.function.Supplier;

import javax.xml.ws.Endpoint;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


/**
 * For example usage, see <a href="https://github.com/isisaddons/isis-module-publishmq">Isis addons' publishmq module</a> (non-ASF)
 */
public class SoapEndpointPublishingRule<T> implements TestRule {

    private static ThreadLocal<Object> ENDPOINT = new ThreadLocal<>();

    private final String endpointAddress;
    private final Supplier<T> endpointImplementorFactory;

    public SoapEndpointPublishingRule(final String endpointAddress, final Supplier<T> endpointImplementorFactory) {
        this.endpointAddress = endpointAddress;
        this.endpointImplementorFactory = endpointImplementorFactory;
    }

    public String getEndpointAddress() {
        return endpointAddress;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        publishEndpointIfRequired();
        return base;
    }

    private void publishEndpointIfRequired() {
        if (ENDPOINT.get() == null) {
            final T implementor = endpointImplementorFactory.get();
            Endpoint.publish(endpointAddress, implementor);
            ENDPOINT.set(implementor);
        }
    }
    //endregion

    public T getPublishedEndpoint() {
        return (T) ENDPOINT.get();
    }


}

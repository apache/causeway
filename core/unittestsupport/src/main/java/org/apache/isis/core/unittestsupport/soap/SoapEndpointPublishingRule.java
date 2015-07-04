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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


/**
 * For example usage, see <a href="https://github.com/isisaddons/isis-module-publishmq">Isis addons' publishmq module</a> (non-ASF)
 */
public class SoapEndpointPublishingRule implements TestRule {

    /**
     * For any endpoints where the address is not specified, ports are assigned starting from this.
     */
    static final int INITIAL_PORT = 54345;

    private static PublishedEndpoints publishedEndpoints = new PublishedEndpoints();

    private final List<SoapEndpointSpec> soapEndpointSpecs = Lists.newArrayList();

    public SoapEndpointPublishingRule(final Class<?> endpointClass, final String endpointAddress) {
        this(new SoapEndpointSpec(endpointClass, endpointAddress));
    }

    public SoapEndpointPublishingRule(Class<?>... endpointClasses) {
        this(Arrays.asList(endpointClasses));
    }

    public SoapEndpointPublishingRule(final List<Class<?>> endpointClasses) {
        this(Iterables.transform(endpointClasses, SoapEndpointSpec.asSoapEndpointSpec()));
    }

    public SoapEndpointPublishingRule(SoapEndpointSpec... soapEndpointSpecs) {
        this(Arrays.asList(soapEndpointSpecs));
    }

    public SoapEndpointPublishingRule(final Iterable<SoapEndpointSpec> soapEndpointSpecs) {
        Iterables.addAll(this.soapEndpointSpecs, soapEndpointSpecs);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        publishedEndpoints.publishEndpointIfRequired(soapEndpointSpecs);
        return base;
    }

    public String getEndpointAddress(Class<?> endpointClass) {
        return publishedEndpoints.getEndpointAddress(endpointClass);
    }

    public <T> T getEndpointImplementor(Class<T> endpointClass) {
        return publishedEndpoints.getEndpointImplementor(endpointClass);
    }


}

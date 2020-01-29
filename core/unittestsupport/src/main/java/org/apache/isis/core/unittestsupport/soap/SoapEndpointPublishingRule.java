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
package org.apache.isis.core.unittestsupport.soap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.apache.isis.core.commons.internal.base._NullSafe.stream;


/**
 * <p>
 *     Used by domain apps only.
 * </p>
 */
public class SoapEndpointPublishingRule implements TestRule {

    /**
     * For any endpoints where the address is not specified, ports are assigned starting from this.
     */
    static final int INITIAL_PORT = 54345;

    private static PublishedEndpoints publishedEndpoints = new PublishedEndpoints();

    private final List<SoapEndpointSpec> soapEndpointSpecs;

    public SoapEndpointPublishingRule(final Class<?> endpointClass, final String endpointAddress) {
        this(new SoapEndpointSpec(endpointClass, endpointAddress));
    }

    public SoapEndpointPublishingRule(Class<?>... endpointClasses) {
        this.soapEndpointSpecs = stream(endpointClasses)
                .map(SoapEndpointSpec::asSoapEndpointSpec)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public SoapEndpointPublishingRule(final List<Class<?>> endpointClasses) {
        this.soapEndpointSpecs = stream(endpointClasses)
                .map(SoapEndpointSpec::asSoapEndpointSpec)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public SoapEndpointPublishingRule(SoapEndpointSpec... soapEndpointSpecs) {
        this.soapEndpointSpecs = stream(soapEndpointSpecs)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public SoapEndpointPublishingRule(final Iterable<SoapEndpointSpec> soapEndpointSpecs) {
        this.soapEndpointSpecs = stream(soapEndpointSpecs)
                .collect(Collectors.toCollection(ArrayList::new));
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

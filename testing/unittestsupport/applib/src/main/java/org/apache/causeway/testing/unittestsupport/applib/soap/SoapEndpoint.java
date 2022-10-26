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
package org.apache.causeway.testing.unittestsupport.applib.soap;

import javax.xml.ws.Endpoint;

class SoapEndpoint {

    private final SoapEndpointSpec spec;
    SoapEndpointSpec getSpec() {
        return spec;
    }

    SoapEndpoint(final SoapEndpointSpec spec) {
        this.spec = spec;
    }

    private Object implementor;
    /**
     * populated when {@link #publish(int) publish}ed.
     */
    public Object getImplementor() {
        return implementor;
    }

    int publish(int port) {
        if (implementor == null) {
            this.implementor = getSpec().getEndpointImplementorFactory().get();

            String endpointAddress = getSpec().getEndpointAddress();
            if (endpointAddress == null) {
                endpointAddress = String.format("http://localhost:%d/%s", port, getSpec().getEndpointClass().getName().replace(".", "/"));
                getSpec().setEndpointAddress(endpointAddress);
            }
            Endpoint.publish(endpointAddress, getImplementor());
        }
        return port;
    }


}

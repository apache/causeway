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
package org.apache.causeway.viewer.wicket.viewer.integration;

import java.util.Locale;

import org.apache.wicket.request.Request;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticatedWebSessionForCauseway_Instantiation {

    private Request mockRequest = Mockito.mock(Request.class);

    @Test
    void canInstantiateIfProvideRequest() {
        Mockito
        // must provide explicit expectation, since Locale is final.
        .when(mockRequest.getLocale())
        .thenReturn(Locale.getDefault());

        new AuthenticatedWebSessionForCauseway(mockRequest);
    }

    @Test
    void requestMustBeProvided() {
        assertThrows(Exception.class, ()->{
            new AuthenticatedWebSessionForCauseway(null);
        });
    }

}

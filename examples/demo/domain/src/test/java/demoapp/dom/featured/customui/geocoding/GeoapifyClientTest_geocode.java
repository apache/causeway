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
package demoapp.dom.featured.customui.geocoding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import demoapp.dom.AppConfiguration;
import demoapp.dom.featured.customui.GeoapifyClient;

class GeoapifyClientTest_geocode {

    @Test
    void happy_case() {

        // given
        final AppConfiguration appConfiguration = new AppConfiguration();
        final GeoapifyClient client = new GeoapifyClient(appConfiguration);

        // when
        final GeoapifyClient.GeocodeResponse response =
                client.geocode("38 Upper Montagu Street, Westminster W1H 1LJ, United Kingdom");

        // then
        assertEquals(Double.valueOf(response.getLatitude()), 51.520, 1E-2);
        assertEquals(Double.valueOf(response.getLongitude()), -0.160, 1E-2);
    }
}

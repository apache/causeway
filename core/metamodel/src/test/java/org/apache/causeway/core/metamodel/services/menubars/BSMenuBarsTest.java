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
package org.apache.causeway.core.metamodel.services.menubars;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuBars;
import org.apache.causeway.applib.services.jaxb.CausewaySchemas;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.jaxb.JaxbService.Simple;
import org.apache.causeway.commons.internal.resources._Resources;

class BSMenuBarsTest {

    private JaxbService jaxbService;

    @BeforeEach
    void setUp() throws Exception {
        jaxbService = new Simple() {};
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void happy_case() throws Exception {

        final BSMenuBars menuBars = jaxbService.fromXml(BSMenuBars.class,
                _Resources.loadAsString(getClass(), "menubars.layout.xml", StandardCharsets.UTF_8));

        final Map<String, String> schemas = jaxbService.toXsd(menuBars, CausewaySchemas.INCLUDE);

        menuBars.visit(data->{
            System.err.printf("%s%n", data);
        });


        for (Map.Entry<String, String> entry : schemas.entrySet()) {
            println(entry.getKey() + ":");
            println(entry.getValue());
        }
    }

    private void println(final String string) {
        //for test debugging only
        //System.err.printf("%s%n", string);
    }

}

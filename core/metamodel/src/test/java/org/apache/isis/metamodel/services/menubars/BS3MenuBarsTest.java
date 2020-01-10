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
package org.apache.isis.metamodel.services.menubars;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBars;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.commons.internal.resources._Resources;

public class BS3MenuBarsTest {

    private JaxbService jaxbService;

    @Before
    public void setUp() throws Exception {
        jaxbService = new JaxbService.Simple() {};
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void happy_case() throws Exception {

        BS3MenuBars menuBars = jaxbService.fromXml(BS3MenuBars.class, 
                _Resources.loadAsString(getClass(), "menubars.layout.xml", StandardCharsets.UTF_8));

        Map<String, String> schemas = jaxbService.toXsd(menuBars, JaxbService.IsisSchemas.INCLUDE);
        for (Map.Entry<String, String> entry : schemas.entrySet()) {
            println(entry.getKey() + ":");
            println(entry.getValue());
        }
    }

    private void println(String string) {
        //for test debugging only
    }

}
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
package org.apache.isis.applib.layout.menubars.bootstrap3;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.layout.component.ServiceActionLayoutData;
import org.apache.isis.applib.services.jaxb.JaxbService;

public class BS3MenuBars_roundtrip_Test {

    private JaxbService jaxbService;

    @Before
    public void setUp() throws Exception {
        jaxbService = new JaxbService.Simple();
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void happyCase() throws Exception {

        // given
        BS3MenuBars menuBars = new BS3MenuBars();
        BS3Menu menu = new BS3Menu();
        menu.setNamed("Parties");

        BS3MenuSection organisationMenu = new BS3MenuSection();
        organisationMenu.getServiceActions().add(new ServiceActionLayoutData("parties.OrganisationMenu", "findByReference"));
        organisationMenu.getServiceActions().add(new ServiceActionLayoutData("parties.OrganisationMenu", "findByName"));
        organisationMenu.getServiceActions().add(new ServiceActionLayoutData("parties.OrganisationMenu", "create"));
        menu.getSections().add(organisationMenu);

        BS3MenuSection personMenu = new BS3MenuSection();
        personMenu.getServiceActions().add(new ServiceActionLayoutData("parties.PersonMenu", "findByUsername"));
        personMenu.getServiceActions().add(new ServiceActionLayoutData("parties.PersonMenu", "create"));
        menu.getSections().add(personMenu);

        menuBars.getPrimary().getMenus().add(menu);

        // when
        String xml = jaxbService.toXml(menuBars);

        // when
        BS3MenuBars menuBars2 =
                jaxbService.fromXml(BS3MenuBars.class, xml);

        // then
        String xml2 = jaxbService.toXml(menuBars2);

        assertThat(xml, is(equalTo(xml2)));

    }

}

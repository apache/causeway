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
package org.apache.isis.applib.layout.menus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.services.jaxb.JaxbService;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MenuBars_roundtrip_Test {

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
        MenuBars menuBars = new MenuBars();
        Menu menu = new Menu();
        menu.setNamed("Parties");

        MenuSection organisationMenu = new MenuSection("parties.OrganisationMenu:1");
        organisationMenu.getActions().add(new ActionLayoutData("findByReference"));
        organisationMenu.getActions().add(new ActionLayoutData("findByName"));
        organisationMenu.getActions().add(new ActionLayoutData("create"));
        menu.getSections().add(organisationMenu);

        MenuSection personMenu = new MenuSection("parties.PersonMenu:1");
        personMenu.getActions().add(new ActionLayoutData("findByUsername"));
        personMenu.getActions().add(new ActionLayoutData("create"));
        menu.getSections().add(personMenu);

        menuBars.getPrimary().getMenus().add(menu);

        // when
        String xml = jaxbService.toXml(menuBars);
        System.out.println(xml);

        // when
        MenuBars menuBars2 =
                jaxbService.fromXml(MenuBars.class, xml);

        // then
        String xml2 = jaxbService.toXml(menuBars2);

        assertThat(xml, is(equalTo(xml2)));

    }

}

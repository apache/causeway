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
package org.apache.isis.core.runtimeservices.menubars.bootstrap3;

import org.junit.jupiter.api.Test;

import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting.MetaModelContext_forTestingBuilder;
import org.apache.isis.core.runtimeservices.RuntimeServicesTestAbstract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;

class MenuBarsServiceBS3Test
extends RuntimeServicesTestAbstract {

    private MenuBarsServiceBS3 menuBarsService;

    @Override
    protected void onSetUp(MetaModelContext_forTestingBuilder mmcBuilder) {
        mmcBuilder.singleton(new Bar());
    }

    @Override
    protected void afterSetUp() {
        menuBarsService = (MenuBarsServiceBS3) getServiceRegistry().lookupServiceElseFail(MenuBarsService.class);

        // double check, we are all set
        getSpecificationLoader().loadSpecification(Bar.class);

        assertTrue(getServiceRegistry().streamRegisteredBeans()
                .anyMatch(bean->bean.getBeanClass().equals(Bar.class)));

        assertTrue(getMetaModelContext().streamServiceAdapters()
                .anyMatch(domainObject->domainObject.getSpecification().getCorrespondingClass().equals(Bar.class)));
    }

    @Test
    void test() {
        val menuBars = menuBarsService.menuBars();
        assertNotNull(menuBars);

        val menuCounter = _Refs.intRef(0);
        menuBars.visit(v->{
            menuCounter.inc();
        });

        assertEquals(1, menuCounter.getValue());

        //TODO[ISIS-2787] verify menu xml export (roundtrip?)
    }

}

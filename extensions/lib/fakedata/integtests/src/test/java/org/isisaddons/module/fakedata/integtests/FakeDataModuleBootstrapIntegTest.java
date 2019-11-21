/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
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
package org.isisaddons.module.fakedata.integtests;

import javax.inject.Inject;

import org.junit.Test;

import org.apache.isis.applib.Module;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.metamodel.MetaModelService4;

public class FakeDataModuleBootstrapIntegTest extends FakeDataModuleIntegTestAbstract {

    @Test
    public void serializes_module() throws Exception {

        final Module module = metaModelService4.getAppManifest2().getModule();

        final String s = jaxbService.toXml(module);
        System.out.println(s);
    }

    @Inject
    MetaModelService4 metaModelService4;

    @Inject
    JaxbService jaxbService;
}
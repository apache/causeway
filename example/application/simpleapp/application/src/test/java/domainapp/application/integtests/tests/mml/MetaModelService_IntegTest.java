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
package domainapp.application.integtests.tests.mml;

import javax.inject.Inject;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.approvaltests.writers.ApprovalTextWriter;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.metamodel.MetaModelService6;
import org.apache.isis.applib.services.metamodel.MetaModelService6.Flags;
import org.apache.isis.schema.metamodel.v1.MetamodelDto;

import domainapp.application.integtests.DomainAppIntegTestAbstract;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assume.assumeThat;

public class MetaModelService_IntegTest extends DomainAppIntegTestAbstract {

    @Inject
    MetaModelService6 metaModelService6;
    @Inject
    JaxbService jaxbService;

    @Before
    public void setUp() throws Exception {
        assumeThat(System.getProperty("skip.lockdown"), is(nullValue()));
    }

    @UseReporter(DiffReporter.class)
    @Test
    public void exports() throws Exception {

        // when
        MetamodelDto metamodelDto = metaModelService6.exportMetaModel(new Flags().ignoreNoop());

        // then
        String asXml = jaxbService.toXml(metamodelDto);
        Approvals.verify(new ApprovalTextWriter(asXml, "xml"));
    }

}
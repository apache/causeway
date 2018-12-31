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
package domainapp.application.integtests.mml;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.approvaltests.namer.StackTraceNamer;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.QuietReporter;
import org.approvaltests.reporters.UseReporter;
import org.approvaltests.writers.ApprovalTextWriter;
import org.junit.Test;

import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.metamodel.MetaModelService6;
import org.apache.isis.schema.metamodel.v1.DomainClassDto;
import org.apache.isis.schema.metamodel.v1.MetamodelDto;

import domainapp.application.integtests.DomainAppIntegTestAbstract;
import static org.approvaltests.Approvals.getReporter;
import static org.approvaltests.Approvals.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assume.assumeThat;

public class MetaModelService_IntegTest extends DomainAppIntegTestAbstract {

    @Inject
    MetaModelService6 metaModelService6;
    @Inject
    JaxbService jaxbService;


    //
    // learn...
    //
    // ... move the resultant files in "received" directory over to "approved".
    //
    @UseReporter(QuietReporter.class)
    @Test
    public void _1_learn() throws Exception {

        assumeThat(System.getProperty("lockdown.learn"), is(notNullValue()));

        // when
        MetamodelDto metamodelDto =
                metaModelService6.exportMetaModel(
                        new MetaModelService6.Config()
                                .withIgnoreNoop()
                                .withIgnoreAbstractClasses()
                                .withIgnoreBuiltInValueTypes()
                                .withIgnoreInterfaces()
                                .withPackagePrefix("domainapp")
                );

        // then
        final List<DomainClassDto> domainClassDto = metamodelDto.getDomainClassDto();
        for (final DomainClassDto domainClass : domainClassDto) {
            try {
                verifyClass(domainClass);
            } catch (Error e) {
                //ignore ... learning.
            }
        }
    }


    //
    // verify ...
    //
    // ... ie compare the current metamodel to that previously captured.
    //
    @UseReporter(DiffReporter.class)
    @Test
    public void _2_verify() throws Exception {

        assumeThat(System.getProperty("lockdown.verify"), is(notNullValue()));

        // when
        MetamodelDto metamodelDto =
                metaModelService6.exportMetaModel(
                        new MetaModelService6.Config()
                                .withIgnoreNoop()
                                .withIgnoreAbstractClasses()
                                .withIgnoreBuiltInValueTypes()
                                .withIgnoreInterfaces()
                                .withPackagePrefix("domainapp")
                );

        // then
        final List<DomainClassDto> domainClassDto = metamodelDto.getDomainClassDto();
        for (final DomainClassDto domainClass : domainClassDto) {
            verifyClass(domainClass);
        }
    }

    private void verifyClass(final DomainClassDto domainClass) {
        String asXml = jaxbService.toXml(domainClass);
        verify(new ApprovalTextWriter(asXml, "xml"){
            @Override public String writeReceivedFile(final String received) throws Exception {
                return super.writeReceivedFile(received);
            }

            @Override public String getReceivedFilename(final String base) {
                return toFilename("received", base);
            }

            @Override public String getApprovalFilename(final String base) {
                return toFilename("approved", base);
            }

            private String toFilename(final String prefix, final String base) {
                final File file = new File(base);
                final File parentFile = file.getParentFile();
                final String localName = file.getName();
                final File newDir = new File(parentFile, prefix);
                final File newFile = new File(newDir, localName + ".xml");
                return newFile.toString();
            }

        }, new StackTraceNamer() {
            @Override public String getApprovalName() {
                return domainClass.getId();
            }
        }, getReporter());
    }

}
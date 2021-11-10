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
package org.apache.isis.subdomains.xdocreport.applib.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.apache.isis.subdomains.xdocreport.applib.XDocReportService;
import org.apache.isis.subdomains.xdocreport.applib.XDocReportService.OutputType;
import org.apache.isis.subdomains.xdocreport.applib.service.example.models.Developer;
import org.apache.isis.subdomains.xdocreport.applib.service.example.models.Project;
import org.apache.isis.subdomains.xdocreport.applib.service.example.models.ProjectDevelopersModel;
import org.apache.isis.testing.unittestsupport.applib.jmocking.JUnitRuleMockery2;

import fr.opensagres.xdocreport.core.io.IOUtils;

public class XDocReportServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    XDocReportService service;

    @Before
    public void setUp() throws Exception {
        service = new XDocReportServiceDefault();
    }

    @Test
    public void simple() throws Exception {

        // given
        InputStream in= new FileInputStream(new File("src/test/java/org/apache/isis/subdomains/xdocreport/applib/service/example/template/Project-template.docx"));
        final byte[] templateBytes = IOUtils.toByteArray(in);

        Project project = new Project("XDocReport");
        List<Developer> developers = new ArrayList<>();
        developers.add(new Developer("ZERR", "Angelo", "angelo.zerr@gmail.com"));
        developers.add(new Developer("Leclercq", "Pascal", "pascal.leclercq@gmail.com"));
        final ProjectDevelopersModel dataModel = new ProjectDevelopersModel(project, developers);

        // when
        final byte[] docxBytes = service.render(templateBytes, dataModel, OutputType.DOCX);

        // then
        new File("target").mkdir(); // create the target folder if needed [gradle]
        IOUtils.write(docxBytes,new FileOutputStream(new File("target/Project.docx")));
    }

}

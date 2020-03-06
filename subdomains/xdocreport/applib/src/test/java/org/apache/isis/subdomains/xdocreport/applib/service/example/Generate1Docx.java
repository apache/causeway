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
package org.apache.isis.subdomains.xdocreport.applib.service.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.isis.subdomains.xdocreport.applib.service.example.models.Developer;
import org.apache.isis.subdomains.xdocreport.applib.service.example.models.Project;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

public class Generate1Docx {

    public static void main(String[] args) throws IOException,
            XDocReportException {

        // 1) Load Docx file by filling freemarker template engine and cache
        // it to the registry
        InputStream in= new FileInputStream(new File("src/test/java/org/apache/isis/extensions/xdocreport/dom/impl/example/template/Project-template.docx"));
        IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in,TemplateEngineKind.Freemarker);

        // 2) Create fields metadata to manage lazy loop (#forech velocity)
        // for table row.
        // 1) Create FieldsMetadata by setting freemarker as template engine
        FieldsMetadata fieldsMetadata = report.createFieldsMetadata();

        // 2) Load fields metadata from Java Class
        fieldsMetadata.load("project", Project.class);
        // Here load is called with true because model is a list of Developer.
        fieldsMetadata.load("developers", Developer.class, true);

        // 3) Create context Java model
        IContext context = report.createContext();

        Project project = new Project("XDocReport");
        context.put("project", project);

        List<Developer> developers = new ArrayList<Developer>();
        developers.add(
                new Developer("ZERR", "Angelo", "angelo.zerr@gmail.com"));
        developers.add(
                new Developer("Leclercq", "Pascal", "pascal.leclercq@gmail.com"));
        context.put("developers", developers);

        // 4) Generate report by merging Java model with the Docx
        OutputStream out = new FileOutputStream(new File("target/Project.docx"));
        report.process(context, out);

    }

}

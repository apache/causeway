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
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.isis.subdomains.xdocreport.applib.service.example.models.Developer;
import org.apache.isis.subdomains.xdocreport.applib.service.example.models.Project;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

public class Generate0Fields {

    public static void main(String[] args) throws XDocReportException,IOException {

        // 1) Create FieldsMetadata by setting Velocity as template engine
        FieldsMetadata fieldsMetadata = new FieldsMetadata(TemplateEngineKind.Freemarker.name());

        // 2) Load fields metadata from Java Class
        fieldsMetadata.load("project", Project.class);
        // Here load is called with true because model is a list of Developer.
        fieldsMetadata.load("developers", Developer.class, true);


        // 3) Generate XML fields in the file "project.fields.xml".
        // Extension *.fields.xml is very important to use it with MS Macro XDocReport.dotm
        // FieldsMetadata#saveXML is called with true to indent the XML.
        final File xmlFieldsFile = new File("target/project.fields.xml");
        fieldsMetadata.saveXML(new FileOutputStream(xmlFieldsFile), true);
    }

}

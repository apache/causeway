package org.apache.isis.extensions.xdocreport.dom.impl.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.isis.extensions.xdocreport.dom.impl.example.models.Developer;
import org.apache.isis.extensions.xdocreport.dom.impl.example.models.Project;

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

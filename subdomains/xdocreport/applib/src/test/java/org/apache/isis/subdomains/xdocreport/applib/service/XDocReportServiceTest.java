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

import org.apache.isis.subdomains.xdocreport.applib.service.example.models.Developer;
import org.apache.isis.subdomains.xdocreport.applib.service.example.models.Project;
import org.apache.isis.subdomains.xdocreport.applib.service.example.models.ProjectDevelopersModel;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import fr.opensagres.xdocreport.core.io.IOUtils;

public class XDocReportServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    XDocReportService service;

    @Before
    public void setUp() throws Exception {
        service = new XDocReportService();
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
        IOUtils.write(docxBytes,new FileOutputStream(new File("target/Project.docx")));
    }

}

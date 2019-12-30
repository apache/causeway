package org.apache.isis.extensions.xdocreport.dom.impl.service;

import fr.opensagres.xdocreport.core.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.isis.extensions.xdocreport.dom.impl.OutputType;
import org.apache.isis.extensions.xdocreport.dom.impl.XDocReportService;
import org.apache.isis.extensions.xdocreport.dom.impl.example.models.Developer;
import org.apache.isis.extensions.xdocreport.dom.impl.example.models.Project;
import org.apache.isis.extensions.xdocreport.dom.impl.example.models.ProjectDevelopersModel;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
        InputStream in= new FileInputStream(new File("src/test/java/org/apache/isis/extensions/xdocreport/dom/impl/example/template/Project-template.docx"));
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

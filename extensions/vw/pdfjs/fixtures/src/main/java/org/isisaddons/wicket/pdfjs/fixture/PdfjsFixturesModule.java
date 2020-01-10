package org.isisaddons.wicket.pdfjs.fixture;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.isis.applib.ModuleAbstract;
import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.isisaddons.wicket.pdfjs.fixture.demoapp.demomodule.fixturescripts.PdfJsDemoObjectWithBlob_tearDown;

@XmlRootElement(name = "module")
public class PdfjsFixturesModule extends ModuleAbstract {

    @Override public FixtureScript getTeardownFixture() {
        return new PdfJsDemoObjectWithBlob_tearDown();
    }

}

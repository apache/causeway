package org.isisaddons.wicket.pdfjs.fixture.demoapp.demomodule.fixturescripts;

import org.apache.isis.applib.fixturescripts.teardown.TeardownFixtureAbstract2;

import org.isisaddons.wicket.pdfjs.fixture.demoapp.demomodule.dom.PdfJsDemoObjectWithBlob;

public class PdfJsDemoObjectWithBlob_tearDown extends TeardownFixtureAbstract2 {

    @Override
    protected void execute(final ExecutionContext executionContext) {
        deleteFrom(PdfJsDemoObjectWithBlob.class);
    }

}

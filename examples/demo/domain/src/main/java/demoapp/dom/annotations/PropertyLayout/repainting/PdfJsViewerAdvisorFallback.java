package demoapp.dom.annotations.PropertyLayout.repainting;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.config.Scale;
import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.spi.PdfJsViewerAdvisor;

@Service
public class PdfJsViewerAdvisorFallback implements PdfJsViewerAdvisor {

    @Override
    public Advice advise(InstanceKey instanceKey) {
        return new Advice(1, new Advice.TypeAdvice(Scale._1_00, 400));
    }

    @Override
    public void pageNumChangedTo(InstanceKey instanceKey, int pageNum) {
    }

    @Override
    public void scaleChangedTo(InstanceKey instanceKey, Scale scale) {
    }

    @Override
    public void heightChangedTo(InstanceKey instanceKey, int height) {
    }
}

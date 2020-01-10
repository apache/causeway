package org.apache.isis.subdomains.xdocreport.applib.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.poi.xwpf.converter.core.IXWPFConverter;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.Programmatic;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.core.io.internal.ByteArrayOutputStream;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

@Service
public class XDocReportService {

    PdfOptions pdfOptions;

    @PostConstruct
    public void init() {
        pdfOptions = PdfOptions.create();
    }

    @Programmatic
    public byte[] render(
            final byte[] templateBytes,
            final XDocReportModel dataModel,
            final OutputType outputType) throws IOException {
        try {
            final byte[] docxBytes = toDocx(templateBytes, dataModel);

            switch (outputType) {
            case PDF:
                return toPdf(docxBytes);
            default: // ie DOCX
                return docxBytes;
            }

        } catch (XDocReportException e) {
            throw new IOException(e);
        }
    }

    private byte[] toDocx(final byte[] bytes, final XDocReportModel dataModel) throws IOException, XDocReportException {
        final IXDocReport report = XDocReportRegistry
                .getRegistry().loadReport(new ByteArrayInputStream(bytes), TemplateEngineKind.Freemarker);

        final IContext context = report.createContext();

        final FieldsMetadata fieldsMetadata = report.createFieldsMetadata();

        final Map<String, XDocReportModel.Data> contextObjects = dataModel.getContextData();
        for (final Map.Entry<String, XDocReportModel.Data> entry : contextObjects.entrySet()) {
            final XDocReportModel.Data data = entry.getValue();
            final String key = entry.getKey();
            fieldsMetadata.load(key, data.getCls(), data.isList());
            context.put(key, data.getObj());
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        report.process(context, baos);

        return baos.toByteArray();
    }

    private byte[] toPdf(final byte[] docxBytes) throws IOException {

        final XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(docxBytes));

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pdfConverter().convert(document, baos, pdfOptions);

        return baos.toByteArray();
    }

    protected IXWPFConverter<PdfOptions> pdfConverter() {
        return PdfConverter.getInstance();
    }


}




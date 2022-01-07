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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Named;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.subdomains.xdocreport.applib.XDocReportService;

import fr.opensagres.poi.xwpf.converter.core.IXWPFConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.core.io.internal.ByteArrayOutputStream;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

@Service
@Named("isis.sub.xdocreport.XDocReportServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class XDocReportServiceDefault implements XDocReportService {

    PdfOptions pdfOptions;

    @PostConstruct
    public void init() {
        pdfOptions = PdfOptions.create();
    }

    @Override
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


    protected IXWPFConverter<PdfOptions> pdfConverter() {
        return PdfConverter.getInstance();
    }

    // -- HELPER

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

}

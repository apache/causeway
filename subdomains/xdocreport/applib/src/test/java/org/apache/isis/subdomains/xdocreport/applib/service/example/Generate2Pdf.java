package org.apache.isis.subdomains.xdocreport.applib.service.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import fr.opensagres.xdocreport.core.XDocReportException;

public class Generate2Pdf {

    public static void main(String[] args) throws IOException,
            XDocReportException {

        // 1) Load DOCX into XWPFDocument
        InputStream in= new FileInputStream(new File("target/Project.docx"));
        XWPFDocument document = new XWPFDocument(in);

        // 2) Prepare Pdf options
        PdfOptions options = PdfOptions.create();

        // 3) Convert XWPFDocument to Pdf
        OutputStream out = new FileOutputStream(new File("target/Project.pdf"));
        PdfConverter.getInstance().convert(document, out, options);
    }

}

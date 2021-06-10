package org.apache.isis.subdomains.docx.applib.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.bind.JAXBException;

import org.docx4j.com.google.common.base.Objects;
import org.docx4j.convert.in.FlatOpcXmlImporter;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Tag;

import org.apache.isis.subdomains.docx.applib.exceptions.MergeException;

public final class Docx {
    private Docx() {
    }

    public static Function<SdtElement, String> tagToValue() {
        return  new Function<SdtElement, String>(){
            public String apply(SdtElement input) {
                return input.getSdtPr().getTag().getVal();
            }
        };
    }

    public static Predicate<Object> withAnyTag() {
        return new Predicate<Object>(){
            public boolean test(Object object) {
                if(!(object instanceof SdtElement)) {
                    return false;
                }
                SdtElement sdtBlock = (SdtElement) object;
                Tag tag = sdtBlock.getSdtPr().getTag();
                return tag != null;
            }
        };
    }

    public static Predicate<Object> withTagVal(final String tagVal) {
        return new Predicate<Object>(){
            public boolean test(Object object) {
                if(!(object instanceof SdtElement)) {
                    return false;
                }
                SdtElement sdtBlock = (SdtElement) object;
                Tag tag = sdtBlock.getSdtPr().getTag();
                return tag != null && Objects.equal(tagVal, tag.getVal());
            }

        };
    }

    @SuppressWarnings({ "rawtypes", "restriction" })
    public
    static boolean setText(R run, String value) {
        List<Object> runContent = run.getContent();
        if(runContent.isEmpty()) {
            return false;
        }
        Object jaxbElObj = runContent.get(0);

        if(!(jaxbElObj instanceof javax.xml.bind.JAXBElement)) {
            return false;
        }
        javax.xml.bind.JAXBElement jaxbElement = (javax.xml.bind.JAXBElement) jaxbElObj;
        Object textObj = jaxbElement.getValue();
        if(!(textObj instanceof org.docx4j.wml.Text)) {
            return false;
        }
        org.docx4j.wml.Text text = (org.docx4j.wml.Text) textObj;
        text.setValue(value);
        return true;
    }

    public static Body docxBodyFor(WordprocessingMLPackage docxPkg) {
        MainDocumentPart docxMdp = docxPkg.getMainDocumentPart();

        org.docx4j.wml.Document docxDoc = (org.docx4j.wml.Document) docxMdp.getJaxbElement();
        return docxDoc.getBody();
    }

    public static WordprocessingMLPackage clone(WordprocessingMLPackage docxTemplate) throws MergeException {
        FlatOpcXmlCreator foxc = new FlatOpcXmlCreator(docxTemplate);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            foxc.marshal(baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            FlatOpcXmlImporter foxi = new FlatOpcXmlImporter(bais);
            docxTemplate = (WordprocessingMLPackage) foxi.get();
        } catch (Docx4JException e) {
            throw new MergeException("unable to defensive copy (problem exporting)", e);
        } catch (@SuppressWarnings("restriction") JAXBException e) {
            throw new MergeException("unable to defensive copy (problem importing)", e);
        }
        return docxTemplate;
    }


}

package org.apache.isis.subdomains.docx.applib.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.function.Function;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;

import org.apache.isis.subdomains.docx.applib.exceptions.LoadInputException;
import org.apache.isis.subdomains.docx.applib.exceptions.MergeException;

public final class Jdom2 {

    private Jdom2(){}

    public static String textValueOf(Element htmlElement) {
        List<Content> htmlContent = htmlElement.getContent();
        if(htmlContent.isEmpty()) {
            return null;
        }
        Content content = htmlContent.get(0);
        if(!(content instanceof Text)) {
            return null;
        }
        Text htmlText = (Text) content;
        return normalized(htmlText.getValue());
    }


    private static String normalized(String value) {
        String replaceAll = value.replaceAll("\\s+", " ");
        return replaceAll;
    }

    public static Function<Element, String> textValue() {
        return  new Function<Element, String>(){
        public String apply(Element input) {
            return textValueOf(input);
        }};
    }

    public static String attrOf(Element input, String attname) {
        Attribute attribute = input.getAttribute(attname);
        if(attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    public static Document loadInput(String html) throws LoadInputException {
        try {
            return new SAXBuilder().build(new StringReader(html));
        } catch (JDOMException e) {
            throw new LoadInputException("Unable to parse input", e);
        } catch (IOException e) {
            throw new LoadInputException("Unable to parse input", e);
        }
    }

    public static Element htmlBodyFor(Document htmlDoc) throws MergeException {
        Element htmlEl = htmlDoc.getRootElement();
        Element bodyEl = htmlEl.getChild("body");
        if (bodyEl == null) {
            throw new MergeException("cannot locate body element within the input HTML");
        }
        return bodyEl;
    }

}

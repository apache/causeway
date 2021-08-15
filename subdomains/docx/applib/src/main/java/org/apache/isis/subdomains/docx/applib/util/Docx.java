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
import org.docx4j.wml.Body;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Tag;

import org.apache.isis.subdomains.docx.applib.exceptions.MergeException;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Docx {

    public static Function<SdtElement, String> tagToValue() {
        return input -> input.getSdtPr().getTag().getVal();
    }

    public static Predicate<Object> withAnyTag() {
        return object -> {
            if(!(object instanceof SdtElement)) {
                return false;
            }
            SdtElement sdtBlock = (SdtElement) object;
            Tag tag = sdtBlock.getSdtPr().getTag();
            return tag != null;
        };
    }

    public static Predicate<Object> withTagVal(final String tagVal) {
        return object -> {
            if(!(object instanceof SdtElement)) {
                return false;
            }
            val sdtBlock = (SdtElement) object;
            val tag = sdtBlock.getSdtPr().getTag();
            return tag != null && Objects.equal(tagVal, tag.getVal());
        };
    }

    @SuppressWarnings({ "rawtypes", "restriction" })
    public
    static boolean setText(R run, String value) {
        List<Object> runContent = run.getContent();
        if(runContent.isEmpty()) {
            return false;
        }
        val jaxbElObj = runContent.get(0);

        if(!(jaxbElObj instanceof javax.xml.bind.JAXBElement)) {
            return false;
        }
        val jaxbElement = (javax.xml.bind.JAXBElement) jaxbElObj;
        val textObj = jaxbElement.getValue();
        if(!(textObj instanceof org.docx4j.wml.Text)) {
            return false;
        }
        val text = (org.docx4j.wml.Text) textObj;
        text.setValue(value);
        return true;
    }

    public static Body docxBodyFor(WordprocessingMLPackage docxPkg) {
        val docxMdp = docxPkg.getMainDocumentPart();
        val docxDoc = docxMdp.getJaxbElement();
        return docxDoc.getBody();
    }

    public static WordprocessingMLPackage clone(WordprocessingMLPackage docxTemplate) throws MergeException {
        val foxc = new FlatOpcXmlCreator(docxTemplate);
        val baos = new ByteArrayOutputStream();
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

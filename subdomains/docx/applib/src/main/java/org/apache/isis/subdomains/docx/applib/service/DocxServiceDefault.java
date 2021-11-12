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
package org.apache.isis.subdomains.docx.applib.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.inject.Named;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.IOUtils;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.com.google.common.base.Objects;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.subdomains.docx.applib.DocxService;
import org.apache.isis.subdomains.docx.applib.exceptions.LoadInputException;
import org.apache.isis.subdomains.docx.applib.exceptions.LoadTemplateException;
import org.apache.isis.subdomains.docx.applib.exceptions.MergeException;
import org.apache.isis.subdomains.docx.applib.traverse.AllMatches;
import org.apache.isis.subdomains.docx.applib.traverse.FirstMatch;
import org.apache.isis.subdomains.docx.applib.util.Docx;
import org.apache.isis.subdomains.docx.applib.util.Jdom2;
import org.apache.isis.subdomains.docx.applib.util.Types;

import lombok.val;

@Service
@Named("isis.sub.docx.DocxServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class DocxServiceDefault implements DocxService {

    @Override
    public WordprocessingMLPackage loadPackage(final InputStream docxTemplate) throws LoadTemplateException {
        final WordprocessingMLPackage docxPkg;
        try {
            docxPkg = WordprocessingMLPackage.load(docxTemplate);
        } catch (final Docx4JException ex) {
            throw new LoadTemplateException("Unable to load docx template from input stream", ex);
        }
        return docxPkg;
    }

    @Override
    public void merge(final MergeParams mergeDefn) throws LoadInputException, LoadTemplateException, MergeException {

        final org.jdom2.Document htmlJdomDoc;
        final Document inputAsHtmlDoc = mergeDefn.getInputAsHtmlDoc();
        final String inputAsHtml = mergeDefn.getInputAsHtml();
        if(inputAsHtmlDoc != null) {
            htmlJdomDoc = new DOMBuilder().build(inputAsHtmlDoc);
        } else if (inputAsHtml != null) {
            htmlJdomDoc = Jdom2.loadInput(inputAsHtml);
        } else {
            throw new IllegalArgumentException("Input HTML must be provided");
        }

        final DefensiveCopy defensiveCopy;
        final WordprocessingMLPackage docxPkg;
        final WordprocessingMLPackage docxTemplateAsWpMlPackage = mergeDefn.getDocxTemplateAsWpMlPackage();
        final InputStream docxTemplate = mergeDefn.getDocxTemplate();
        if(docxTemplateAsWpMlPackage != null) {
            docxPkg = docxTemplateAsWpMlPackage;
            defensiveCopy = DefensiveCopy.REQUIRED;
        } else if (docxTemplate != null) {
            docxPkg = loadPackage(docxTemplate);
            defensiveCopy = DefensiveCopy.NOT_REQUIRED;
        } else {
            throw new IllegalArgumentException("Docx template HTML must be provided");
        }

        val output = mergeDefn.getOutput();
        if(output == null) {
            throw new IllegalArgumentException("Output stream must be provided");
        }
        merge(htmlJdomDoc, docxPkg, output, mergeDefn.getMatchingPolicy(), defensiveCopy, mergeDefn.getOutputType());
    }

    // -- HELPER

    private void merge(
            final org.jdom2.Document htmlDoc,
            final WordprocessingMLPackage docxTemplateInput,
            final OutputStream docxTarget,
            final MatchingPolicy matchingPolicy,
            final DefensiveCopy defensiveCopy,
            final OutputType outputType)
            throws MergeException {

        final WordprocessingMLPackage docxTemplate =
                defensiveCopy == DefensiveCopy.REQUIRED
                        ? Docx.clone(docxTemplateInput)
                        : docxTemplateInput;

        try {
            final Element bodyEl = Jdom2.htmlBodyFor(htmlDoc);
            final Body docXBody = Docx.docxBodyFor(docxTemplate);

            merge(bodyEl, docXBody, matchingPolicy);

            if (outputType == OutputType.PDF) {


                final FOSettings foSettings = Docx4J.createFOSettings();
                foSettings.setOpcPackage(docxTemplate);

                try {
                    final Mapper fontMapper = new IdentityPlusMapper();
                    docxTemplate.setFontMapper(fontMapper, true);
                } catch (final Exception e) {
                    throw new MergeException("unable to set font mapper for PDF generation", e);
                }

                // according to the documentation/examples the XSL transformation
                // is slower but more feature complete than Docx4J.FLAG_EXPORT_PREFER_NONXSL

                final int flags = Docx4J.FLAG_EXPORT_PREFER_XSL;

                Docx4J.toFO(foSettings, docxTarget, flags);

            } else {
                final File tempTargetFile = createTempFile();
                FileInputStream tempTargetFis = null;
                try {
                    docxTemplate.save(tempTargetFile);
                    tempTargetFis = new FileInputStream(tempTargetFile);
                    IOUtils.copy(tempTargetFis, docxTarget);
                } finally {
                    IOUtils.closeQuietly(tempTargetFis);
                    tempTargetFile.delete();
                }
            }
        } catch (final Docx4JException e) {
            throw new MergeException("unable to write to target file", e);
        } catch (final FileNotFoundException e) {
            throw new MergeException("unable to read back from target file", e);
        } catch (final IOException e) {
            throw new MergeException("unable to generate output stream from temporary file", e);
        }
    }

    private enum DefensiveCopy {
        REQUIRED,
        NOT_REQUIRED
    }

    private enum MergeType {
        PLAIN("p.plain"),
        RICH("p.rich"),
        DATE("p.date"),
        UL("ul") {
            @Override
            boolean merge(final Element htmlUl, final SdtElement sdtElement) {
                final List<Element> htmlLiList = htmlUl.getChildren("li"); // can be empty

                final List<P> docxPOrigList = AllMatches.<P>matching(sdtElement, Types.withType(P.class));
                if (docxPOrigList.isEmpty()) {
                    return false;
                }

                final List<P> docxPNewList = new ArrayList<P>();
                for (final Element htmlLi : htmlLiList) {
                    final List<Element> htmlPList = htmlLi.getChildren("p");

                    for (int htmlPNum = 0; htmlPNum < htmlPList.size(); htmlPNum++) {
                        final int numDocxPNum = docxPOrigList.size();
                        final int docxPNum = numDocxPNum == 1 || htmlPNum == 0 ? 0 : 1;
                        final P docxP = XmlUtils.deepCopy(docxPOrigList.get(docxPNum));
                        docxPNewList.add(docxP);
                        final R docxR = FirstMatch.<R>matching(docxP, Types.withType(R.class));
                        final Element htmlP = htmlPList.get(htmlPNum);
                        Docx.setText(docxR, Jdom2.textValueOf(htmlP));
                    }
                }

                // remove original and replace with new
                final List<Object> content = sdtElement.getSdtContent().getContent();
                for (final P docxP : docxPOrigList) {
                    content.remove(docxP);
                }
                for (final P docxP : docxPNewList) {
                    content.add(docxP);
                }
                return true;
            }
        },
        TABLE("table") {
            @Override
            boolean merge(final Element htmlTable, final SdtElement sdtElement) {

                final List<Element> htmlTrOrigList = htmlTable.getChildren("tr"); // can be empty

                final List<Object> docxContents = sdtElement.getSdtContent().getContent();
                final Tbl docxTbl = FirstMatch.matching(docxContents, Types.withType(Tbl.class));
                if (docxTbl == null) {
                    return false;
                }
                final List<Tr> docxTrList = AllMatches.matching(docxTbl, Types.withType(Tr.class));
                if (docxTrList.size() < 2) {
                    // require a header row and one other
                    return false;
                }

                final List<Tr> docxTrNewList = Lists.newArrayList();
                for (int htmlRowNum = 0; htmlRowNum < htmlTrOrigList.size(); htmlRowNum++) {
                    final Element htmlTr = htmlTrOrigList.get(htmlRowNum);

                    final int numDocxBodyTr = docxTrList.size() - 1;
                    final int docxTrNum = (htmlRowNum % numDocxBodyTr) + 1;
                    final Tr docxTr = XmlUtils.deepCopy(docxTrList.get(docxTrNum));
                    docxTrNewList.add(docxTr);
                    final List<Tc> docxTcList = AllMatches.matching(docxTr.getContent(), Types.withType(Tc.class));
                    final List<Element> htmlTdList = htmlTr.getChildren("td");
                    final List<String> htmlCellValues =
                            htmlTdList.stream().map(x -> Jdom2.textValue().apply(x))
                            .collect(Collectors.toList());
                    for (int cellNum = 0; cellNum < docxTcList.size(); cellNum++) {
                        final Tc docxTc = docxTcList.get(cellNum);
                        final String value = cellNum < htmlCellValues.size() ? htmlCellValues.get(cellNum) : "";
                        final P docxP = FirstMatch.matching(docxTc.getContent(), Types.withType(P.class));
                        if (docxP == null) {
                            return false;
                        }
                        final R docxR = FirstMatch.matching(docxP, Types.withType(R.class));
                        if (docxR == null) {
                            return false;
                        }
                        Docx.setText(docxR, value);
                    }
                }
                docxReplaceRows(docxTbl, docxTrList, docxTrNewList);
                return true;
            }

            private void docxReplaceRows(final Tbl docxTbl, final List<Tr> docxTrList, final List<Tr> docxTrToAdd) {
                final List<Object> docxTblContent = docxTbl.getContent();
                boolean first = true;
                for (final Tr docxTr : docxTrList) {
                    if (first) {
                        // header, do NOT remove
                        first = false;
                    } else {
                        docxTblContent.remove(docxTr);
                    }
                }
                for (final Tr docxTr : docxTrToAdd) {
                    docxTblContent.add(docxTr);
                }
            }
        };

        private final String type;


        private MergeType(final String type) {
            this.type = type;
        }

        public static MergeType lookup(final String name, final String clazz) {
            final String type = name + (clazz != null ? "." + clazz : "");
            for (final MergeType mt : values()) {
                if (Objects.equal(mt.type, type)) {
                    return mt;
                }
            }
            return null;
        }

        boolean merge(final Element htmlElement, final SdtElement docxElement) {
            final String htmlTextValue = Jdom2.textValueOf(htmlElement);
            if (htmlTextValue == null) {
                return false;
            }

            final R docxR = FirstMatch.matching(docxElement, Types.withType(R.class));
            if (docxR == null) {
                return false;
            }
            return Docx.setText(docxR, htmlTextValue);
        }
    }

    private static void merge(final Element htmlBody, final Body docXBody, final MatchingPolicy matchingPolicy) throws MergeException {
        final List<String> matchedInputIds = Lists.newArrayList();
        final List<String> unmatchedInputIds = Lists.newArrayList();

        final List<Content> htmlBodyContents = htmlBody.getContent();
        for (final Content input : htmlBodyContents) {
            if (!(input instanceof Element)) {
                continue;
            }
            mergeInto((Element) input, docXBody, matchedInputIds, unmatchedInputIds);
        }

        final List<String> unmatchedPlaceHolders = unmatchedPlaceholders(docXBody, matchedInputIds);

        matchingPolicy.unmatchedInputs(unmatchedInputIds);
        matchingPolicy.unmatchedPlaceholders(unmatchedPlaceHolders);
    }

    private static void mergeInto(final Element input, final Body docXBody, final List<String> matchedInputs, final List<String> unmatchedInputs) throws MergeException {

        final String id = Jdom2.attrOf(input, "id");
        if (id == null) {
            throw new MergeException("Missing 'id' attribute for element within body of input HTML");
        }

        final MergeType mergeType = MergeType.lookup(input.getName(), Jdom2.attrOf(input, "class"));
        if (mergeType == null) {
            unmatchedInputs.add(id);
            return;
        }

        final SdtElement docxElement = FirstMatch.matching(docXBody, Docx.withTagVal(id));
        if (docxElement == null) {
            unmatchedInputs.add(id);
            return;
        }

        if (mergeType.merge(input, docxElement)) {
            matchedInputs.add(id);
        } else {
            unmatchedInputs.add(id);
        }
    }

    private static List<String> unmatchedPlaceholders(final Body docXBody, final List<String> matchedIds) {
        final List<SdtElement> taggedElements = AllMatches.matching(docXBody, Docx.withAnyTag());
        final List<String> unmatchedPlaceHolders = taggedElements.stream().map(x -> Docx.tagToValue().apply(x)).collect(Collectors.toList());
        unmatchedPlaceHolders.removeAll(matchedIds);
        return unmatchedPlaceHolders;
    }

    private static File createTempFile() throws MergeException {
        try {
            return File.createTempFile("docx", null);
        } catch (final IOException ex) {
            throw new MergeException("Unable to create temporary working file", ex);
        }
    }
}

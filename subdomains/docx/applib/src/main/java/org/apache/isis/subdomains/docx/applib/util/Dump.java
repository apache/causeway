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

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.Callback;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;

public class Dump {

    public static void main(final String[] args) throws Exception {

        //String filename = "helloWorld.docx";
        String filename = "TypicalDocument.docx";
        File file = new File(System.getProperty("user.dir") + "/" +
        		filename
                );

        System.out.println(file);

        Dump dump = new Dump(file);
        System.out.println("\nPARTS LIST");
        dump.partsList(System.out);
        System.out.println("\n\nDOCUMENT TRAVERSE");
        dump.documentTraverse(System.out);
    }

    private final File file;

    private Map<Part, Part> handled = new HashMap<Part, Part>();

    public Dump(final File file) {
        this.file = file;
    }

    public void partsList(final PrintStream out) throws Exception {

        OpcPackage opcPackage = OpcPackage.load(file);

        // printContentTypes(opcPackage);

        RelationshipsPart rp = opcPackage.getRelationshipsPart();
        StringBuilder sb = new StringBuilder();
        appendInfo(rp, sb, "");
        traverseRelationships(opcPackage, rp, sb, "  ");

        out.println(sb.toString());

        // SaveToZipFile saver = new SaveToZipFile(opcPackage);
        // saver.save(System.getProperty("user.dir") + "/out.docx");
    }

    @SuppressWarnings({ "rawtypes" })
    private void appendInfo(final Part p, final StringBuilder sb, final String indent) {

        String relationshipType = "";
        if (p.getSourceRelationships().size() > 0) {
            relationshipType = p.getSourceRelationships().get(0).getType();
        }

        sb.append("\n" + indent + "Part " + p.getPartName() + " [" + p.getClass().getName() + "] " + relationshipType);

        if (p instanceof JaxbXmlPart) {
            Object o = ((JaxbXmlPart) p).getJaxbElement();
            if (o instanceof javax.xml.bind.JAXBElement) {
                sb.append(" containing JaxbElement:" + XmlUtils.JAXBElementDebug((javax.xml.bind.JAXBElement) o));
            } else {
                sb.append(" containing JaxbElement:" + o.getClass().getName());
            }
        }
    }

    private void traverseRelationships(final OpcPackage opcPackage, final RelationshipsPart rp, final StringBuilder sb, final String indent) {

        // TODO: order by rel id

        for (Relationship r : rp.getRelationships().getRelationship()) {

            // log.info("\nFor Relationship Id=" + r.getId()
            // + " Source is " + rp.getSourceP().getPartName()
            // + ", Target is " + r.getTarget()
            // + " type " + r.getType() + "\n");

            if (r.getTargetMode() != null && r.getTargetMode().equals("External")) {

                sb.append("\n" + indent + "external resource " + r.getTarget() + " of type " + r.getType());
                continue;
            }

            Part part = rp.getPart(r);

            appendInfo(part, sb, indent);
            if (handled.get(part) != null) {
                sb.append(" [additional reference] ");
                continue;
            }
            handled.put(part, part);
            if (part.getRelationshipsPart(false) == null) {
                // sb.append(".. no rels" );
            } else {
                traverseRelationships(opcPackage, part.getRelationshipsPart(false), sb, indent + "    ");
            }
        }
    }

    public void documentTraverse(final PrintStream out) throws Docx4JException {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(file);
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

        org.docx4j.wml.Document wmlDocumentEl = documentPart.getJaxbElement();
        Body body = wmlDocumentEl.getBody();

        new TraversalUtil(body,

            new Callback() {

                String indent = "";

                @Override
                public List<Object> apply(final Object o) {

                    String text = "";
                    if (o instanceof org.docx4j.wml.Text)
                        text = ((org.docx4j.wml.Text) o).getValue();

                    System.out.println(indent + o.getClass().getName() + "  \"" + text + "\"");
                    return null;
                }

                @Override
                public boolean shouldTraverse(final Object o) {
                    return true;
                }

                // Depth first
                @Override
                public void walkJAXBElements(final Object parent) {

                    indent += "    ";

                    List<Object> children = getChildren(parent);
                    if (children != null) {

                        for (Object o : children) {

                            // if its wrapped in javax.xml.bind.JAXBElement, get its
                            // value
                            o = XmlUtils.unwrap(o);

                            this.apply(o);

                            if (this.shouldTraverse(o)) {
                                walkJAXBElements(o);
                            }

                        }
                    }

                    indent = indent.substring(0, indent.length() - 4);
                }

                @Override
                public List<Object> getChildren(final Object o) {
                    return TraversalUtil.getChildrenImpl(o);
                }
            }

        );

    }

}

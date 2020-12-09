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
package org.apache.isis.tooling.cli.doclet;

import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import com.github.javaparser.javadoc.description.JavadocSnippet;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value(staticConstructor = "of")
class ToAsciiDoc {

    private final DocletContext docletContext;

    //TODO method java-doc needs further post processing when spanning multiple paragraphs
    public String javadoc( 
            final @NonNull Javadoc javadoc) {

        val adoc = new StringBuilder();

        javadoc.getDescription().getElements()
        .forEach(e->{

            if(e instanceof JavadocSnippet) {
                adoc.append(normalizeHtmlTags(e.toText()));
            } else if(e instanceof JavadocInlineTag) {
                adoc.append(inlineTag((JavadocInlineTag) e));
            } else {
                adoc.append(e.toText());
            }

        });

        return adoc.toString();
    }

    public String inlineTag(
            final @NonNull JavadocInlineTag inlineTag) {

        val inlineContent = inlineTag.getContent().trim();

        switch(inlineTag.getType()) {
        case LINK:
            val refDoclet = docletContext.getDoclet(inlineContent).orElse(null);
            if(refDoclet!=null) {
                return String.format(" %s ", xref(refDoclet));
            }
        default:
            return String.format(" _%s_ ", inlineContent);
        }
    }
    
    public String xref(
            final @NonNull Doclet doclet) {
        return String.format(" xref:%s[%s] ", 
                String.format(docletContext.getXrefPageIdFormat(), doclet.getName()), 
                doclet.getName()); 
    }

    // -- HELPER 

    /*
     * try to convert HTML formatting directives to normal text  
     */
    private static String normalizeHtmlTags(final @NonNull String s) {
        return s.replace("<p>", "\n").replace("</p>", "");
    }

}

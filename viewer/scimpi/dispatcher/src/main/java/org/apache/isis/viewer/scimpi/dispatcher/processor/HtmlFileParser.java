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


package org.apache.isis.viewer.scimpi.dispatcher.processor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.ParserException;
import org.apache.isis.viewer.scimpi.dispatcher.ElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.action.Attributes;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.view.HtmlSnippet;
import org.apache.isis.viewer.scimpi.dispatcher.view.Snippet;
import org.apache.isis.viewer.scimpi.dispatcher.view.SwfTag;


public class HtmlFileParser {
    private static final Logger LOG = Logger.getLogger(HtmlFileParser.class);
    private final ProcessorLookup processors;

    public HtmlFileParser(ProcessorLookup processors) {
        this.processors = processors;
    }

    public Stack<Snippet> parseHtmlFile(String filePath, RequestContext context) {
        Stack<Snippet> tagsBeforeContent = new Stack<Snippet>();
        Stack<Snippet> tagsAfterContent = new Stack<Snippet>();
        parseHtmlFile("/", filePath, context, tagsBeforeContent, tagsAfterContent);
        return tagsBeforeContent;
    }

    
    public void parseHtmlFile(String parentPath, String filePath, RequestContext context, Stack<Snippet> allTags, Stack<Snippet> tagsForPreviousTemplate ) {
        LOG.debug("parent/file: " + parentPath + " & " + filePath);
        File directory = filePath.startsWith("/") ? new File(".") : new File(parentPath);
        File loadFile = new File(directory.getParentFile(), filePath);
        String loadPath = loadFile.getPath().replace('\\', '/');
        LOG.debug("loading template '" + loadPath + "'");
        InputStream in = context.openStream(loadPath);

        Page page;
        try {
            page = new Page(in, null);
        } catch (UnsupportedEncodingException e) {
            throw new ScimpiException(e);
        }
        Lexer lexer = new Lexer(page);

        Node node = null;
        try {
            Stack<Snippet> tags = allTags;
            String lineNumbers = "0";
            String template = null;
            tags.push(new HtmlSnippet(lineNumbers, filePath));

            // NOTE done like this the tags can be cached for faster processing
            while ((node = lexer.nextNode()) != null) {
                if (node instanceof Remark) {
                    // TODO need to pick up on comments within tags; at the moment this splits a tag into two causing a failure later
                    continue;
                    
                } else if (node instanceof TagNode && ((TagNode) node).getTagName().startsWith("SWF:")) {
                    TagNode tagNode = (TagNode) node;
                    String tagName = tagNode.getTagName().toUpperCase();
                    LOG.debug(tagName);
                   
                    // TODO remove context & request from Attributes -- the tags will be re-used across 
                    // requests 
                    Attributes attributes = new Attributes(tagNode, context); 
                    int type = 0; 
                    if (tagNode.isEndTag()) { 
                        type = SwfTag.END; 
                    } else { 
                        type = tagNode.isEmptyXmlTag() ? SwfTag.EMPTY : SwfTag.START; 
                    } 
                    testForProcessorForTag(lexer, tagName); 
                    lineNumbers = lineNumbering(node); 
                    SwfTag tag = new SwfTag(tagName, attributes, type, lineNumbers, loadFile.getCanonicalPath()); 
                    tags.push(tag); 

                    if (tagName.equals("SWF:IMPORT")) {
                        if (!tagNode.isEmptyXmlTag()) {
                            throw new ScimpiException("Import tag must be empty");
                        }
                        String importFile = tagNode.getAttribute("file");
                        if (context.isDebug()) { 
                            context.getWriter().println("<!-- " +  "import file " + importFile + " -->"); 
                        } 
                        importFile = context.replaceVariables(importFile, true);
                        parseHtmlFile(loadPath, importFile, context, tags, tagsForPreviousTemplate);
                    }

                    if (tagName.equals("SWF:TEMPLATE")) {
                        if (!tagNode.isEmptyXmlTag()) {
                            throw new ScimpiException("Template tag must be empty");
                        }
                        if (template != null) {
                            throw new ScimpiException("Template tag can only be used once within a file");
                        }
                        template = tagNode.getAttribute("file");
                        template = context.replaceVariables(template, true);
                        if (context.isDebug()) { 
                            context.getWriter().println("<!-- " +  "apply template " + template + " -->"); 
                        } 
                        tags =  new Stack<Snippet>();
                    }

                    if (tagName.equals("SWF:CONTENT")) {
                        if (!tagNode.isEmptyXmlTag()) {
                            throw new ScimpiException("Content tag must be empty");
                        }
                        if (context.isDebug()) { 
                            context.getWriter().println("<!-- " +  "insert content into template -->"); 
                        } 
                        tags.addAll(tagsForPreviousTemplate);
                    }
                } else {
                    Snippet snippet = tags.size() == 0 ? null : tags.peek();
                    if (snippet instanceof HtmlSnippet) {
                        ((HtmlSnippet) snippet).append(node.toHtml());
                    } else {
                        HtmlSnippet htmlSnippet = new HtmlSnippet(lineNumbers, filePath);
                        htmlSnippet.append(node.toHtml());
                        tags.push(htmlSnippet);
                    }
                }
                
            }
            in.close();
            
            if (template != null) {
                String filePathRoot = loadPath.startsWith("/") ? "" : "/";
                parseHtmlFile(filePathRoot + loadPath, template, context, allTags, tags);
            }

        } catch (ParserException e) {
            exception(loadPath, node, e);
            //throw new ScimpiException(e);
        } catch (RuntimeException e) {
            // TODO: extend to deal with other exceptions
            exception(loadPath, node, e);
        } catch (IOException e) {
            throw new ScimpiException(e);
        }
    }

    private void exception(String filePath, Node node, Exception e) {
        String lineNumbers = "";
        String element = ("" + node).toLowerCase();
        if (node instanceof TagNode) {
            lineNumbers = ":" + lineNumbering(node);
            element = "tag &lt;" + node.getText() + "&gt;";
        }
        throw new ScimpiException("Error processing " + element + " in " + filePath + lineNumbers, e);
    }

    private String lineNumbering(Node node) {
        String lineNumbers;
        int startingLine = ((TagNode) node).getStartingLineNumber() + 1;
        int endingLine = ((TagNode) node).getStartingLineNumber() + 1;
        if (startingLine == endingLine) {
            lineNumbers = "" + startingLine;
        } else {
            lineNumbers = startingLine + "-" + endingLine;
        }
        return lineNumbers;
    }

    private void testForProcessorForTag(Lexer lexer, String tagName) {
        ElementProcessor elementProcessor = processors.getFor(tagName);
        if (elementProcessor == null) {
            throw new ScimpiException("No processor for tag " + tagName.toLowerCase());
        }
    }
}


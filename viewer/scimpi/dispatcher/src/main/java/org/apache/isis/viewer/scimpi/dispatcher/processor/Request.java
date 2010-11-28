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

import java.util.Stack;

import org.apache.isis.viewer.scimpi.dispatcher.BlockContent;
import org.apache.isis.viewer.scimpi.dispatcher.ElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.action.Attributes;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.view.HtmlSnippet;
import org.apache.isis.viewer.scimpi.dispatcher.view.Snippet;
import org.apache.isis.viewer.scimpi.dispatcher.view.SwfTag;
import org.apache.log4j.Logger;


public class Request implements PageWriter {

    public class RepeatMarker {
        private final int index;

        private RepeatMarker(int index) {
            this.index = index;
        }

        public void repeat() {
            Request.this.index = index;
        }
    }

    private static Logger LOG = Logger.getLogger(Request.class);
    public static final boolean ENSURE_VARIABLES_EXIST = true;
    public static final boolean NO_VARIABLE_CHECKING = false;

    private final RequestContext context;
    private final Stack<Snippet> snippets;
    private final Stack<StringBuffer> buffers;
    private final Stack<BlockContent> blocks;
    private final ProcessorLookup processors;
    private int index = -1;
    private final String path;

    public Request(String path, RequestContext context, Stack<Snippet> snippets, ProcessorLookup processors) {
        this.path = path;
        this.context = context;
        this.snippets = snippets;
        this.processors = processors;

        buffers = new Stack<StringBuffer>();
        blocks = new Stack<BlockContent>();
        pushNewBuffer();
    }

    public void processNextTag() {
        while (index < snippets.size() - 1) {
            index++;
            Snippet snippet = snippets.get(index);
            if (snippet instanceof HtmlSnippet) {
                append(snippet);
            } else {
                SwfTag tag = (SwfTag) snippet;
                String name = tag.getName();
                ElementProcessor processor = processors.getFor(name);
                if (context.isDebug()) { 
                    context.getWriter().println("<!-- " +  "process " + tag + " -->"); 
                } 
                process(tag, processor);
            }
        }
    }

    private void append(Snippet snippet) {
        try {
            String html = snippet.getHtml();
            if (((HtmlSnippet) snippet).isContainsVariable()) {
                html = context.replaceVariables(html, true);
            }
            appendHtml(html);
        } catch (TagProcessingException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new TagProcessingException(snippet.errorAt(), e);
        }
    }

    public void appendHtml(String html) {
        StringBuffer buffer = buffers.peek();
        buffer.append(html);
    }

    private void process(SwfTag tag, ElementProcessor processor) {
        try {
            LOG.debug("processing " + processor.getName() + " " + tag);
            processor.process(this);
        } catch (TagProcessingException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new TagProcessingException(tag.errorAt(), e);
        }
    }

    public void processUtilCloseTag() {
        SwfTag tag = getTag();
        if (tag.getType() == SwfTag.EMPTY) {
            return;
        }
        while (index < snippets.size() - 1) {
            index++;
            Snippet snippet = snippets.get(index);
            if (snippet instanceof HtmlSnippet) {
                append(snippet);
            } else {
                SwfTag nextTag = (SwfTag) snippet;
                if (tag.getName().equals(nextTag.getName())) {
                    if (nextTag.getType() == SwfTag.START) {} else {
                        return;
                    }
                }
                String name = nextTag.getName();
                ElementProcessor processor = processors.getFor(name);
                process(nextTag, processor);
            }
        }
    }

    public void skipUntilClose() {
        SwfTag tag = getTag();
        if (tag.getType() == SwfTag.EMPTY) {
            return;
        }
        int depth = 1;
        while (index < snippets.size() - 1) {
            index++;
            Snippet snippet = snippets.get(index);
            if (snippet instanceof SwfTag) {
                SwfTag nextTag = (SwfTag) snippet;
                if (tag.getName().equals(nextTag.getName())) {
                    if (nextTag.getType() == SwfTag.START) {
                        depth++;
                    } else {
                        depth--;
                        if (depth == 0) {
                            return;
                        }
                    }
                }
            }
        }
    }

    public void closeEmpty() {
        SwfTag tag = getTag();
        if (tag.getType() == SwfTag.EMPTY) {
            return;
        }
        if (index < snippets.size()) {
            Snippet snippet = snippets.get(index);
            if (snippet instanceof SwfTag) {
                SwfTag nextTag = (SwfTag) snippet;
                if (nextTag.getType() == SwfTag.EMPTY) {
                    return;
                }
            }
        }
        throw new ScimpiException("Empty tag not closed");

    }

    public void pushNewBuffer() {
        StringBuffer buffer = new StringBuffer();
        buffers.push(buffer);
    }

    public String popBuffer() {
        String content = buffers.pop().toString();
        return content;
    }

    public SwfTag getTag() {
        return (SwfTag) snippets.get(index);
    }

    public RequestContext getContext() {
        return context;
    }

    // TODO rename to pushBlock()
    public void setBlockContent(BlockContent content) {
        blocks.add(content);
    }

    public BlockContent popBlockContent() {
        return blocks.pop();
    }

    public BlockContent getBlockContent() {
        return blocks.peek();
    }

    public String getViewPath() {
        return path;
    }

    public String getOptionalProperty(String name, String defaultValue) {
        return getOptionalProperty(name, defaultValue, true);
    }

    public String getOptionalProperty(String name, String defaultValue, boolean ensureVariablesExists) {
        Attributes attributes = getTag().getAttributes();
        return attributes.getOptionalProperty(name, defaultValue, ensureVariablesExists);
    }

    public String getOptionalProperty(String name) {
        return getOptionalProperty(name, true);
    }

    public String getOptionalProperty(String name, boolean ensureVariablesExists) {
        Attributes attributes = getTag().getAttributes();
        return attributes.getOptionalProperty(name, ensureVariablesExists);
    }

    public Attributes getAttributes() {
        return getTag().getAttributes();
    }

    public String getRequiredProperty(String name) {
        return getRequiredProperty(name, true);
    }

    public String getRequiredProperty(String name, boolean ensureVariablesExists) {
        Attributes attributes = getTag().getAttributes();
        return attributes.getRequiredProperty(name, ensureVariablesExists);
    }

    public boolean isRequested(String name) {
        Attributes attributes = getTag().getAttributes();
        return attributes.isRequested(name);
    }

    public boolean isRequested(String name, boolean defaultValue) {
        Attributes attributes = getTag().getAttributes();
        return attributes.isRequested(name, defaultValue);
    }

    public boolean isPropertySet(String name) {
        Attributes attributes = getTag().getAttributes();
        return attributes.isPropertySet(name);
    }

    public boolean isPropertySpecified(String name) {
        Attributes attributes = getTag().getAttributes();
        return attributes.isPropertySpecified(name);
    }

    public RepeatMarker createMarker() {
        return new RepeatMarker(index);
    }
}


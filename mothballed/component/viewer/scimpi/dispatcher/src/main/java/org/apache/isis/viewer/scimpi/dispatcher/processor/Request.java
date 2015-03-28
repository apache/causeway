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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.viewer.scimpi.dispatcher.BlockContent;
import org.apache.isis.viewer.scimpi.dispatcher.ElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.action.Attributes;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.view.HtmlSnippet;
import org.apache.isis.viewer.scimpi.dispatcher.view.Snippet;
import org.apache.isis.viewer.scimpi.dispatcher.view.SwfTag;

public class Request implements PageWriter {

    public class RepeatMarker {
        private final int index;

        private RepeatMarker(final int index) {
            this.index = index;
        }

        public void repeat() {
            Request.this.index = index;
        }
    }

    private static Logger LOG = LoggerFactory.getLogger(Request.class);
    public static final boolean ENSURE_VARIABLES_EXIST = true;
    public static final boolean NO_VARIABLE_CHECKING = false;
    private static Encoder encoder;

    public static Encoder getEncoder() {
        return encoder;
    }

    private final RequestContext context;
    private final Stack<Snippet> snippets;
    private final Stack<StringBuffer> buffers;
    private final Stack<BlockContent> blocks;
    private final ProcessorLookup processors;
    private int nextFormId;
    private int index = -1;
    private final String path;

    public Request(final String path, final RequestContext context, final Encoder encoder, final Stack<Snippet> snippets, final ProcessorLookup processors) {
        this.path = path;
        this.context = context;
        Request.encoder = encoder;
        this.snippets = snippets;
        this.processors = processors;

        buffers = new Stack<StringBuffer>();
        blocks = new Stack<BlockContent>();
        pushNewBuffer();
    }

    public void processNextTag() {
        while (index < snippets.size() - 1) {
            index++;
            final Snippet snippet = snippets.get(index);
            if (snippet instanceof HtmlSnippet) {
                appendSnippet((HtmlSnippet) snippet);
            } else {
                final SwfTag tag = (SwfTag) snippet;
                final String name = tag.getName();
                final ElementProcessor processor = processors.getFor(name);
                process(tag, processor);
                if (context.isAborted()) {
                    return;
                }
            }
        }
    }

    private void appendSnippet(final HtmlSnippet snippet) {
        String html = snippet.getHtml();
        try {
            if (snippet.isContainsVariable()) {
                html = context.replaceVariables(html);
            }
            appendHtml(html);
        } catch (final TagProcessingException e) {
            throw e;
        } catch (final RuntimeException e) {
            final String replace = "<";
            final String withReplacement = "&lt;";
            html = html.replaceAll(replace, withReplacement);

            throw new TagProcessingException("Error while processing html block at " + snippet.errorAt() + " - " + e.getMessage(), html, e);
        }
    }

    @Override
    public void appendAsHtmlEncoded(final String string) {
        appendHtml(encodeHtml(string));
        // appendHtml(string);
    }

    @Override
    public void appendHtml(final String html) {
        final StringBuffer buffer = buffers.peek();
        buffer.append(html);
    }

    public void appendDebug(final String line) {
        context.appendDebugTrace(encodeHtml(line));
    }

    private String encodeHtml(final String text) {
        return encoder.encoder(text);
    }

    public void appendTruncated(String text, final int truncateTo) {
        if (truncateTo > 0 && text.length() > truncateTo) {
            text = text.substring(0, truncateTo) + "...";
        }
        appendAsHtmlEncoded(text);
    }

    private void process(final SwfTag tag, final ElementProcessor processor) {
        try {
            LOG.debug("processing " + processor.getName() + " " + tag);
            appendDebug("\n" + tag.debug());
            if (tag.getType() == SwfTag.END) {
                throw new TagProcessingException(tag.errorAt() + " - end tag mistaken for a start tag", tag.toString());
            }
            processor.process(this);
        } catch (final TagProcessingException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw new TagProcessingException("Error while processing " + tag.getName().toLowerCase() + " element at " + tag.errorAt() + " - " + e.getMessage(), tag.toString(), e);
        }
    }

    public void processUtilCloseTag() {
        final SwfTag tag = getTag();
        if (tag.getType() == SwfTag.EMPTY) {
            return;
        }
        while (index < snippets.size() - 1) {
            index++;
            final Snippet snippet = snippets.get(index);
            if (snippet instanceof HtmlSnippet) {
                appendSnippet((HtmlSnippet) snippet);
            } else {
                final SwfTag nextTag = (SwfTag) snippet;
                if (tag.getName().equals(nextTag.getName())) {
                    if (nextTag.getType() == SwfTag.START) {
                    } else {
                        return;
                    }
                }
                final String name = nextTag.getName();
                if (nextTag.getType() == SwfTag.END && !tag.getName().equals(name)) {
                    throw new TagProcessingException("Expected " + nextTag.getName().toLowerCase() + " tag but found " + tag.getName().toLowerCase() + " tag at " + nextTag.errorAt(), tag.toString());
                }
                final ElementProcessor processor = processors.getFor(name);
                process(nextTag, processor);
            }
        }
    }

    public void skipUntilClose() {
        final SwfTag tag = getTag();
        if (tag.getType() == SwfTag.EMPTY) {
            if (context.isDebug()) {
                appendHtml("<!-- " + "skipped " + tag + " -->");
            }
            return;
        }
        int depth = 1;
        while (index < snippets.size() - 1) {
            index++;
            final Snippet snippet = snippets.get(index);
            if (snippet instanceof SwfTag) {
                final SwfTag nextTag = (SwfTag) snippet;
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
        final SwfTag tag = getTag();
        if (tag.getType() == SwfTag.EMPTY) {
            return;
        }
        if (index < snippets.size()) {
            final Snippet snippet = snippets.get(index);
            if (snippet instanceof SwfTag) {
                final SwfTag nextTag = (SwfTag) snippet;
                if (nextTag.getType() == SwfTag.EMPTY) {
                    return;
                }
            }
        }
        throw new ScimpiException("Empty tag not closed");
    }

    public void pushNewBuffer() {
        final StringBuffer buffer = new StringBuffer();
        buffers.push(buffer);
    }

    public String popBuffer() {
        final String content = buffers.pop().toString();
        return content;
    }

    public SwfTag getTag() {
        return (SwfTag) snippets.get(index);
    }

    public RequestContext getContext() {
        return context;
    }

    // TODO rename to pushBlock()
    public void setBlockContent(final BlockContent content) {
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

    public String nextFormId() {
        return String.valueOf(nextFormId++);
    }

    public String getOptionalProperty(final String name, final String defaultValue) {
        return getOptionalProperty(name, defaultValue, true);
    }

    public String getOptionalProperty(final String name, final String defaultValue, final boolean ensureVariablesExists) {
        final Attributes attributes = getTag().getAttributes();
        return attributes.getOptionalProperty(name, defaultValue, ensureVariablesExists);
    }

    public String getOptionalProperty(final String name) {
        return getOptionalProperty(name, true);
    }

    public String getOptionalProperty(final String name, final boolean ensureVariablesExists) {
        final Attributes attributes = getTag().getAttributes();
        return attributes.getOptionalProperty(name, ensureVariablesExists);
    }

    public Attributes getAttributes() {
        return getTag().getAttributes();
    }

    public String getRequiredProperty(final String name) {
        return getRequiredProperty(name, true);
    }

    public String getRequiredProperty(final String name, final boolean ensureVariablesExists) {
        final Attributes attributes = getTag().getAttributes();
        return attributes.getRequiredProperty(name, ensureVariablesExists);
    }

    public boolean isRequested(final String name) {
        final Attributes attributes = getTag().getAttributes();
        return attributes.isRequested(name);
    }

    public boolean isRequested(final String name, final boolean defaultValue) {
        final Attributes attributes = getTag().getAttributes();
        return attributes.isRequested(name, defaultValue);
    }

    public boolean isPropertySet(final String name) {
        final Attributes attributes = getTag().getAttributes();
        return attributes.isPropertySet(name);
    }

    public boolean isPropertySpecified(final String name) {
        final Attributes attributes = getTag().getAttributes();
        return attributes.isPropertySpecified(name);
    }

    public RepeatMarker createMarker() {
        return new RepeatMarker(index);
    }
}

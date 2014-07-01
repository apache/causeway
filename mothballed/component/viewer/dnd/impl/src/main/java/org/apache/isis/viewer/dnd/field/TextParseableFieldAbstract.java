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

package org.apache.isis.viewer.dnd.field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.content.TextParseableContent;

public abstract class TextParseableFieldAbstract extends AbstractField {
    private static final Logger LOG = LoggerFactory.getLogger(TextField.class);

    protected TextParseableFieldAbstract(final Content content, final ViewSpecification design) {
        super(content, design);
    }

    @Override
    protected boolean provideClearCopyPaste() {
        return true;
    }

    @Override
    protected void pasteFromClipboard() {
        try {
            final String text = (String) getViewManager().getClipboard(String.class);
            final TextParseableContent content = (TextParseableContent) getContent();
            content.parseTextEntry(text);
            content.entryComplete();
            LOG.debug("pasted " + text);
        } catch (final Throwable e) {
            LOG.error("invalid paste operation " + e);
        }
    }

    @Override
    protected Consent canClear() {
        final TextParseableContent field = (TextParseableContent) getContent();
        return field.canClear();
    }

    @Override
    protected void clear() {
        try {
            final TextParseableContent content = (TextParseableContent) getContent();
            content.parseTextEntry("");
            content.entryComplete();
            LOG.debug("cleared");
        } catch (final Throwable e) {
            LOG.error("invalid paste operation " + e);
        }
    }

    @Override
    protected void copyToClipboard() {
        final TextParseableContent content = (TextParseableContent) getContent();
        final ObjectAdapter object = content.getAdapter();
        if (object != null) {
            final String text = object.titleString();
            getViewManager().setClipboard(text, String.class);
            LOG.debug("copied " + text);
        }
    }

    @Override
    public boolean isEmpty() {
        final TextParseableContent content = (TextParseableContent) getContent();
        return content.isEmpty();
    }

    @Override
    public Consent canChangeValue() {
        final TextParseableContent cont = (TextParseableContent) getContent();
        return cont.isEditable();
    }

    protected void saveValue(final ObjectAdapter value) {
        parseEntry(value.titleString());
    }

    protected void parseEntry(final String entryText) {
        final TextParseableContent content = (TextParseableContent) getContent();
        content.parseTextEntry(entryText);
        content.entryComplete();
    }

}

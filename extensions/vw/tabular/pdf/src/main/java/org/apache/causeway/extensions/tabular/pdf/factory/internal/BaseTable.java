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
package org.apache.causeway.extensions.tabular.pdf.factory.internal;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class BaseTable extends Table<PDPage> {

    public BaseTable(final float yStart, final float yStartNewPage, final float bottomMargin, final float width,
            final float margin, final PDDocument document, final PDPage currentPage, final boolean drawLines,
            final boolean drawContent) throws IOException {
        super(yStart, yStartNewPage, 0, bottomMargin, width, margin, document, currentPage, drawLines, drawContent,
                new PageProvider(document, currentPage.getMediaBox()));
    }

    public BaseTable(final float yStart, final float yStartNewPage, final float pageTopMargin, final float bottomMargin,
            final float width, final float margin, final PDDocument document, final PDPage currentPage, final boolean drawLines,
            final boolean drawContent) throws IOException {
        super(yStart, yStartNewPage, pageTopMargin, bottomMargin, width, margin, document, currentPage, drawLines, drawContent,
                new PageProvider(document, currentPage.getMediaBox()));
    }

    public BaseTable(final float yStart, final float yStartNewPage, final float pageTopMargin, final float bottomMargin,
            final float width, final float margin, final PDDocument document, final PDPage currentPage, final boolean drawLines,
            final boolean drawContent, final PageProvider pageProvider) throws IOException {
        super(yStart, yStartNewPage, pageTopMargin, bottomMargin, width, margin, document, currentPage, drawLines, drawContent, pageProvider);
    }

    @Override
    protected void loadFonts() {
        // Do nothing as we don't have any fonts to load
    }

}

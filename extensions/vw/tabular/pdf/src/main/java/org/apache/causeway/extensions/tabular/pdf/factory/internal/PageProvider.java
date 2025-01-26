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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import lombok.Getter;

final class PageProvider {

    @Getter private final PDDocument document;
    private final PDRectangle size;
    private int currentPageIndex = -1;

    PageProvider(final PDDocument document, final PDRectangle size) {
        this.document = document;
        this.size = size;
    }

    public PDPage createPage() {
        currentPageIndex = document.getNumberOfPages();
        return getCurrentPage();
    }

    public PDPage nextPage() {
        if (currentPageIndex == -1) {
            currentPageIndex = document.getNumberOfPages();
        } else {
            currentPageIndex++;
        }
        return getCurrentPage();
    }

    public PDPage previousPage() {
        currentPageIndex--;
        if (currentPageIndex < 0) {
            currentPageIndex = 0;
        }
        return getCurrentPage();
    }

    private PDPage getCurrentPage() {
        if (currentPageIndex >= document.getNumberOfPages()) {
            final PDPage newPage = new PDPage(size);
            document.addPage(newPage);
            return newPage;
        }
        return document.getPage(currentPageIndex);
    }

}

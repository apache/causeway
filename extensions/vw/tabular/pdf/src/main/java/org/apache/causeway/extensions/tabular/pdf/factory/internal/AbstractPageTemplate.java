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
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

abstract class AbstractPageTemplate extends PDPage {

    protected abstract PDDocument getDocument();

    protected abstract float yStart();

    protected void addPicture(final PDImageXObject ximage, final float cursorX, final float cursorY, final int width, final int height) throws IOException {

        PDPageContentStream contentStream = new PDPageContentStream(getDocument(), this,
                PDPageContentStream.AppendMode.APPEND, false);
        contentStream.drawImage(ximage, cursorX, cursorY, width, height);
        contentStream.close();
    }

    protected PDImage loadPicture(final String nameJPGFile) throws IOException {
        return PDImageXObject.createFromFile(nameJPGFile, getDocument());
    }

}

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
package org.apache.causeway.client.kroviz.utils.js

@JsModule("pdfjs")
@JsNonModule
// https://stackoverflow.com/questions/9328551/how-to-use-pdf-js
external object PdfJs {
// Fetch the PDF document from the URL using promises
//    PDFJS.getDocument('helloworld.pdf').then(function(pdf) {
    // Using promise to fetch the page
    //      pdf.getPage(1).then(function(page) {
    //         var scale = 1.5;
    //         var viewport = page.getViewport(scale);

    // Prepare canvas using PDF page dimensions
    //       var canvas = document.getElementById('the-canvas');
    //     var context = canvas.getContext('2d');
    //    canvas.height = viewport.height;
    //   canvas.width = viewport.width;

    // Render PDF page into canvas context
    // var renderContext = {
    //         canvasContext: context,
    //        viewport: viewport
    // };
    //  page.render(renderContext);
    //  });
    // });

/*    fun createDoc(): PdfJs {
        var doc = PdfJs()
        doc.text(20, 20, 'hello, I am PDF.');
        doc.text(20, 30, 'i was created in the browser using javascript.');
        doc.text(20, 40, 'i can also be created from node.js');

       doc.setProperties({
                title: 'A sample document created by pdf.js',
                subject: 'PDFs are kinda cool, i guess',
                author: 'Marak Squires',
                keywords: 'pdf.js, javascript, Marak, Marak Squires',
                creator: 'pdf.js'
        });

        doc.addPage();
        doc.setFontSize(22);
        doc.text(20, 20, 'This is a title');
        doc.setFontSize(16);
        doc.text(20, 30, 'This is some normal sized text underneath.');
    }*/
}

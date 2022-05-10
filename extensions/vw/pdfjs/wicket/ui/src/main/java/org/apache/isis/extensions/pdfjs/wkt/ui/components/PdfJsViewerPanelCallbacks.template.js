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

if (typeof(PdfJsViewerPanel) !== 'object') {
    window.PdfJsViewerPanel = {};

    if (typeof(PdfJsViewerPanel.Callbacks) !== 'object') {
        PdfJsViewerPanel.Callbacks = {
            updatePageNum: function (pageNum) {
                Wicket.Ajax.get({'u':'${pageNumCallbackUrl}&pageNum=' + pageNum})
            },
            updateScale: function (scale) {
                Wicket.Ajax.get({'u':'${scaleCallbackUrl}&scale=' + scale})
            },
            updateHeight: function (height) {
                Wicket.Ajax.get({'u':'${heightCallbackUrl}&height=' + height})
            }
        };
    }

}



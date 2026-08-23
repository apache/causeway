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
package demoapp.dom._infra;

import org.springframework.stereotype.Service;

import org.apache.causeway.extensions.pdfjs.applib.config.Scale;
import org.apache.causeway.extensions.pdfjs.applib.spi.PdfJsViewerAdvisor;

@Service
public class PdfJsViewerAdvisorFallback implements PdfJsViewerAdvisor {

    @Override
    public Advice advise(final InstanceKey instanceKey) {
        return new Advice(1, Scale._1_00, 800);
    }

    @Override
    public void pageNumChangedTo(final InstanceKey instanceKey, final int pageNum) {
    }

    @Override
    public void scaleChangedTo(final InstanceKey instanceKey, final Scale scale) {
    }

    @Override
    public void heightChangedTo(final InstanceKey instanceKey, final int height) {
    }
}

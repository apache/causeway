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
package org.apache.causeway.viewer.wicket.ui.components.widgets.fileinput;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;

import org.apache.causeway.viewer.wicket.ui.util.WktHeaderItems;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.BootstrapFileInputField;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.FileInputConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

public class FileUploadFieldWithNestingFix extends BootstrapFileInputField {

    private static final long serialVersionUID = 1L;

    @Getter(lazy = true) @Accessors(fluent = true)
    private static final HeaderItem headerItem =
                WktHeaderItems.forScriptReferenceAsOnDomReady(
                        FileUploadFieldWithNestingFix.class, "causeway-file-upload-nesting-fix.nocompress.js");

    public FileUploadFieldWithNestingFix(final String id,
            final IModel<List<FileUpload>> model, final FileInputConfig config) {
        super(id, model, config);
    }

    @Override @SneakyThrows
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(headerItem());
    }

    @Override
    public boolean isRequired() {
        //FIXME[ISIS-3203]
        return false; // nothing else worked yet
    }

//experiments ...
//        @Override
//        public void convertInput() {
//            super.convertInput(); // keep side-effects
//            if(!isRequired()) {return;}
//            /*[ISIS-3203]: in the context of mandatory property or action parameter negotiation,
//             * we need to set the converted input to something other than null, even an empty list will do
//             */
//            if(isConvertedInputNull()
//                    && !isModelEmpty()) {
//                super.setConvertedInput(Collections.emptyList()); // always pass
//            }
//        }
//        @Override
//        public boolean checkRequired() {
//            super.checkRequired(); // keep side-effects
//            return true; // always pass otherwise workaround won't work
//        }
//        private boolean isModelEmpty() { return getModel().getObject()==null; }
//        private boolean isConvertedInputNull() { return getConvertedInput()==null; }

}

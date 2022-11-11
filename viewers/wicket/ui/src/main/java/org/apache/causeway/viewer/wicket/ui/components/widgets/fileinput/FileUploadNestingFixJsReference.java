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

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;

import org.apache.causeway.commons.internal.base._Text;

import lombok.SneakyThrows;

final class FileUploadNestingFixJsReference implements IHeaderContributor {
    private static final long serialVersionUID = 1L;
    public static final FileUploadNestingFixJsReference INSTANCE = new FileUploadNestingFixJsReference();
    private String jsScriptSource;

    @SneakyThrows
    private FileUploadNestingFixJsReference() {
        this.jsScriptSource = _Text.readLinesFromResource(
                FileUploadNestingFixJsReference.class, "file-upload-nesting-fix.js", StandardCharsets.UTF_8)
                .stream()
                .skip(18) // skip license header
                .collect(Collectors.joining("\n"));
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        response.render(OnDomReadyHeaderItem.forScript(jsScriptSource));
    }
}

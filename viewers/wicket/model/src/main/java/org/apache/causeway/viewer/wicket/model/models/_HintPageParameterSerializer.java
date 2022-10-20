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
package org.apache.causeway.viewer.wicket.model.models;

import java.io.Serializable;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.hint.HintStore;
import org.apache.causeway.viewer.wicket.model.util.ComponentHintKey;

import lombok.val;

class _HintPageParameterSerializer implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String PREFIX = "hint-";

    private _HintPageParameterSerializer() {}

    public static PageParameters hintStoreToPageParameters(
            final HintStore hintStore,
            final PageParameters pageParameters,
            final Bookmark bookmark) {

        if(bookmark!=null) {
            for (val hintKey : hintStore.findHintKeys(bookmark)) {
                ComponentHintKey.create(hintStore, hintKey).hintTo(bookmark, pageParameters, PREFIX);
            }
        }
        return pageParameters;
    }

}

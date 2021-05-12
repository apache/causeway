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
package org.apache.isis.viewer.wicket.model.models;

import java.io.Serializable;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.hint.HintStore;
import org.apache.isis.viewer.wicket.model.util.ComponentHintKey;

import lombok.val;

class HintPageParameterSerializer implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String PREFIX = "hint-";

    private HintPageParameterSerializer() {}

    public static void hintStoreToPageParameters(
            final PageParameters pageParameters,
            final ManagedObjectModel objectModel) {

        val bookmark = objectModel.asHintingBookmarkIfSupported();
        val hintStore = objectModel.getCommonContext().lookupServiceElseFail(HintStore.class);
        hintStoreToPageParameters(pageParameters, bookmark, hintStore);
    }

    // -- HELPER

    private static void hintStoreToPageParameters(
            final PageParameters pageParameters,
            final Bookmark bookmark,
            final HintStore hintStore) {

        if(bookmark==null) {
            return;
        }
        for (val hintKey : hintStore.findHintKeys(bookmark)) {
            ComponentHintKey.create(hintStore, hintKey).hintTo(bookmark, pageParameters, PREFIX);
        }
    }

}

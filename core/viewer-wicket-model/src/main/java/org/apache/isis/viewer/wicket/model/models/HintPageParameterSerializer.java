/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.model.models;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.isis.commons.internal.collections._Lists;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.hint.HintStore;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.util.ComponentHintKey;

class HintPageParameterSerializer implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String PREFIX = "hint-";

    private HintPageParameterSerializer() {}

    public static void hintStoreToPageParameters(final PageParameters pageParameters, final EntityModel entityModel) {
        ObjectAdapterMemento objectAdapterMemento = entityModel.getObjectAdapterMemento();
        hintStoreToPageParameters(pageParameters, objectAdapterMemento);
    }

    public static void hintStoreToPageParameters(
            final PageParameters pageParameters,
            final ObjectAdapterMemento objectAdapterMemento) {
        if(objectAdapterMemento == null) {
            return;
        }
        final HintStore hintStore = getHintStore();
        final Bookmark bookmark = objectAdapterMemento.asHintingBookmark();
        Set<String> hintKeys = hintStore.findHintKeys(bookmark);
        for (String hintKey : hintKeys) {
            ComponentHintKey.create(hintKey).hintTo(bookmark, pageParameters, PREFIX);
        }
    }


    public static void updateHintStoreUNUSED(
            final PageParameters pageParameters,
            final ObjectAdapterMemento objectAdapterMemento) {
        if(objectAdapterMemento == null) {
            return;
        }
        Set<String> namedKeys = pageParameters.getNamedKeys();
        if (namedKeys.contains("no-hints")) {
            getHintStore().removeAll(objectAdapterMemento.asHintingBookmark());
            return;
        }
        List<ComponentHintKey> newComponentHintKeys = _Lists.newArrayList();
        for (String namedKey : namedKeys) {
            if (namedKey.startsWith(PREFIX)) {
                String value = pageParameters.get(namedKey).toString(null);
                String key = namedKey.substring(5);
                final ComponentHintKey componentHintKey = ComponentHintKey.create(key);
                newComponentHintKeys.add(componentHintKey);
                final Bookmark bookmark = objectAdapterMemento.asHintingBookmark();
                componentHintKey.set(bookmark, value);
            }
        }
    }

    private static HintStore getHintStore() {
        return getIsisSessionFactory().getServicesInjector().lookupService(HintStore.class);
    }

    private static IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

}

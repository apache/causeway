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

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Common;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Common.Application;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Wicket;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;

public interface HasCommonContext extends HasMetaModelContext {

    default Common getCommonViewerSettings() {
        return getConfiguration().getViewer().getCommon();
    }

    default Wicket getWicketViewerSettings() {
        return getConfiguration().getViewer().getWicket();
    }

    default Application getApplicationSettings() {
        return getCommonViewerSettings().getApplication();
    }

    /**
     * Translate without context: Tooltips, Button-Labels, etc.
     */
    default String translate(final String input) {
        return getTranslationService().translate(TranslationContext.empty(), input);
    }

    default String translate(final TranslationContext tc, final String text) {
        return getTranslationService().translate(tc, text);
    }

    default boolean isPrototyping() {
        return getSystemEnvironment().isPrototyping();
    }

}

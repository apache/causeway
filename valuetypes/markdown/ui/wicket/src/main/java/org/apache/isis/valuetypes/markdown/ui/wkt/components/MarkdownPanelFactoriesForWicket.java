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
package org.apache.isis.valuetypes.markdown.ui.wkt.components;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.valuetypes.markdown.applib.value.Markdown;
import org.apache.isis.viewer.wicket.ui.components.scalars.markup.MarkupComponentFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.markup.MarkupPanelFactories;

import lombok.val;

/**
 * @implNote Almost a copy of {@code Parented} and {@code Standalone} in
 * {@link MarkupPanelFactories}, but specific to
 * the {@link Markdown} value-type which requires client-side java-script to be
 * executed to enable syntax highlighting
 */
public class MarkdownPanelFactoriesForWicket {

    // -- PARENTED

    @Programmatic
    public static class Parented extends MarkupPanelFactories.ParentedAbstract {
        private static final long serialVersionUID = 1L;

        public Parented() {
            super(Markdown.class);
        }

        @Override
        protected MarkupComponentFactory getMarkupComponentFactory() {
            return (id, model) -> {
                val markupComponent = new org.apache.isis.valuetypes.markdown.ui.wkt.components.MarkdownComponent(id, model);
                markupComponent.setEnabled(false);
                return markupComponent;
            };

        }

    }

    // -- STANDALONE

    @Programmatic
    public static class Standalone extends MarkupPanelFactories.StandaloneAbstract {
        private static final long serialVersionUID = 1L;

        public Standalone() {
            super(Markdown.class);
        }

        @Override
        protected MarkupComponentFactory getMarkupComponentFactory() {
            return (id, model) -> {
                val markupComponent = new MarkdownComponent(id, model);
                return markupComponent;
            };
        }

    }


}

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
package org.apache.isis.viewer.wicket.ui.components.scalars.string;

import java.util.EnumSet;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Fragment;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.CompactFragment;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldWithValueSemantics;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

/**
 * Panel for rendering titles for scalars of any type.
 */
public class ScalarTitlePanel<T> extends ScalarPanelTextFieldWithValueSemantics<T> {

    private static final long serialVersionUID = 1L;

    public ScalarTitlePanel(final String id, final ScalarModel scalarModel, final Class<T> type) {
        super(id, scalarModel, type);
    }

    @Override
    protected void setupFormatModifiers(final EnumSet<FormatModifier> modifiers) {
        modifiers.add(FormatModifier.MARKUP);
    }

    @Override
    protected Component createComponentForOutput(final String id) {

        System.err.printf("%s%n", id);

        if(id.equals("container-scalarValue-inputFormat")) {
            val badgeFragment = //CompactFragment.BADGE.createFragment(this)
                    new Fragment(id, "fragment-compact-badge", this)
                    ;
            Wkt.labelAdd(badgeFragment, "scalarValue", obtainOutputFormatModel());
            return badgeFragment;

        } else {
            return Wkt.labelAdd(
                  CompactFragment.BADGE.createFragment(this),
                  id,
                  obtainOutputFormatModel());
        }

    }

//
//    static class TitleField<T> extends AbstractTextComponent<T> {
//        private static final long serialVersionUID = 1L;
//
//        final IModel<T> model;
//        final Class<T> type;
//        final @Nullable IConverter<T> converter;
//
//        public TitleField(final String id, final IModel<T> model, final Class<T> type,
//                final @Nullable IConverter<T> converter) {
//            super(id);
//            this.model = model;
//            this.type = type;
//            this.converter = converter;
//
//        }
//
//        @SuppressWarnings("unchecked")
//        @Override public <C> IConverter<C> getConverter(final Class<C> cType) {
//            return cType == type
//                    ? (IConverter<C>) converter
//                    : super.getConverter(cType);}
//        @Override public void error(final IValidationError error) {
//            //errorMessageIgnoringResourceBundles(this, error);
//        }
//
//    }

}

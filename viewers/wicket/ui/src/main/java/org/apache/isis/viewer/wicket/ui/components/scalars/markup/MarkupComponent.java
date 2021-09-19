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
package org.apache.isis.viewer.wicket.ui.components.scalars.markup;

import java.util.Optional;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;

public class MarkupComponent extends WebComponent {

    private static final long serialVersionUID = 1L;

    public MarkupComponent(final String id, final IModel<?> model){
        super(id, model);
    }

    @Override
    public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag){
        final var htmlContent = extractHtmlOrElse(getDefaultModelObject(), "" /*fallback*/);
        replaceComponentTagBody(markupStream, openTag, htmlContent);
    }

    @Override
    protected void onComponentTag(final ComponentTag tag)	{
        super.onComponentTag(tag);
        tag.setType(TagType.OPEN);
    }

    // -- HELPER

    protected CharSequence extractHtmlOrElse(final Object modelObject, final String fallback) {

        if(modelObject==null) {
            return fallback;
        }

        if(modelObject instanceof ManagedObject) {

            final var adapter = (ManagedObject) modelObject;

            if(adapter.getPojo()==null) {
                return fallback;
            }

            final var asHtml = lookupObjectFeatureIn(getDefaultModel())
            .map(feature->adapter.titleString(conf->conf.feature(feature)))
            .orElseGet(adapter::titleString);

            if(asHtml != null) {
                return asHtml;
            }

            return fallback;
        }

        return modelObject.toString();
    }

    // -- HELPER

    protected Optional<ObjectFeature> lookupObjectFeatureIn(final IModel<?> model) {
        if(model instanceof ScalarPropertyModel) {
            return Optional.of(((ScalarPropertyModel)model).getMetaModel());
        }
        if(model instanceof ParameterUiModel) {
            return Optional.of(((ParameterUiModel)model).getMetaModel());
        }
        if(model instanceof ValueModel) {
            return Optional.ofNullable(((ValueModel)model).getActionModelHint())
                    .map(act->act.getMetaModel());
        }
        return Optional.empty();
    }

}

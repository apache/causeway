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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.metamodel.adapter.ObjectAdapter;

import lombok.val;

public class MarkupComponent extends WebComponent {

    private static final long serialVersionUID = 1L;

    private final LocalResourcePath observing;

    public MarkupComponent(final String id, IModel<?> model, LocalResourcePath observing){
        super(id, model);
        this.observing = observing;
    }

    @Override
    public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag){
        val htmlContent = extractHtmlOrElse(getDefaultModelObject(), "" /*fallback*/);
        replaceComponentTagBody(
                markupStream, 
                openTag, 

                observing!=null 
                ? MarkupComponent_observing.decorate(htmlContent, observing)
                        : htmlContent

                );
    }

    @Override
    protected void onComponentTag(ComponentTag tag)	{
        super.onComponentTag(tag);
        tag.setType(TagType.OPEN);
    }

    // -- HELPER

    protected static CharSequence extractHtmlOrElse(Object modelObject, final String fallback) {

        if(modelObject==null) {
            return fallback;
        }

        if(modelObject instanceof ObjectAdapter) {

            final ObjectAdapter objAdapter = (ObjectAdapter) modelObject;

            if(objAdapter.getPojo()==null)
                return fallback;

            final Object value = objAdapter.getPojo();

            if(!(value instanceof Markup))
                return fallback;

            return ((Markup)value).asString();
        }

        return modelObject.toString();

    }


}

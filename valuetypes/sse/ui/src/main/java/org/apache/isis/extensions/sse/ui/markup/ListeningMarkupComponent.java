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
package org.apache.isis.extensions.sse.ui.markup;

import javax.inject.Inject;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.core.config.viewer.wicket.WebAppContextPath;
import org.apache.isis.viewer.wicket.ui.components.scalars.markup.MarkupComponent;

import lombok.val;

public class ListeningMarkupComponent extends MarkupComponent {

    private static final long serialVersionUID = 1L;
    
    private final LocalResourcePath observing;
    @Inject
    private WebAppContextPath webAppContextPath;

    public ListeningMarkupComponent(final String id, IModel<?> model, LocalResourcePath observing){
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
                ? ListeningMarkupComponent_observing.decorate(htmlContent, observing, webAppContextPath)
                        : htmlContent

                );
    }

    
}

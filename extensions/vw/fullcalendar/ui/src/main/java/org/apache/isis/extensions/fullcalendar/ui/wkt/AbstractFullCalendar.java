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
package org.apache.isis.extensions.fullcalendar.ui.wkt;

import java.util.List;

import org.apache.wicket.ajax.WicketAjaxJQueryResourceReference;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import lombok.NonNull;

abstract class AbstractFullCalendar
extends WebMarkupContainer
implements IHeaderContributor {

    private static final long serialVersionUID = 1L;

    protected AbstractFullCalendar(@NonNull final String id) {
		super(id);
	}

    /**
     * This is the original CSS file from the FullCalendar.io project.
     */
	protected static final ResourceReference CSS = new PackageResourceReference(AbstractFullCalendar.class,
		"res/fullcalendar.min.css");

    /**
     * JS files for FullCalendar and the integration of it into <i>Wicket</i>.
     * The order of the files is important.
     * <p>
     * With the exception of 'fullcalendar.ext.js',
     * these are the original JavaScript files from the FullCalendar.io project (and related projects).
     * These can/should be updated when updating FullCalendar version.
     */
    private static final List<ResourceReference> JS_FILES = List.of(
            new PackageResourceReference(AbstractFullCalendar.class, "res/moment.min.js"),
            new PackageResourceReference(AbstractFullCalendar.class, "res/fullcalendar.js"),
            new PackageResourceReference(AbstractFullCalendar.class, "res/fullcalendar.ext.js"),
            new PackageResourceReference(AbstractFullCalendar.class, "res/locale-all.js")
    );


    /**
     * Renders the necessary Javascript files for FullCalendar.
     */
    @Override
    public void renderHead(@NonNull final IHeaderResponse response) {
        // jQuery
        response.render(JavaScriptHeaderItem.forReference(WicketAjaxJQueryResourceReference.get()));
        response.render(CssReferenceHeaderItem.forReference(CSS));
        for (ResourceReference jsFile : JS_FILES) {
            response.render(JavaScriptReferenceHeaderItem.forReference(jsFile));
        }
    }

	public final String toJson(final Object value) {
		return _Json.toJson(value);
	}
}

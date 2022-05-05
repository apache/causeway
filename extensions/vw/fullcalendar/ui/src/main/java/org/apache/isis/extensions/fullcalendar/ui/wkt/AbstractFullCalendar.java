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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.WicketAjaxJQueryResourceReference;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;

abstract class AbstractFullCalendar extends MarkupContainer implements IHeaderContributor {

    private static final long serialVersionUID = 1L;

    protected AbstractFullCalendar(final String id) {
		super(id);
	}

	// TODO see if it makes sense to switch these to Css/JavaScriptResourceReference
	protected static final ResourceReference CSS = new PackageResourceReference(AbstractFullCalendar.class,
		"res/fullcalendar.css");
	protected static final ResourceReference JS = new PackageResourceReference(AbstractFullCalendar.class,
		"res/fullcalendar.js");
	protected static final ResourceReference JS_EXT = new PackageResourceReference(AbstractFullCalendar.class,
		"res/fullcalendar.ext.js");
	protected static final ResourceReference JS_MIN = new PackageResourceReference(AbstractFullCalendar.class,
		"res/fullcalendar.min.js");
	protected static final UrlResourceReference LOADASH = new UrlResourceReference(
		Url.parse("https://cdn.jsdelivr.net/npm/lodash@4.17.21/lodash.min.js"));

	@Override
	public void renderHead(final IHeaderResponse response) {

		response.render(JavaScriptHeaderItem.forReference(WicketAjaxJQueryResourceReference.get()));

		renderCssResources(response);

		renderJavaScriptResources(response);
		response.render(JavaScriptReferenceHeaderItem.forReference(JS_EXT));

	}

	protected void renderJavaScriptResources(final IHeaderResponse response) {
		response.render(JavaScriptReferenceHeaderItem.forReference(LOADASH));
		if (getApplication().usesDeploymentConfig()) {
			response.render(JavaScriptReferenceHeaderItem.forReference(JS));
		} else {
			response.render(JavaScriptReferenceHeaderItem.forReference(JS));
		}
	}

	protected void renderCssResources(final IHeaderResponse response) {
		response.render(CssReferenceHeaderItem.forReference(CSS));
	}

	public final String toJson(final Object value) {
		return _Json.toJson(value);
	}
}

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
package org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;

import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.res.FullCalendarCssReference;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.res.FullCalendarIntegrationJsReference;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.res.FullCalendarJsReference;

abstract class AbstractFullCalendar extends MarkupContainer implements IHeaderContributor {
    private static final long serialVersionUID = 1L;

    public AbstractFullCalendar(final String id) {
		super(id);
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		response.render(FullCalendarCssReference.asHeaderItem());
		response.render(FullCalendarJsReference.asHeaderItem());
		response.render(FullCalendarIntegrationJsReference.asHeaderItem());
	}

	public final String toJson(final Object value) {
		return _Json.toJson(value);
	}
}

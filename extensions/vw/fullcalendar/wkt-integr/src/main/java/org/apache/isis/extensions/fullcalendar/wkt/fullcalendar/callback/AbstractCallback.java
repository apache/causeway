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

package org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback;

import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.FullCalendar;

abstract class AbstractCallback extends Behavior implements IRequestListener {
	private FullCalendar calendar;

	@Override
	public void bind(Component component) {
		super.bind(component);
		this.calendar = (FullCalendar) component;
	}

	protected final String getUrl(Map<String, Object> parameters) {
		PageParameters params = new PageParameters();
		String url = calendar.urlForListener(this, params).toString();

		if (parameters != null) {
			for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
				url += "&" + parameter.getKey() + "=" + parameter.getValue();
			}
		}
		return url;
	}

	@Override
	public final void onRequest() {
		respond();
	}

	protected abstract void respond();

	protected final FullCalendar getCalendar() {
		return calendar;
	}

	@Override
	public boolean getStatelessHint(Component component) {
		return false;
	}

	@Override
	public boolean rendersPage() {
		return false;
	}
}

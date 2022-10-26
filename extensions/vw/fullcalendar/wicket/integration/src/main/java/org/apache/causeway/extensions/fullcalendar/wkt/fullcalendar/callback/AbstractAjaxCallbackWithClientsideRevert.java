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
package org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback;

import java.util.UUID;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;

abstract class AbstractAjaxCallbackWithClientsideRevert extends AbstractAjaxCallback {

    private static final long serialVersionUID = 1L;

    private String uuid = "u" + UUID.randomUUID().toString().replace("-", "");

	protected abstract String getRevertScript();

	protected abstract boolean onEvent(AjaxRequestTarget target);

	private String getRevertScriptBlock() {
		return "{" + getRevertScript() + ";}";
	}

	@Override
	protected final void respond(final AjaxRequestTarget target) {
		boolean result = onEvent(target);
		target.prependJavaScript(String.format("$.data(document, '%s', %s);", uuid, String.valueOf(result)));
	}

	@Override
	protected void updateAjaxAttributes(final AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		AjaxCallListener listener = new AjaxCallListener() {
            private static final long serialVersionUID = 1L;

            @Override
			public CharSequence getSuccessHandler(final Component component) {
				return String.format("if (false===$.data(document, '%s')) %s $.removeData(document, '%s');", uuid,
					getRevertScriptBlock(), uuid);
			}

			@Override
			public CharSequence getFailureHandler(final Component component) {
				return getRevertScriptBlock();
			}
		};
		attributes.getAjaxCallListeners().add(listener);
	}

}

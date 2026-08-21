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
package org.apache.causeway.viewer.commons.model.webjar;

import java.util.Map;

import org.apache.causeway.commons.internal.base._StringInterpolation;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum WebJar {
	DATATABLES_CSS("datatables", "${id}/${version}/css/dataTables.dataTables.min.css"),
	DATATABLES_JS("datatables", "${id}/${version}/js/dataTables.min.js"),
	DATATABLES_BOOTSTRAP_CSS("datatables", "${id}/${version}/css/dataTables.bootstrap5.min.css"),
	DATATABLES_BOOTSTRAP_JS("datatables", "${id}/${version}/js/dataTables.bootstrap5.min.js"),
	FONT_AWESOME_CSS("font-awesome", "${id}/${version}/css/all.min.css"),
	FULL_CALENDAR_JS("fullcalendar", "${id}/${version}/index.global.min.js"),
	MOMENTJS_JS("momentjs", "${id}/${version}/min/moment.min.js"),
	PDFJS_CMAPS("pdfjs-dist", "${id}/${version}/cmaps/_.bcmap"),
	PDFJS_JS("pdfjs-dist", "${id}/${version}/build/pdf.min.mjs"),
	PDFJS_WORKER_JS("pdfjs-dist", "${id}/${version}/build/pdf.worker.min.mjs"),
	VEGA(null, null),
	VEGA_LITE(null, null),
	VEGA_EMBED(null, null);

	/**
	 * corresponds to the webjars path as provided by the maven artifact
	 * {@code org.webjars:font-awesome}
	 */
	private final String id;
	private final String resourceFormat;

	public String resource() {
		var version = WebjarEnumerator.lookupElseFail(id)
				.version();
		return new _StringInterpolation(Map.of(
				"id", id,
				"version", version))
			.applyTo(resourceFormat);
	}

}

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
package org.apache.causeway.valuetypes.vega.applib.builder;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._StringInterpolation;
import org.apache.causeway.valuetypes.vega.applib.value.Vega;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

public record VegaDonutDiagram(
		Settings settings,
		Iterable<DataPoint> dataPoints)
implements VegaBuilder {

	@Builder @Getter @Accessors(fluent = true)
	public static class Settings {
		@Builder.Default
		private final String description = "Donut Diagram";
		@Builder.Default
		private final int width = 160;
		@Builder.Default
		private final int height = 160;
	}

	public record DataPoint(
			String category,
			long value) {
		String toJson() {
			return """
                {"category": "%s", "value": %d}"""
					.formatted(category, value);
		}
	}

	public VegaDonutDiagram(
			final UnaryOperator<Settings.SettingsBuilder> settingsConfigurer,
			final List<DataPoint> dataList) {
		this(settingsConfigurer.apply(Settings.builder()).build(), dataList);
	}

	@Override
	public Vega build() {
		var json = new _StringInterpolation(Map.of(
				"schema", Vega.Schema.VEGA_LITE.value(),
                "description", settings.description(),
                "width", ""+settings.width(),
  			  	"height", ""+settings.height(),
                "values", _NullSafe.stream(dataPoints())
                    .map(DataPoint::toJson)
                    .collect(Collectors.joining(", "))
                ))
			.applyTo(TEMPLATE);
		return Vega.valueOf(json);
	}

	private static String TEMPLATE = """
		{
		  "$schema": "${schema}",
		  "description": "${description}",
		  "width": ${width},
		  "height": ${height},
		  "data": {
			"values": [
               ${values}
            ]
           },
           "encoding": {
		       "theta": {"field": "value", "type": "quantitative", "stack": true},
               "color": {"field": "category", "type": "nominal"}
           },
           "mark": {"type": "arc", "innerRadius": 50, "tooltip": true}
		}
		""";
}

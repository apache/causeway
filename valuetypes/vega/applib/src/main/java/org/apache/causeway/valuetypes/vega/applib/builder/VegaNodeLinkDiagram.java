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
import java.util.OptionalInt;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._StringInterpolation;
import org.apache.causeway.valuetypes.vega.applib.value.Vega;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

public record VegaNodeLinkDiagram(
		Settings settings,
		Iterable<DataPoint> dataPoints)
implements VegaBuilder {

	@Builder @Getter @Accessors(fluent = true)
	public static class Settings {
		@Builder.Default
		private final String description = "Node Link Diagram";
		@Builder.Default
		private final int width = 800;
		@Builder.Default
		private final int height = 600;
		@Builder.Default
		private final int fontSize = 10;
		@Builder.Default
		private final int textAngle = -45;
	}

	public record DataPoint(
			  int id,
			  String name,
			  OptionalInt parentId) {
		String toJson() {
			return """
				{ "id": %d, "name": "%s", "parent": %d}"""
					.formatted(id, name, parentId.isPresent()
							? parentId.getAsInt()
							: null);
		}
	}

	public VegaNodeLinkDiagram(
			final UnaryOperator<Settings.SettingsBuilder> settingsConfigurer,
			final List<DataPoint> dataList) {
		this(settingsConfigurer.apply(Settings.builder()).build(), dataList);
	}

	@Override
	public Vega build() {
		var json = new _StringInterpolation(Map.of(
				"schema", Vega.Schema.VEGA.value(),
                "description", settings.description(),
                "width", ""+settings.width(),
  			  	"height", ""+settings.height(),
  			    "fontSize", ""+settings.fontSize(),
			  	"textAngle", ""+settings.textAngle(),
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
		  "padding": 5,

		  "signals": [
		    {"name": "labels", "value": true},
		    {"name": "layout", "value": "tidy"},
		    {"name": "links", "value": "diagonal"},
		    {"name": "separation", "value": true}],

		  "data": [
		    {
		      "name": "tree",
	          "values": [
		        ${values}
		      ],
		      "transform": [
		        {
		          "type": "stratify",
		          "key": "id",
		          "parentKey": "parent"
		        },
		        {
		          "type": "tree",
		          "method": {"signal": "layout"},
		          "size": [{"signal": "height"}, {"signal": "width - 100"}],
		          "separation": {"signal": "separation"},
		          "as": ["y", "x", "depth", "children"]
		        }
		      ]
		    },
		    {
		      "name": "links",
		      "source": "tree",
		      "transform": [
		        { "type": "treelinks" },
		        {
		          "type": "linkpath",
		          "orient": "horizontal",
		          "shape": {"signal": "links"}
		        }
		      ]
		    }
		  ],

		  "scales": [
		    {
		      "name": "color",
		      "type": "linear",
		      "range": {"scheme": "magma"},
		      "domain": {"data": "tree", "field": "depth"},
		      "zero": true
		    }
		  ],

		  "marks": [
		    {
		      "type": "path",
		      "from": {"data": "links"},
		      "encode": {
		        "update": {
		          "path": {"field": "path"},
		          "stroke": {"value": "#ccc"}
		        }
		      }
		    },
		    {
		      "type": "symbol",
		      "from": {"data": "tree"},
		      "encode": {
		        "enter": {
		          "size": {"value": 100},
		          "stroke": {"value": "#fff"}
		        },
		        "update": {
		          "x": {"field": "x"},
		          "y": {"field": "y"},
		          "fill": {"scale": "color", "field": "depth"}
		        }
		      }
		    },
		    {
		      "type": "text",
		      "from": {"data": "tree"},
		      "encode": {
		        "enter": {
		          "text": {"field": "name"},
		          "fontSize": {"value": ${fontSize}},
		          "baseline": {"value": "middle"},
		          "angle": {"value": ${textAngle}}
		        },
		        "update": {
		          "x": {"field": "x"},
		          "y": {"field": "y"},
		          "dx": {"signal": "datum.children ? -7 : 7"},
		          "align": {"signal": "datum.children ? 'right' : 'left'"},
		          "opacity": {"signal": "labels ? 1 : 0"}
		        }
		      }
		    }
		  ]
		}
		""";
}

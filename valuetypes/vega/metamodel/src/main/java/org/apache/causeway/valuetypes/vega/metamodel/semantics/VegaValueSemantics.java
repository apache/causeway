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
package org.apache.causeway.valuetypes.vega.metamodel.semantics;

import java.util.UUID;

import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.valuetypes.vega.applib.CausewayModuleValVegaApplib;
import org.apache.causeway.valuetypes.vega.applib.value.Vega;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import jakarta.inject.Named;

@Component
@Named(CausewayModuleValVegaApplib.NAMESPACE + ".VegaValueSemantics")
public class VegaValueSemantics
extends ValueSemanticsAbstract<Vega>
implements
    DefaultsProvider<Vega>,
    Renderer<Vega>,
    Parser<Vega> {

    @Override
    public Class<Vega> getCorrespondingClass() {
        return Vega.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING;
    }

    // -- DEFAULTS

    @Override
    public Vega getDefaultValue() {
        return new Vega();
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Vega value) {
        return decomposeAsString(value, Vega::getJson, ()->null);
    }

    @Override
    public Vega compose(final ValueDecomposition decomposition) {
        return composeFromString(decomposition, Vega::valueOf, ()->null);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final ValueSemanticsProvider.Context context, final Vega vega) {
        return renderTitle(vega, Vega::toString);
    }

    @Override
    public String htmlPresentation(final ValueSemanticsProvider.Context context, final Vega vega) {
        return renderHtml(vega, this::asHtml);
    }

    /**
     * see usage examples at <a href="https://vega.github.io/vega/usage/">vega</a>
     * and <a href="https://vega.github.io/vega-lite/usage/embed.html">vega-lite</a>
     */
    private String asHtml(final @NonNull Vega vega) {
        var containerId = "vegaContainer" + UUID.randomUUID().toString();

        return switch (vega.getSchema()) {
		case VEGA -> {
			var htmlFragment = String.format("""
				<div id="%1$s"></div>
				<script type="text/javascript">
				document.addEventListener('DOMContentLoaded', (event) => {
				  var spec = %2$s;
				  var view = new vega.View(vega.parse(spec), {
				    renderer: '%3$s',
				    container: '#%1$s',
				    hover: %4$b
				  });
				  view.runAsync();
				});\
				</script>""",
                    containerId,
                    vega.getJson(),
                    "canvas", // renderer (canvas or svg)
                    true // enable hover processing
                    );
			yield htmlFragment;
		}
		case VEGA_LITE -> {
			var htmlFragment = String.format("""
				<div id="%1$s"></div>
				<script type="text/javascript">
				document.addEventListener('DOMContentLoaded', (event) => {
				  var spec = %2$s;
				  vegaEmbed('#%1$s', spec);\
				});\
				</script>""",
                    containerId,
                    vega.getJson());
			yield htmlFragment;
		}
		default -> "<!-- empty Vega (unsupported schema) -->";
		};
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final Vega vega) {
        return vega!=null ? vega.getJson() : null;
    }

    @Override
    public Vega parseTextRepresentation(final ValueSemanticsProvider.Context context, final String json) {
        return json!=null ? Vega.valueOf(json) : null;
    }

    @Override
    public int typicalLength() {
        return 0;
    }

    // -- EXAMPLES

    @Override
    public Can<Vega> getExamples() {
        return Can.of(
                Vega.valueOf("""
					{
					  "$schema": "https://vega.github.io/schema/vega-lite/v5.json",
					  "data": {
					    "values": [
					      {"a": "C", "b": 2},
					      {"a": "C", "b": 7},
					      {"a": "C", "b": 4},
					      {"a": "D", "b": 1},
					      {"a": "D", "b": 2},
					      {"a": "D", "b": 6},
					      {"a": "E", "b": 8},
					      {"a": "E", "b": 4},
					      {"a": "E", "b": 7}
					    ]
					  },
					  "mark": "point",
					  "encoding": {
					    "x": {"field": "a", "type": "nominal"},
					    "y": {"field": "b", "type": "quantitative"}
					  }
					}"""),
                Vega.valueOf("""
					{
					  "$schema": "https://vega.github.io/schema/vega/v5.json",
					  "description": "A basic bar chart example, with value labels shown upon mouse hover.",
					  "width": 400,
					  "height": 200,
					  "padding": 5,

					  "data": [
					    {
					      "name": "table",
					      "values": [
					        {"category": "A", "amount": 28},
					        {"category": "B", "amount": 55},
					        {"category": "C", "amount": 43},
					        {"category": "D", "amount": 91},
					        {"category": "E", "amount": 81},
					        {"category": "F", "amount": 53},
					        {"category": "G", "amount": 19},
					        {"category": "H", "amount": 87}
					      ]
					    }
					  ],

					  "signals": [
					    {
					      "name": "tooltip",
					      "value": {},
					      "on": [
					        {"events": "rect:mouseover", "update": "datum"},
					        {"events": "rect:mouseout",  "update": "{}"}
					      ]
					    }
					  ],

					  "scales": [
					    {
					      "name": "xscale",
					      "type": "band",
					      "domain": {"data": "table", "field": "category"},
					      "range": "width",
					      "padding": 0.05,
					      "round": true
					    },
					    {
					      "name": "yscale",
					      "domain": {"data": "table", "field": "amount"},
					      "nice": true,
					      "range": "height"
					    }
					  ],

					  "axes": [
					    { "orient": "bottom", "scale": "xscale" },
					    { "orient": "left", "scale": "yscale" }
					  ],

					  "marks": [
					    {
					      "type": "rect",
					      "from": {"data":"table"},
					      "encode": {
					        "enter": {
					          "x": {"scale": "xscale", "field": "category"},
					          "width": {"scale": "xscale", "band": 1},
					          "y": {"scale": "yscale", "field": "amount"},
					          "y2": {"scale": "yscale", "value": 0}
					        },
					        "update": {
					          "fill": {"value": "steelblue"}
					        },
					        "hover": {
					          "fill": {"value": "red"}
					        }
					      }
					    },
					    {
					      "type": "text",
					      "encode": {
					        "enter": {
					          "align": {"value": "center"},
					          "baseline": {"value": "bottom"},
					          "fill": {"value": "#333"}
					        },
					        "update": {
					          "x": {"scale": "xscale", "signal": "tooltip.category", "band": 0.5},
					          "y": {"scale": "yscale", "signal": "tooltip.amount", "offset": -2},
					          "text": {"signal": "tooltip.amount"},
					          "fillOpacity": [
					            {"test": "datum === tooltip", "value": 0},
					            {"value": 1}
					          ]
					        }
					      }
					    }
					  ]
					}"""));
    }

}

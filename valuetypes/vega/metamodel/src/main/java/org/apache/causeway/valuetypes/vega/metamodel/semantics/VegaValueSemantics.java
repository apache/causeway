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

import javax.inject.Named;

import org.springframework.stereotype.Component;

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

import lombok.NonNull;
import lombok.val;

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
        val containerId = "vegaContainer" + UUID.randomUUID().toString();

        switch (vega.getSchema()) {
        case VEGA: {
            val htmlFragment = String.format(""
                    + "<div id=\"%1$s\"></div>\n"
                    + "<script type=\"text/javascript\">\n"
                    + "document.addEventListener('DOMContentLoaded', (event) => {\n"
                    + "  var spec = %2$s;\n"
                    + "  var view = new vega.View(vega.parse(spec), {\n"
                    + "    renderer: '%3$s',\n"
                    + "    container: '#%1$s',\n"
                    + "    hover: %4$b\n"
                    + "  });\n"
                    + "  view.runAsync();\n"
                    + "});"
                    + "</script>",
                    containerId,
                    vega.getJson(),
                    "canvas", // renderer (canvas or svg)
                    true // enable hover processing
                    );
            return htmlFragment;
        }
        case VEGA_LITE: {
            val htmlFragment = String.format(""
                    + "<div id=\"%1$s\"></div>\n"
                    + "<script type=\"text/javascript\">\n"
                    + "document.addEventListener('DOMContentLoaded', (event) => {\n"
                    + "  var spec = %2$s;\n"
                    + "  vegaEmbed('#%1$s', spec);"
                    + "});"
                    + "</script>",
                    containerId,
                    vega.getJson());
            return htmlFragment;
        }
        default:
            return "<!-- empty Vega (unsupported schema) -->";
        }
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
                Vega.valueOf("{\n"
                        + "  \"$schema\": \"https://vega.github.io/schema/vega-lite/v5.json\",\n"
                        + "  \"data\": {\n"
                        + "    \"values\": [\n"
                        + "      {\"a\": \"C\", \"b\": 2},\n"
                        + "      {\"a\": \"C\", \"b\": 7},\n"
                        + "      {\"a\": \"C\", \"b\": 4},\n"
                        + "      {\"a\": \"D\", \"b\": 1},\n"
                        + "      {\"a\": \"D\", \"b\": 2},\n"
                        + "      {\"a\": \"D\", \"b\": 6},\n"
                        + "      {\"a\": \"E\", \"b\": 8},\n"
                        + "      {\"a\": \"E\", \"b\": 4},\n"
                        + "      {\"a\": \"E\", \"b\": 7}\n"
                        + "    ]\n"
                        + "  },\n"
                        + "  \"mark\": \"point\",\n"
                        + "  \"encoding\": {\n"
                        + "    \"x\": {\"field\": \"a\", \"type\": \"nominal\"},\n"
                        + "    \"y\": {\"field\": \"b\", \"type\": \"quantitative\"}\n"
                        + "  }\n"
                        + "}"),
                Vega.valueOf("{\n"
                        + "  \"$schema\": \"https://vega.github.io/schema/vega/v5.json\",\n"
                        + "  \"description\": \"A basic bar chart example, with value labels shown upon mouse hover.\",\n"
                        + "  \"width\": 400,\n"
                        + "  \"height\": 200,\n"
                        + "  \"padding\": 5,\n"
                        + "\n"
                        + "  \"data\": [\n"
                        + "    {\n"
                        + "      \"name\": \"table\",\n"
                        + "      \"values\": [\n"
                        + "        {\"category\": \"A\", \"amount\": 28},\n"
                        + "        {\"category\": \"B\", \"amount\": 55},\n"
                        + "        {\"category\": \"C\", \"amount\": 43},\n"
                        + "        {\"category\": \"D\", \"amount\": 91},\n"
                        + "        {\"category\": \"E\", \"amount\": 81},\n"
                        + "        {\"category\": \"F\", \"amount\": 53},\n"
                        + "        {\"category\": \"G\", \"amount\": 19},\n"
                        + "        {\"category\": \"H\", \"amount\": 87}\n"
                        + "      ]\n"
                        + "    }\n"
                        + "  ],\n"
                        + "\n"
                        + "  \"signals\": [\n"
                        + "    {\n"
                        + "      \"name\": \"tooltip\",\n"
                        + "      \"value\": {},\n"
                        + "      \"on\": [\n"
                        + "        {\"events\": \"rect:mouseover\", \"update\": \"datum\"},\n"
                        + "        {\"events\": \"rect:mouseout\",  \"update\": \"{}\"}\n"
                        + "      ]\n"
                        + "    }\n"
                        + "  ],\n"
                        + "\n"
                        + "  \"scales\": [\n"
                        + "    {\n"
                        + "      \"name\": \"xscale\",\n"
                        + "      \"type\": \"band\",\n"
                        + "      \"domain\": {\"data\": \"table\", \"field\": \"category\"},\n"
                        + "      \"range\": \"width\",\n"
                        + "      \"padding\": 0.05,\n"
                        + "      \"round\": true\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"name\": \"yscale\",\n"
                        + "      \"domain\": {\"data\": \"table\", \"field\": \"amount\"},\n"
                        + "      \"nice\": true,\n"
                        + "      \"range\": \"height\"\n"
                        + "    }\n"
                        + "  ],\n"
                        + "\n"
                        + "  \"axes\": [\n"
                        + "    { \"orient\": \"bottom\", \"scale\": \"xscale\" },\n"
                        + "    { \"orient\": \"left\", \"scale\": \"yscale\" }\n"
                        + "  ],\n"
                        + "\n"
                        + "  \"marks\": [\n"
                        + "    {\n"
                        + "      \"type\": \"rect\",\n"
                        + "      \"from\": {\"data\":\"table\"},\n"
                        + "      \"encode\": {\n"
                        + "        \"enter\": {\n"
                        + "          \"x\": {\"scale\": \"xscale\", \"field\": \"category\"},\n"
                        + "          \"width\": {\"scale\": \"xscale\", \"band\": 1},\n"
                        + "          \"y\": {\"scale\": \"yscale\", \"field\": \"amount\"},\n"
                        + "          \"y2\": {\"scale\": \"yscale\", \"value\": 0}\n"
                        + "        },\n"
                        + "        \"update\": {\n"
                        + "          \"fill\": {\"value\": \"steelblue\"}\n"
                        + "        },\n"
                        + "        \"hover\": {\n"
                        + "          \"fill\": {\"value\": \"red\"}\n"
                        + "        }\n"
                        + "      }\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"type\": \"text\",\n"
                        + "      \"encode\": {\n"
                        + "        \"enter\": {\n"
                        + "          \"align\": {\"value\": \"center\"},\n"
                        + "          \"baseline\": {\"value\": \"bottom\"},\n"
                        + "          \"fill\": {\"value\": \"#333\"}\n"
                        + "        },\n"
                        + "        \"update\": {\n"
                        + "          \"x\": {\"scale\": \"xscale\", \"signal\": \"tooltip.category\", \"band\": 0.5},\n"
                        + "          \"y\": {\"scale\": \"yscale\", \"signal\": \"tooltip.amount\", \"offset\": -2},\n"
                        + "          \"text\": {\"signal\": \"tooltip.amount\"},\n"
                        + "          \"fillOpacity\": [\n"
                        + "            {\"test\": \"datum === tooltip\", \"value\": 0},\n"
                        + "            {\"value\": 1}\n"
                        + "          ]\n"
                        + "        }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}"));
    }

}

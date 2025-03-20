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
package org.apache.causeway.valuetypes.asciidoc.builder.objgraph.plantuml;

import org.apache.causeway.applib.services.metamodel.objgraph.ObjectGraph;
import org.apache.causeway.commons.internal.base._Strings;

public class ObjectGraphRendererPlantuml implements ObjectGraph.Renderer {

    @Override
    public void render(final StringBuilder sb, final ObjectGraph objGraph) {

        renderProlog(sb);

        objGraph
            .objectsGroupedByPackage()
            .forEach((namespace, objects)->{
                // package start
                sb.append("package ").append(namespace).append(" {\n");

                objects.stream()
                    .map(this::render)
                    .forEach(s->sb.append(s).append('\n'));

                // package end
                sb.append("}\n");
            });

        objGraph.relations().stream()
            .map(this::render)
            .forEach(s->sb.append(s).append('\n'));
    }

    protected void renderProlog(final StringBuilder sb) {
        sb.append("left to right direction\n"); // arranges packages vertically
    }

    protected String render(final ObjectGraph.Object obj) {
        var sb = new StringBuilder()
                .append("object %s as %s".formatted(
                    doubleQuoted(obj.name()),
                    obj.stereotype()
                        .map(stp->"%s <<%s>>".formatted(obj.id(), stp)).orElse(obj.id())))
                .append('\n');

        obj.fields().forEach(field->{
                sb.append(obj.id() + " : " + render(field)).append('\n');
        });
        return sb.toString();
    }

    protected String render(final ObjectGraph.Field field) {
        return field.isPlural()
                ? "%s: [%s]".formatted(field.name(), field.elementTypeShortName())
                : "%s: %s".formatted(field.name(), field.elementTypeShortName());
    }

    protected String render(final ObjectGraph.Relation rel) {
        return switch(rel.relationType()) {
            case ONE_TO_ONE, ONE_TO_MANY, MERGED_ASSOCIATIONS -> "%s -> \"%s\" %s".formatted(
                    rel.fromId(),
                    rel.descriptionFormatted() /*NOTE: format has no effect if merged*/,
                    rel.toId());
            case BIDIR_ASSOCIATION -> "%s \"%s\" -- \"%s\" %s%s".formatted(
                    rel.fromId(),
                    rel.nearLabel(),
                    rel.farLabel(),
                    rel.toId(),
                    _Strings.nonEmpty(rel.description()/*NOTE: already formatted*/)
                        .map(" : %s"::formatted)
                        .orElse(""));
            case INHERITANCE -> "%s --|> %s".formatted(
                    rel.fromId(),
                    rel.toId());
        };
    }

    // -- HELPER

    private static String doubleQuoted(final String string) {
        return "\"" + string + "\"";
    }

}

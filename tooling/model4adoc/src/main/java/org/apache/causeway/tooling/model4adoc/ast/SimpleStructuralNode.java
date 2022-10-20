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
package org.apache.causeway.tooling.model4adoc.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.StructuralNode;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class SimpleStructuralNode extends ContentNodeAbstract implements StructuralNode {

    @Getter @Setter private String title;
    @Getter @Setter private String caption;
    @Getter @Setter private String style;
    @Getter private final List<StructuralNode> blocks = new ArrayList<>();
    @Getter @Setter private Object content;
    @Getter @Setter private int level;
    @Getter @Setter private String convert;

    @Override
    @Deprecated
    public String title() {
        return getTitle();
    }

    @Override
    @Deprecated
    public String style() {
        return getStyle();
    }

    @Override
    @Deprecated
    public List<StructuralNode> blocks() {
        return getBlocks();
    }

    @Override
    public void append(StructuralNode block) {
        getBlocks().add(block);
    }

    @Override
    @Deprecated
    public Object content() {
        return getContent();
    }

    @Override
    public String convert() {
        return getConvert();
    }

    @Override
    public Cursor getSourceLocation() {
        return null;
    }

    @Override
    public String getContentModel() {
        return null;
    }

    @Override
    public List<String> getSubstitutions() {
        return Collections.emptyList();
    }

    @Override
    public boolean isSubstitutionEnabled(String substitution) {
        return false;
    }

    @Override
    public void removeSubstitution(String substitution) {

    }

    @Override
    public void addSubstitution(String substitution) {

    }

    @Override
    public void prependSubstitution(String substitution) {

    }

    @Override
    public void setSubstitutions(String... substitutions) {
    }

    @Override
    public List<StructuralNode> findBy(Map<Object, Object> selector) {
        return Collections.emptyList();
    }


}

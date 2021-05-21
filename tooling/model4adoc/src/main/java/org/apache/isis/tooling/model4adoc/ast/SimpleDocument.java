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
package org.apache.isis.tooling.model4adoc.ast;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.Author;
import org.asciidoctor.ast.Catalog;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.RevisionInfo;
import org.asciidoctor.ast.Title;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
public class SimpleDocument extends SimpleStructuralNode implements Document {

    @Getter @Setter private Title structuredDoctitle;
    @Getter @Setter private String doctitle;
    @Getter private final Map<Object, Object> options = new HashMap<>();
    @Getter @Setter private boolean sourcemap;

    @Override
    public String doctitle() {
        return getDoctitle();
    }

    @Override
    public boolean isBasebackend(String backend) {
        return false;
    }

    @Override
    public boolean basebackend(String backend) {
        return false;
    }

    @Override
    public int getAndIncrementCounter(String name) {
        return 0;
    }

    @Override
    public int getAndIncrementCounter(String name, int initialValue) {
        return 0;
    }

    @Override
    public Catalog getCatalog() {
        return null;
    }

    @Override
    public List<Author> getAuthors() {
        return Collections.emptyList();
    }

    @Override
    public RevisionInfo getRevisionInfo() {
        return null;
    }


}

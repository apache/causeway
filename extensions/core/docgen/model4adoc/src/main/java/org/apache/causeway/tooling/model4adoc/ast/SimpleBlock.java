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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Block;

import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
public class SimpleBlock extends SimpleStructuralNode implements Block {

    @Getter private final List<String> lines = new ArrayList<>();

    @Override
    @Deprecated
    public List<String> lines() {
        return getLines();
    }

    @Override
    public void setLines(List<String> lines) {
        this.lines.clear();
        this.lines.addAll(lines);
    }

    @Override
    @Deprecated
    public String source() {
        return getSource();
    }

    @Override
    public String getSource() {
        return lines.stream().collect(Collectors.joining("\n"));
    }

    @Override
    public void setSource(String source) {
        if(source==null) {
            lines.clear();
            return;
        }
        setLines(Arrays.asList(source.replace("\r", "").split("\n")));
    }

    @Override
    public void setStyle(String style) {
        setAttribute("style", style, true);
        super.setStyle(style);
    }

    @Override
    public void setTitle(String title) {
        setAttribute("title", title, true);
        super.setTitle(title);
    }

}

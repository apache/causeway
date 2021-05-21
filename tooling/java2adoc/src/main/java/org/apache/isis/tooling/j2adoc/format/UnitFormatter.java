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
package org.apache.isis.tooling.j2adoc.format;

import java.io.File;
import java.util.function.BiFunction;
import org.asciidoctor.ast.Document;

import org.apache.isis.tooling.j2adoc.J2AdocUnit;

public interface UnitFormatter
extends BiFunction<J2AdocUnit, File, Document> {

    /**
     *
     * @param j2AdocUnit - the java AST to convert to asciidoc
     * @param file - the file that will be written to.  The intent is not for the formatter to write to this file, but it can be used to determine if include's of hook files are required.
     *
     * @return - the asciidoc representation of the java AST.
     */
    @Override
    Document apply(J2AdocUnit j2AdocUnit, File file);
}

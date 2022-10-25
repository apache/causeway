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
package org.apache.causeway.tooling.cli.test.adocfix;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.tooling.model4adoc.include.IncludeStatements;

import lombok.NonNull;
import lombok.val;

class IncludeStatementFixerTest {

    @Test @Disabled("to reinstate after changing to refguide format")
    void adocDocMining() throws IOException {

        val adocFiles = ProjectSampler.adocFiles(ProjectSampler.apacheCausewayRoot());

        val names = _Sets.<String>newTreeSet();

        Can.ofCollection(adocFiles)
        .stream()
        .filter(source->
            !source.toString().contains("\\system\\generated\\")
            && !source.toString().contains("/system/generated/"))

        //.filter(source->source.toString().contains("XmlSnapshotService"))
        .forEach(file->parseAdoc(file, names::add));

        names.forEach(System.out::println);
    }

    private void parseAdoc(final @NonNull File file, Consumer<String> onName) {
        val lines = _Text.readLinesFromFile(file, StandardCharsets.UTF_8);

        IncludeStatements.find(lines)
        .filter(include->!include.isLocal()
                && "system".equals(include.getComponent())
                && "generated".equals(include.getModule()))
        .forEach(include->{
            onName.accept(include.toString());
        });
    }

}

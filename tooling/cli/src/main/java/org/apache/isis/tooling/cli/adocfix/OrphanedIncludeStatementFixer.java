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
package org.apache.isis.tooling.cli.adocfix;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.SortedSet;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.tooling.cli.CliConfig;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.model4adoc.include.IncludeStatement;
import org.apache.isis.tooling.model4adoc.include.IncludeStatements;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class OrphanedIncludeStatementFixer {

    public static void fixIncludeStatements(
            final @NonNull SortedSet<File> adocFiles,
            final @NonNull CliConfig cliConfig,
            final @NonNull J2AdocContext j2aContext) {

        if(cliConfig.getCommands().getIndex().isDryRun()) {
            log.debug("IncludeStatementFixer: skip (dry-run)");
            return;
        }

        if(!cliConfig.getCommands().getIndex().isFixOrphanedAdocIncludeStatements()) {
            log.debug("IncludeStatementFixer: skip (disabled via config, fixOrphandedAdocIncludeStatements=false)");
            return;
        }

        log.debug("IncludeStatementFixer: about to process {} adoc files", adocFiles.size());

        val totalFixed = _Refs.intRef(0);

        adocFiles.forEach(adocFile->{
            //_Probe.errOut("adoc file found: %s", adocFile);

            val fixedCounter = _Refs.intRef(0);
            val originLines = _Text.readLinesFromFile(adocFile, StandardCharsets.UTF_8);

            val lines = IncludeStatements.rewrite(originLines, include->{
                final boolean inGlobalIndex =
                        "refguide".equals(include.getComponent())               // TODO should be reasoned from config
                        && include.getNamespace().startsWith(Can.of("index"));  // TODO should be reasoned from config
                if(include.isLocal() || !inGlobalIndex) {
                    return null; // keep original line, don't mangle
                }

                val correctedIncludeStatement = _Refs.<IncludeStatement>objectRef(null);
                val typeSimpleName = include.getCanonicalName();

                j2aContext.findUnitByTypeSimpleName(typeSimpleName)
                .ifPresent(unit->{

                    val module = unit.getNamespace().stream()
                            .skip(j2aContext.getNamespacePartsSkipCount())
                            .findFirst().get();
                    val expected = IncludeStatement.builder()
                    .component("refguide")
                    .module(module)
                    .type("page")
                    .namespace(unit.getNamespace().stream()
                            .skip(j2aContext.getNamespacePartsSkipCount() + 1) // +1 because is part of the module
                            .collect(Can.toCan())
                            .add(0, "index") // TODO this is antora config specific
                            )
                    .canonicalName(typeSimpleName)
                    .ext(".adoc")
                    .options("[leveloffset=+2]")
                    .build();

                    val includeLineShouldBe = expected.toAdocAsString();

                    if(!includeLineShouldBe.equals(include.getMatchingLine())) {
                        log.warn("mismatch\n {}\n {}\n", includeLineShouldBe, include.getMatchingLine());
                        correctedIncludeStatement.setValue(expected);
                        fixedCounter.incAndGet();
                    }

                });

                return correctedIncludeStatement
                        .getValue()
                        .orElse(null); // keep original line, don't mangle

            });

            if(fixedCounter.getValue()>0) {

                // write lines to file
                _Text.writeLinesToFile(lines, adocFile, StandardCharsets.UTF_8);

                totalFixed.update(n->n + fixedCounter.getValue());
            }

        });

        log.debug("IncludeStatementFixer: all done. ({} orphanded inlcudes fixed)", totalFixed.getValue());

    }

    // -- HELPER


}

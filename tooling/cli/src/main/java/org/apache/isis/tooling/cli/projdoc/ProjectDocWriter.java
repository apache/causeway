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
package org.apache.isis.tooling.cli.projdoc;

import java.io.File;
import java.util.function.BiConsumer;

import org.asciidoctor.ast.Document;

import org.apache.isis.commons.internal.base._Files;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.tooling.cli.CliConfig;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;
import org.apache.isis.tooling.model4adoc.AsciiDocWriter;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

final class ProjectDocWriter {

    @SneakyThrows
    static void write(
            final @NonNull CliConfig cliConfig,
            final @NonNull Document systemSummaryAdoc,
            final @NonNull J2AdocContext j2aContext,
            final @NonNull ProjectDocModel.Mode mode) {

        final BiConsumer<Document, File> docWriter = cliConfig.getGlobal().isDryRun()
                ? (doc, file)->AsciiDocWriter.print(doc) // print to system out only (dry run)
                : AsciiDocWriter::writeToFile;

        val currentUnit = _Refs.<J2AdocUnit>objectRef(null);
        val global = cliConfig.getGlobal();
        val overview = cliConfig.getCommands().getOverview();
        val index = cliConfig.getCommands().getIndex();

        //val rootFolder = global.getOutputRootFolder();
        val pagesFolder = global.getDocumentPagesFolder();

        val deleteCount = _Refs.intRef(0);
        int writeCount = 0;

        try {

            if (mode.includeOverview()) {

                // write system overview
                val overviewFile = new File(pagesFolder, overview.getSystemOverviewFilename());
                System.out.printf("writing system overview: %s%n", overviewFile.getName());
                docWriter.accept(systemSummaryAdoc, overviewFile);
                ++writeCount;
            }

            if(mode.includeIndex()) {

                // delete all generated documents in the index
                _Files.searchFiles(pagesFolder, dir->true, file-> {
                    val fileName = file.getName();
                    return fileName.endsWith(".adoc") &&
                           !fileName.equals(overview.getSystemOverviewFilename());
                })
                .stream()
                .peek(adocFile->System.out.printf("deleting file: %s%n", adocFile.getName()))
                .peek(__->deleteCount.inc())
                .forEach(_Files::deleteFile);


                // write document index
                for(val unit : j2aContext.getUnitIndex().values()) {

                    currentUnit.setValue(unit);

                    val adocIndexFile = adocDestinationFileForUnit(unit, global, overview, index);

                    System.out.printf("writing file: %s%n", adocIndexFile.getName());

                    docWriter.accept(
                            unit.toAsciiDoc(j2aContext),
                            adocIndexFile);

                    ++writeCount;
                }

                // summary
                System.out.printf(
                        "ProjectDocWriter: all done. (deleted: %d, written: %d)%n",
                        deleteCount.getValue(), writeCount);
            }

        } catch (Exception e) {
            System.err.printf(
                    "failed to write adoc for unit %s%n",
                    currentUnit.getValue().map(J2AdocUnit::getCanonicalName).orElse("none"));
            e.printStackTrace();
            System.exit(1);
        }
    }

    // generate output file based on unit's namespace and unit's name
    private static File adocDestinationFileForUnit(
            final @NonNull J2AdocUnit unit,
            final @NonNull CliConfig.Global global,
            final @NonNull CliConfig.Commands.Overview overview,
            final @NonNull CliConfig.Commands.Index index
            ) {

        val indexFolder = index.getDocumentIndexFolder(global.getOutputRootFolder());

        val destFolderBuilder = _Refs.<File>objectRef(indexFolder);

        unit.getNamespace().stream()
        .skip(global.getNamespacePartsSkipCount())
        .forEach(subDir->destFolderBuilder.update(currentDir->new File(currentDir, subDir)));

        val destFolder = destFolderBuilder.getValueElseDefault(indexFolder);
        destFolder.mkdirs();

        return new File(
                destFolder,
                unit.getCanonicalName()+ ".adoc");

    }


}

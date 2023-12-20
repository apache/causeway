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
 *
 */

package org.apache.causeway.tooling.metaprog.demoshowcases.value;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.commons.io.FileUtils;
import org.apache.causeway.commons.io.TextUtils;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@SuppressWarnings("unused")
@Log4j2
class ValueTypeGenTemplateTest {

    @ParameterizedTest
    @EnumSource(ValueShowCase.class)
    void test(final ValueShowCase valueShowCase) {
        testShowcase(valueShowCase.getConfigBuilder()
                .outputRootDir(outputDir(valueShowCase.name().toLowerCase()))
                .build());
    }

    // -- HELPER

    @SneakyThrows
    void testShowcase(final ValueTypeGenTemplate.Config config) {

        var frameWorkRoot = new File(".").getAbsoluteFile().getParentFile().getParentFile().getParentFile();

        // hack for Dan's PC; write out to a different git worktree
        frameWorkRoot = new File(frameWorkRoot.getParentFile(), "demo");

        val demoDomainRoot = new File(frameWorkRoot, "examples/demo/domain/src/main/java");
        val demoDomainShowCase = new File(demoDomainRoot, config.getJavaPackage().replace('.', '/'));

        // list reference source files
        val refShowcaseFiles = FileUtils.searchFiles(demoDomainShowCase, _Predicates.alwaysTrue(), file->
                  file.getName().endsWith(".java")
                  || file.getName().endsWith(".xml")
                  || file.getName().endsWith(".adoc")
              );

        val generator = new ValueTypeGenTemplate(config);

        val generatedFiles = _Sets.<File>newLinkedHashSet();
        generator.generate(generatedFiles::add);

        // override origin
        copyFiles(generatedFiles, config.getOutputRootDir(), demoDomainShowCase);
        copyMissingFiles(generatedFiles, config.getOutputRootDir(), demoDomainShowCase);

//        assertFileSetEquals(refShowcaseFiles, demoDomainShowCase, generatedFiles, config.getOutputRootDir());
//        assertFileContentEquals(refShowcaseFiles, generatedFiles);

    }

    static boolean PERSIST = true;
    static File outputRootDir;

    @BeforeAll
    static void setup() {
        outputRootDir = PERSIST
                ? FileUtils.makeDir(new File("C:/tmp/valueTypes"))
                : FileUtils.tempDir("causeway-tooling-showcases");

        log.info("tmp dir created in {}", outputRootDir);
    }

    private File outputDir(final String subfolder) {
        return new File(outputRootDir, subfolder);
    }

    /* not used
    private void assertFileSetEquals(
            final Set<File> setA, final File rootA,
            final Set<File> setB, final File rootB) {
        assertEquals(
                Can.ofCollection(setA)
                .map(FileUtils.realtiveFileName(rootA))
                .sorted(Comparator.naturalOrder()),
                Can.ofCollection(setB)
                .map(FileUtils.realtiveFileName(rootB))
                .sorted(Comparator.naturalOrder()));
    }*/


    private void copyFiles(final Collection<File> generatedFiles, final File sourceRoot, final File destinationRoot) {
        generatedFiles.forEach(src->{
            val dest = new File(destinationRoot, FileUtils.realtiveFileName(sourceRoot, src));
            FileUtils.makeDir(dest.getParentFile());
            copyWithCrlf(src, dest);
//            FileUtils.copy(src, dest);
        });
    }

    private void copyMissingFiles(final Collection<File> generatedFiles, final File sourceRoot, final File destinationRoot) {
        generatedFiles.forEach(src->{
            val dest = new File(destinationRoot, FileUtils.realtiveFileName(sourceRoot, src));
            if(!dest.exists()) {
                FileUtils.makeDir(dest.getParentFile());
                copyWithCrlf(src, dest);
//                FileUtils.copy(src, dest);
            }
        });
    }


    private void assertFileContentEquals(final Collection<File> filesA, final Collection<File> filesB) {

        val sortedA = Can.ofCollection(filesA).sorted(Comparator.naturalOrder());
        val sortedB = Can.ofCollection(filesB).sorted(Comparator.naturalOrder());

        val failedFileComparisons = sortedA
                .zipMap(sortedB, (a, b)->{

                    var linesA = _Text.normalize(
                            TextUtils.readLinesFromFile(a, StandardCharsets.UTF_8))
                            .map(this::normalizeForComparision);
                    var linesB = _Text.normalize(
                            TextUtils.readLinesFromFile(b, StandardCharsets.UTF_8))
                            .map(this::normalizeForComparision);

                    return Objects.equals(linesA, linesB)
                            ? null
                            : String.format("non equal line in file %s: %s",
                                    a.getName(),
                                    firstLineNotEqual(linesA, linesB));
                });

        failedFileComparisons.forEach(msg->System.err.printf("%s%n", msg));

        if(failedFileComparisons.isNotEmpty()) {
            fail(String.format("some file contents are not equal %s", failedFileComparisons));
        }
    }

    private String normalizeForComparision(final String line) {
        return line;//.replace("java.lang.Long", "Long");
    }

    private String firstLineNotEqual(final Can<String> linesA, final Can<String> linesB) {
        int lineIndex = 0;
        for(val lineA : linesA) {
            val lineB = linesB.get(lineIndex++).orElse(null);
            if(!lineA.equals(lineB)) {
                return String.format("%s <-> %s", lineA, lineB);
            }
        }
        return "";
    }

    private void copyWithCrlf(final @NonNull File from, final @NonNull File to) {
        // Appends CR before the LF line ending to each line
        FileUtils.copyLines(from, to, StandardCharsets.UTF_8, line->line + "\r");
    }

}

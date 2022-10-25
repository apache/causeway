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
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Files;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.functions._Predicates;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

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

        val frameWorkRoot = new File(".").getAbsoluteFile().getParentFile().getParentFile().getParentFile();
        val demoDomainRoot = new File(frameWorkRoot, "examples/demo/domain/src/main/java");
        val demoDomainShowCase = new File(demoDomainRoot, config.getJavaPackage().replace('.', '/'));

        // list reference source files
        val refShowcaseFiles = _Files.searchFiles(demoDomainShowCase, _Predicates.alwaysTrue(), file->
                  file.getName().endsWith(".java")
                  || file.getName().endsWith(".xml")
                  || file.getName().endsWith(".adoc")
              );

        val generator = new ValueTypeGenTemplate(config);

        val generatedFiles = _Sets.<File>newLinkedHashSet();
        generator.generate(generatedFiles::add);

        // override origin
        //copyFiles(generatedFiles, config.getOutputRootDir(), demoDomainShowCase);
        //copyMissingFiles(generatedFiles, config.getOutputRootDir(), demoDomainShowCase);

        assertFileSetEquals(refShowcaseFiles, demoDomainShowCase, generatedFiles, config.getOutputRootDir());
        assertFileContentEquals(refShowcaseFiles, generatedFiles);

    }

    static boolean PERSIST = true;
    static File outputRootDir;

    @BeforeAll
    static void setup() {
        outputRootDir = PERSIST
                ? _Files.makeDir(new File("D:/tmp/valueTypes"))
                : _Files.tempDir("casueway-tooling-showcases");

        log.info("tmp dir created in {}", outputRootDir);
    }

    private File outputDir(final String subfolder) {
        return new File(outputRootDir, subfolder);
    }

    private void assertFileSetEquals(
            final Set<File> setA, final File rootA,
            final Set<File> setB, final File rootB) {
        assertEquals(
                Can.ofCollection(setA)
                .map(_Files.realtiveFileName(rootA))
                .sorted(Comparator.naturalOrder()),
                Can.ofCollection(setB)
                .map(_Files.realtiveFileName(rootB))
                .sorted(Comparator.naturalOrder()));
    }


    @SuppressWarnings("unused")
    private void copyFiles(final Collection<File> generatedFiles, final File sourceRoot, final File destinationRoot) {
        generatedFiles.forEach(src->{
            val dest = new File(destinationRoot, _Files.realtiveFileName(sourceRoot, src));
            _Files.makeDir(dest.getParentFile());
            _Files.copy(src, dest);
        });
    }

    @SuppressWarnings("unused")
    private void copyMissingFiles(final Collection<File> generatedFiles, final File sourceRoot, final File destinationRoot) {
        generatedFiles.forEach(src->{
            val dest = new File(destinationRoot, _Files.realtiveFileName(sourceRoot, src));
            if(!dest.exists()) {
                _Files.makeDir(dest.getParentFile());
                _Files.copy(src, dest);
            }
        });
    }


    private void assertFileContentEquals(final Collection<File> filesA, final Collection<File> filesB) {

        val sortedA = Can.ofCollection(filesA).sorted(Comparator.naturalOrder());
        val sortedB = Can.ofCollection(filesB).sorted(Comparator.naturalOrder());

        val failedFileComparisons = sortedA
                .zipMap(sortedB, (a, b)->{

                    var linesA = _Text.normalize(
                            _Text.readLinesFromFile(a, StandardCharsets.UTF_8))
                            .map(this::normalizeForComparision);
                    var linesB = _Text.normalize(
                            _Text.readLinesFromFile(b, StandardCharsets.UTF_8))
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

}

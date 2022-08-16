package org.apache.isis.tooling.metaprog.demoshowcases.value;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Files;
import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.tooling.metaprog.demoshowcases.value.ValueTypeGenTemplate.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
class ValueTypeGenTemplateTest {

    @Test
    void test() throws IOException {

        val frameWorkRoot = new File(".").getAbsoluteFile().getParentFile().getParentFile().getParentFile();
        val demoDomainRoot = new File(frameWorkRoot, "examples/demo/domain/src/main/java");
        val uuidDemoDomain = new File(demoDomainRoot, "demoapp/dom/types/javautil/uuids");

        // list UUID source files
        val uuidShowcaseFiles = _Files.searchFiles(uuidDemoDomain, _Predicates.alwaysTrue(), file->
                  file.getName().endsWith(".java")
                  || file.getName().endsWith(".xml")
                  || file.getName().endsWith(".adoc")
              );

        val outputRootDir = new File("D:/tmp");
                //_Files.tempDir("isis-tooling-showcases");
        log.info("tmp dir created in {}", outputRootDir);

        val generator = new ValueTypeGenTemplate(Config.builder()
                .outputRootDir(outputRootDir)
                .showcaseName("JavaUtilUuid")
                .javaPackage("demoapp.dom.types.javautil.uuids")
                .showcaseValueType("java.util.UUID")
                .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.UUIDValueSemantics")
                .suppressGeneratedFileNotice(true)
                .build());

        val generatedFiles = _Sets.<File>newLinkedHashSet();
        generator.generate(generatedFiles::add);

        assertFileSetEquals(uuidShowcaseFiles, uuidDemoDomain, generatedFiles, outputRootDir);
        assertFileContentEquals(uuidShowcaseFiles, generatedFiles);

    }

    // -- HELPER

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


    private void assertFileContentEquals(final Collection<File> filesA, final Collection<File> filesB) {

        val sortedA = Can.ofCollection(filesA).sorted(Comparator.naturalOrder());
        val sortedB = Can.ofCollection(filesB).sorted(Comparator.naturalOrder());

        val failedFileComparisons = sortedA
                .zipMap(sortedB, (a, b)->{

                    var linesA = _Text.normalize(
                            _Text.readLinesFromFile(a, StandardCharsets.UTF_8));
                    var linesB = _Text.normalize(
                            _Text.readLinesFromFile(b, StandardCharsets.UTF_8));

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

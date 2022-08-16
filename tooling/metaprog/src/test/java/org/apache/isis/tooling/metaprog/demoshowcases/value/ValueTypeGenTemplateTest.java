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

        val outputRootDir = _Files.tempDir("isis-tooling-showcases");
        log.info("tmp dir created in {}", outputRootDir);

        val generator = new ValueTypeGenTemplate(Config.builder()
                .outputRootDir(outputRootDir)
                .showcaseName("JavaUtilUuid")
                .javaPackage("demoapp.dom.types.javautil.uuids")
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

        val equalsVector = sortedA.zipMap(sortedB, (a, b)->{

            return Objects.equals(
                    _Text.readLinesFromFile(a, StandardCharsets.UTF_8),
                    _Text.readLinesFromFile(b, StandardCharsets.UTF_8)
                    );
        });

        for(var flag: equalsVector) {
            if(!flag) {
                fail(String.format("some file contents are not equal %s", equalsVector));
            }
        }


    }

}

package org.apache.isis.tooling.metaprog.demoshowcases.value;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import org.junit.jupiter.api.Test;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Files;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.tooling.metaprog.demoshowcases.value.ValueTypeGenTemplate.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        val config = Config.builder()
                .outputRootDir(outputRootDir)
                .showcaseName("JavaUtilUuid")
                .build();

        val generatedFiles = _Sets.<File>newLinkedHashSet();

        new ValueTypeGenTemplate().generate(config, generatedFiles::add);

        assertEquals(
                Can.ofCollection(uuidShowcaseFiles)
                .map(_Files.realtiveFileName(uuidDemoDomain))
                .sorted(Comparator.naturalOrder()),
                Can.ofCollection(generatedFiles)
                .map(_Files.realtiveFileName(outputRootDir))
                .sorted(Comparator.naturalOrder()));

    }

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */
package org.apache.causeway.regressiontests.referenceapp.domain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorpusIntegrityTest {

    private static final List<String> COPIED_ROOTS = List.of(
            "domain/src/main/java",
            "domain/src/main/resources",
            "support-jpa/src/main/java/demoapp",
            "support-jpa/src/main/resources/config");

    @Test
    void copiedCorpusMatchesPinnedManifest() throws Exception {
        final Path corpusRoot = findCorpusRoot();
        final Map<String, String> expected = readManifest(corpusRoot.resolve("source-manifest.sha256"));
        final Set<String> actual = copiedFiles(corpusRoot);

        assertThat(actual).containsExactlyElementsOf(expected.keySet());
        for (final var entry : expected.entrySet()) {
            assertThat(sha256(corpusRoot.resolve(entry.getKey())))
                    .as("checksum of %s", entry.getKey())
                    .isEqualTo(entry.getValue());
        }
        assertThat(Files.readString(corpusRoot.resolve("corpus-paths.json")))
                .contains("29b43bfe4f77d525fb345394e5a52bd7d85a91ba")
                .contains("https://github.com/apache/causeway-app-referenceapp");
        assertThat(corpusRoot.resolve("CREDITS.TXT")).isRegularFile();
    }

    private static Path findCorpusRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("source-manifest.sha256"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate Reference Application corpus root from " + System.getProperty("user.dir"));
    }

    private static Map<String, String> readManifest(final Path manifest) throws IOException {
        final Map<String, String> entries = new LinkedHashMap<>();
        for (final String line : Files.readAllLines(manifest, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            final String[] parts = line.split("  ", 2);
            assertThat(parts).as("manifest line %s", line).hasSize(2);
            assertThat(entries.put(parts[1], parts[0])).as("duplicate path %s", parts[1]).isNull();
        }
        return entries;
    }

    private static Set<String> copiedFiles(final Path corpusRoot) throws IOException {
        final Set<String> paths = new LinkedHashSet<>();
        for (final String rootName : COPIED_ROOTS) {
            final Path root = corpusRoot.resolve(rootName);
            try (Stream<Path> stream = Files.walk(root)) {
                paths.addAll(stream
                        .filter(Files::isRegularFile)
                        .map(path -> corpusRoot.relativize(path).toString().replace('\\', '/'))
                        .sorted()
                        .collect(Collectors.toList()));
            }
        }
        return paths;
    }

    private static String sha256(final Path path) throws IOException, NoSuchAlgorithmException {
        final byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
        final StringBuilder result = new StringBuilder(digest.length * 2);
        for (final byte value : digest) {
            result.append("%02x".formatted(value & 0xff));
        }
        return result.toString();
    }
}

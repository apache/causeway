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
package org.apache.isis.tooling.cli;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Optional;

import org.yaml.snakeyaml.constructor.ConstructorException;

import org.apache.isis.commons.internal.resources._Yaml;
import org.apache.isis.tooling.j2adoc.format.UnitFormatter;
import org.apache.isis.tooling.j2adoc.format.UnitFormatterCompact;
import org.apache.isis.tooling.j2adoc.format.UnitFormatterWithSourceAndCallouts;
import org.apache.isis.tooling.j2adoc.format.UnitFormatterWithSourceAndSections;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@Data
public class CliConfig {

    private Global global = new Global();

    @Data
    public static class Global {

        private String licenseHeader =
                "Licensed to the Apache Software Foundation (ASF) under one or more contributor license "
                        + "agreements. See the NOTICE file distributed with this work for additional information regarding "
                        + "copyright ownership. The ASF licenses this file to you under the Apache License, "
                        + "Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. "
                        + "You may obtain a copy of the License at. http://www.apache.org/licenses/LICENSE-2.0 . "
                        + "Unless required by applicable law or agreed to in writing, software distributed under the License "
                        + "is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR  CONDITIONS OF ANY KIND, either express "
                        + "or implied. See the License for the specific language governing permissions and limitations under "
                        + "the License.";

    }

    private Commands commands = new Commands();

    @Data
    public static class Commands {

        private Overview overview = new Overview();

        @Data
        public static class Overview {

            private File rootFolder = null; // where to write to (overridden by -r flag)

            private String pagesPath = "modules/_overview/pages";

            private String systemOverviewFilename = "about.adoc";

            private String description = "These tables summarize all Maven artifacts available with this project.";

            public boolean isDryRun() {
                return getRootFolder() == null;
            }

            private LinkedHashMap<String, String> sections = new LinkedHashMap<>();

            public File getPagesFolder() {
                return Optional.ofNullable(getRootFolder())
                        .map(root->new File(root, getPagesPath()))
                        .orElse(null);
            }

        }


        private Index index = new Index();

        @Data
        public static class Index {

            private File rootFolder = null; // where to write to (overridden by -o flag)

            private String documentGlobalIndexXrefPageIdFormat = "refguide:%s:index/%s.adoc";

            // when 3 eg. skips first three parts of the package names 'org.apache.isis'
            private int namespacePartsSkipCount = 0;

            public boolean isDryRun() {
                return getRootFolder() == null;
            }

            private boolean fixOrphanedAdocIncludeStatements = false;
            private boolean skipTitleHeader = false;

            public enum Formatter {
                COMPACT(UnitFormatterCompact.class),
                JAVA_SOURCES_WITH_CALLOUTS(UnitFormatterWithSourceAndCallouts.class),
                JAVA_SOURCES_WITH_SECTIONS(UnitFormatterWithSourceAndSections.class),
                ;

                @Getter
                private final Class<? extends UnitFormatter> unitFormatterClass;
                Formatter(Class<? extends UnitFormatter> unitFormatterClass) {
                    this.unitFormatterClass = unitFormatterClass;
                }
            }

            private Formatter formatter = Formatter.JAVA_SOURCES_WITH_SECTIONS;

        }

    }


    // -- LOADING

    public static CliConfig read(final @NonNull File file) {
        return _Yaml.readYaml(CliConfig.class, file)
        .ifFailure(e->{
            if(e instanceof ConstructorException) {
                final ConstructorException ce = (ConstructorException) e;
                throw new RuntimeException(String.format("config file '%s' not readable\n%s", file.getAbsolutePath(), ce.getProblem()));
            } else {
                throw new RuntimeException(String.format("config file '%s' not readable\n%s", file.getAbsolutePath(), e));
            }
        })
        .presentElseGet(CliConfig::new);
    }


}

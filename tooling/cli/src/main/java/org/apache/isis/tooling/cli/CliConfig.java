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
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Optional;

import org.apache.isis.commons.internal.resources._Yaml;

import lombok.Data;
import lombok.NonNull;

@Data
public class CliConfig {
    
    private ProjectDoc projectDoc = new ProjectDoc();
        
    @Data
    public static class ProjectDoc {
        private String description = "These tables summarize all Maven artifacts available with this project.";
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
        private LinkedHashMap<String, String> artifactGroups = new LinkedHashMap<>();
        
        private File outputRootFolder = null; // where to write eg. system-overview.adoc
        
        private String documentPagesPath = "pages";
        private String documentGlobalIndexPath = "pages/index";
        private String documentGlobalIndexXrefPageIdFormat = "system:generated:index/%s.adoc";
        
        private String systemOverviewFilename = "system-overview.adoc";

        // when 3 eg. skips first three parts of the package names 'org.apache.isis'
        private int namespacePartsSkipCount = 3;
        
        public boolean isDryRun() {
            return getOutputRootFolder() == null;
        }
        
        public File getDocumentIndexFolder() {
            return Optional.ofNullable(getOutputRootFolder())
                    .map(root->new File(root, getDocumentGlobalIndexPath()))
                    .orElse(null);
        }
        
        public File getDocumentPagesFolder() {
            return Optional.ofNullable(getOutputRootFolder())
                    .map(root->new File(root, getDocumentPagesPath()))
                    .orElse(null);
        }
        
    }
    
    // -- LOADING

    public static CliConfig read(final @NonNull File file) {
        if(!file.canRead()) {
            System.err.println(String.format("config file '%s' not readable, using defaults", file.getAbsolutePath()));
            return new CliConfig();
        }
        try {
            return _Yaml.readYaml(CliConfig.class, new FileInputStream(file));
        } catch (Exception e) {
            System.err.println(String.format("config file '%s' not readable, using defaults", file.getAbsolutePath()));
            return new CliConfig();
        }
    }


}

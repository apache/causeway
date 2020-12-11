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
    private File outputFile = null; // where to write eg. system-overview.adoc
    
    private String documentGlobalIndexPath = "index";
    private String documentGlobalIndexXrefPageIdFormat = "system:generated:index/%s.adoc";

    public boolean isDryRun() {
        return getOutputFile() == null;
    }
    
    public File getDocumentGlobalIndexOutputFolder() {
        return Optional.ofNullable(getOutputFile())
                .map(File::getParentFile)
                .map(folder->new File(folder, getDocumentGlobalIndexPath()))
                .orElse(null);
    }
    
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

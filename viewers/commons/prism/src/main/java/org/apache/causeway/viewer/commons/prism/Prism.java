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
package org.apache.causeway.viewer.commons.prism;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Prism {
    DEFAULT("", false),
    COY("-coy", true),
    DARK("-dark", false),
    FUNKY("-funky", false),
    OKAIDIA("-okaidia", false),
    SOLARIZEDLIGHT("-solarizedlight", false),
    TOMORROW("-tomorrow", false),
    TWILIGHT("-twilight", false),
    ;
    final String themeSuffix;
    final boolean override;

    public String cssPrimaryFile() {
        return "prism/themes/prism" + themeSuffix + ".min.css";
    }

    public Optional<String> cssOverrideFile() {
        return override
                ? Optional.of("prismoverride/prism" + themeSuffix + ".css")
                : Optional.empty();
    }

    public List<String> cssFiles() {
        return cssOverrideFile()
                .map(cssOverride->List.of(cssPrimaryFile(), cssOverride))
                .orElse(List.of(cssPrimaryFile()));
    }

    public String jsFile() {
        return "prism/prism" + ".js";
    }
}

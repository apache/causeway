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
package org.apache.causeway.tooling.j2adoc.test;

import java.io.File;
import java.util.Set;

import org.apache.causeway.commons.internal.base._Files;
import org.apache.causeway.commons.internal.functions._Predicates;

import lombok.SneakyThrows;

final class ProjectSampler {

    static File local() {
        return new File("./").getAbsoluteFile();
    }
    
    static File apacheCausewayRoot() {
        return new File("./").getAbsoluteFile().getParentFile().getParentFile().getParentFile();
    }
    
    static File apacheCausewayApplib() {
        return new File(apacheCausewayRoot(), "api/applib");
    }

    @SneakyThrows
    public static Set<File> adocFiles(File folder) {
        return _Files.searchFiles(
                    folder, 
                    _Predicates.alwaysTrue(), 
                    file->file.getName().endsWith(".adoc"));
    }
    
}

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
package org.apache.isis.tooling.cli.adocfix;

import java.io.File;
import java.util.SortedSet;

import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.tooling.cli.CliConfig;
import org.apache.isis.tooling.j2adoc.J2AdocContext;

import lombok.NonNull;
import lombok.val;

public final class IncludeStatementFixer {

    public static void fixIncludeStatements(
            final @NonNull SortedSet<File> adocFiles,
            final @NonNull CliConfig cliConfig, 
            final @NonNull J2AdocContext j2aContext) {
        
        if(cliConfig.getProjectDoc().isDryRun()) {
            System.out.println("IncludeStatementFixer: skip (dry-run)");
            return;
        }
        
        if(!cliConfig.getProjectDoc().isFixOrphandedAdocIncludeStatements()) {
            System.out.println("IncludeStatementFixer: skip (disabled via config, fixOrphandedAdocIncludeStatements=false)");
            return;
        }
        
        System.out.println(String.format("IncludeStatementFixer: about to process %d adoc files", adocFiles.size()));
        
        val fixedCounter = _Refs.intRef(0); 
        
        adocFiles.forEach(f->{
            _Probe.errOut("adoc file found: %s", f);    
        });
        
        System.out.println(String.format("IncludeStatementFixer: all done. (%d adoc files fixed)", fixedCounter.getValue()));
        
    }
    
    // -- HELPER

}

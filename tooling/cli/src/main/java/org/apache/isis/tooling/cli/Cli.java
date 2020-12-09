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
import java.util.concurrent.Callable;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.tooling.cli.projdoc.ProjectDocModel;
import org.apache.isis.tooling.projectmodel.ProjectNodeFactory;

import lombok.val;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "cli", 
        mixinStandardHelpOptions = true, 
        version = "0.1",
        description = "CLI for the Apache Isis Tooling Ecosystem",
        subcommands = {
                Cli.ProjectDocCommand.class})
class Cli implements Callable<Integer> {

    @Option(
            names = {"-p", "--project"}, 
            description = "path to the (multi-module) project root (default: current dir)")
    private String projectRootPath;

    private _Lazy<CliConfig> configRef = _Lazy.threadSafe(()->CliConfig
            .read(projectRootPath!=null
                    ? new File(projectRootPath, "isis-tooling.yml")
                    : new File("isis-tooling.yml")));

    public CliConfig getConfig() {
        return configRef.get();
    }
    
    public  File getProjectRoot() {
        return projectRootPath!=null
                ? new File(projectRootPath)
                : new File(".");
    }

    @Override
    public Integer call() throws Exception { 
        // not used
        return 0;
    }

    // -- SUB COMMANDS

    @Command(
            name = "projdoc",
            description = "Writes a System Overview document (AsciiDoc) to given output.")
    static class ProjectDocCommand extends CliCommandAbstract {

        @Option(
                names = {"-o", "--output"}, 
                description = "path to the output file (default: NONE = write to std.out)")
        private String outputFilePath;

        @Override
        public Integer call() throws Exception {
            
            if(outputFilePath!=null) {
                getConfig().setOutputFile(new File(outputFilePath));
            }
            
            val projTree = ProjectNodeFactory.maven(getProjectRoot());
            val projectDocModel = new ProjectDocModel(projTree);
            projectDocModel.generateAsciiDoc(getConfig());
            return 0;
        }
    }
    
    //TODO mvn2gradle
    //description = "Detects differences between Maven and Gradle (multi-module) projects.",
    

    //    @Command
    //    int shout() {
    //        System.out.println("HI! " + getConfig());
    //        return 0;
    //    }

    // -- ENTRY POINT

    public static void main(String... args) {
        val cli = new Cli();
        _Context.putSingleton(Cli.class, cli);
        int exitCode = new CommandLine(cli).execute(args);
        System.exit(exitCode);
    }

    // -- HELPER


}
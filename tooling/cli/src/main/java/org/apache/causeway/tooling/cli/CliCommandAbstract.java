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
package org.apache.causeway.tooling.cli;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.tooling.cli.projdoc.ProjectDocModel;
import org.apache.causeway.tooling.projectmodel.ProjectNodeFactory;

import lombok.val;

abstract class CliCommandAbstract implements Callable<Integer> {

    public CliConfig getConfig() {
        return _Context.getElseFail(Cli.class).getConfig();
    }

    public File getProjectRoot() {
        return _Context.getElseFail(Cli.class).getProjectRoot();
    }

    public File getOverviewPath() {
        return _Context.getElseFail(Cli.class).getOverviewPath();
    }
    public File getIndexPath() {
        return _Context.getElseFail(Cli.class).getIndexPath();
    }

    /**
     * factor out common logic
     * @param mode
     */
    protected void generateAsciidoc(ProjectDocModel.Mode mode) {
        if (getOverviewPath() != null) {
            getConfig().getCommands().getOverview().setRootFolder(getOverviewPath());
        }
        if (getIndexPath() != null) {
            getConfig().getCommands().getIndex().setRootFolder(getIndexPath());
        }

        val projTree = ProjectNodeFactory.maven(getProjectRoot());
        val projectDocModel = new ProjectDocModel(projTree);
        projectDocModel.generateAsciiDoc(getConfig(), mode);
    }

}

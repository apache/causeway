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
package org.apache.isis.tooling.projectmodel;

import java.io.File;

import javax.annotation.Nullable;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.model.GradleProject;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.tooling.projectmodel.maven.MavenModelFactory;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
class ProjectNodeFactory_gradle {

    public static ProjectNode createProjectTree(@NonNull File projRootFolder) {
        try(val projectConnection = GradleConnector.newConnector().forProjectDirectory(projRootFolder).connect()) {
            val rootProject = projectConnection.getModel(GradleProject.class);
            val rootNode = visitGradleProject(null, rootProject);
            return rootNode;
        }
    }
    
    // -- HELPER
    
    private static ProjectNode visitGradleProject(
            final @Nullable ProjectNode parent, 
            final @NonNull GradleProject gradleProj) {
        
        val projNode = toProjectNode(parent, gradleProj);
        for(val child : gradleProj.getChildren()){
            visitGradleProject(projNode, child);
        }
        return projNode;
    }

    private static ProjectNode toProjectNode(
            final @Nullable ProjectNode parent, 
            final @NonNull GradleProject gradleProj) {
        
        val projNode = ProjectNode.builder()
                .parent(parent)
                .artifactCoordinates(artifactKeyOf(gradleProj))
                .name(_Strings.nullToEmpty(gradleProj.getName()))
                .description(_Strings.nullToEmpty(gradleProj.getDescription()))
                .projectDirectory(gradleProj.getProjectDirectory())
                .build();
        if(parent!=null) {
            parent.getChildren().add(projNode);
        }
        return projNode;
    }

    private static ArtifactCoordinates artifactKeyOf(final @NonNull GradleProject gradleProj) {
        val pomFile = new File(gradleProj.getProjectDirectory().getAbsoluteFile(), "pom.xml");
        if(pomFile.canRead()) {
            val mavenModel = MavenModelFactory.readModel(pomFile);
            if(mavenModel!=null) {
                return ProjectNodeFactory_maven.artifactCoordinatesOf(mavenModel);
            }
        }
        log.warn("cannot find pom.xml for project {} at {}", gradleProj.getName(), pomFile.getAbsolutePath());
        val groupId = "?";
        val artifactId = gradleProj.getName();
        val type = "?";
        val version = "?";
        return ArtifactCoordinates.of(groupId, artifactId, type, version);
    }

}

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
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.model.GradleProject;

import org.apache.isis.tooling.projectmodel.maven.MavenModelFactory;
import org.apache.isis.tooling.projectmodel.maven.SimpleModelResolver;

import lombok.val;

public class ProjectNodeFactory {

    public static ProjectNode maven(File projRootFolder) {
        val modelResolver = new SimpleModelResolver(projRootFolder);
        val rootModel = modelResolver.getRootModel();
        val interpolate = false; //XXX experimental
        return visitMavenProject(null, rootModel, modelResolver, interpolate);
    }

    public static ProjectNode gradle(File projRootFolder) {
        try(val projectConnection = GradleConnector.newConnector().forProjectDirectory(projRootFolder).connect()) {
            val rootProject = projectConnection.getModel(GradleProject.class);
            val rootNode = visitGradleProject(null, rootProject);
            return rootNode;
        } 
    }

    // -- HELPER MAVEN

    private static ProjectNode visitMavenProject(
            ProjectNode parent, 
            Model mavenProj, 
            SimpleModelResolver modelResolver,
            boolean interpolate) {
        
        val interpolatedProj = interpolate 
                ? MavenModelFactory.interpolateModel(mavenProj, modelResolver)
                : mavenProj;
        val projNode = toProjectNode(parent, interpolatedProj);
        for(val child : childrenOf(interpolatedProj, modelResolver)){
            visitMavenProject(projNode, child, modelResolver, interpolate);
        }
        return projNode;
    }
    
    private static ProjectNode toProjectNode(ProjectNode parent, Model mavenProj) {
        val projNode = ProjectNode.builder()
                .parent(parent)
                .artifactId(mavenProj.getArtifactId())
                .name(mavenProj.getName())
                .build();

        if(parent!=null) {
            parent.getChildren().add(projNode);
        }

        return projNode;

    }
    
    private static Iterable<Model> childrenOf(Model mavenProj, SimpleModelResolver modelResolver) {
        return mavenProj.getModules()
        .stream()
        .map(name->modelResolver.lookupCatalogForSubmoduleOf(mavenProj, name))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }
    
    // -- HELPER GRADLE
    
    private static ProjectNode visitGradleProject(ProjectNode parent, GradleProject gradleProj) {
        val projNode = toProjectNode(parent, gradleProj);
        for(val child : gradleProj.getChildren()){
            visitGradleProject(projNode, child);
        }
        return projNode;
    }

    private static ProjectNode toProjectNode(ProjectNode parent, GradleProject gradleProj) {
        val projNode = ProjectNode.builder()
                .parent(parent)
                .artifactId(gradleProj.getProjectIdentifier().getProjectPath())
                .name(gradleProj.getName())
                .build();
        if(parent!=null) {
            parent.getChildren().add(projNode);
        }
        return projNode;
    }



}

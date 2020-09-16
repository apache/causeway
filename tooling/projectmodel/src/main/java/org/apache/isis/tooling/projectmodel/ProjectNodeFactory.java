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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.maven.model.Model;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.model.GradleProject;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.tooling.projectmodel.maven.MavenModelFactory;
import org.apache.isis.tooling.projectmodel.maven.SimpleModelResolver;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProjectNodeFactory {

    public static ProjectNode maven(final @NonNull File projRootFolder) {
        val modelResolver = new SimpleModelResolver(projRootFolder);
        val rootModel = modelResolver.getRootModel();
        val interpolate = false; //XXX interpolation is experimental
        return visitMavenProject(null, rootModel, modelResolver, interpolate);
    }

    public static ProjectNode gradle(final @NonNull File projRootFolder) {
        try(val projectConnection = GradleConnector.newConnector().forProjectDirectory(projRootFolder).connect()) {
            val rootProject = projectConnection.getModel(GradleProject.class);
            val rootNode = visitGradleProject(null, rootProject);
            return rootNode;
        } 
    }

    // -- HELPER MAVEN

    private static ProjectNode visitMavenProject(
            final @Nullable ProjectNode parent, 
            final @NonNull Model mavenProj, 
            final @NonNull SimpleModelResolver modelResolver,
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
    
    private static ProjectNode toProjectNode(
            final @Nullable ProjectNode parent, 
            final @NonNull Model mavenProj) {
        val projNode = ProjectNode.builder()
                .parent(parent)
                .artifactKey(artifactKeyOf(mavenProj))
                .name(_Strings.nullToEmpty(mavenProj.getName()))
                .description(_Strings.nullToEmpty(mavenProj.getDescription()))
                .projectDirectory(mavenProj.getProjectDirectory())
                .build();
        
        mavenProj.getDependencies()
        .stream()
        .map(ProjectNodeFactory::toDependency)
        .forEach(projNode.getDependencies()::add);
        
        if(parent!=null) {
            parent.getChildren().add(projNode);
        }

        return projNode;
    }
    
    private static Dependency toDependency(final @NonNull org.apache.maven.model.Dependency dependency) {
        return Dependency.builder()
                .artifactKey(ArtifactKey.of(
                        dependency.getGroupId(), 
                        dependency.getArtifactId(),
                        dependency.getType(),
                        Optional.ofNullable(dependency.getVersion()).orElse("<managed>") //TODO to resolve this requires interpolation
                        ))
                .build();
    }
    
    private static ArtifactKey artifactKeyOf(final @NonNull Model mavenProj) {
        val groupId = MavenModelFactory.getGroupId(mavenProj);
        val artifactId = mavenProj.getArtifactId();
        val type = mavenProj.getPackaging();
        val version = MavenModelFactory.getVersion(mavenProj);
        return ArtifactKey.of(groupId, artifactId, type, version);
    }
    
    private static Iterable<Model> childrenOf(
            final @NonNull Model mavenProj, 
            final @NonNull SimpleModelResolver modelResolver) {
        
        return Stream.<String>concat(
                mavenProj.getProfiles().stream().flatMap(profile->profile.getModules().stream()),
                mavenProj.getModules().stream())
        .distinct()
        .map(name->modelResolver.lookupCatalogForSubmoduleOf(mavenProj, name))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }
    
    // -- HELPER GRADLE
    
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
                .artifactKey(artifactKeyOf(gradleProj))
                .name(_Strings.nullToEmpty(gradleProj.getName()))
                .description(_Strings.nullToEmpty(gradleProj.getDescription()))
                .projectDirectory(gradleProj.getProjectDirectory())
                .build();
        if(parent!=null) {
            parent.getChildren().add(projNode);
        }
        return projNode;
    }

    private static ArtifactKey artifactKeyOf(final @NonNull GradleProject gradleProj) {
        val pomFile = new File(gradleProj.getProjectDirectory().getAbsoluteFile(), "pom.xml");
        if(pomFile.canRead()) {
            val mavenModel = MavenModelFactory.readModel(pomFile);
            if(mavenModel!=null) {
                return artifactKeyOf(mavenModel);
            }
        }
        log.warn("cannot find pom.xml for project {} at {}", gradleProj.getName(), pomFile.getAbsolutePath());
        val groupId = "?";
        val artifactId = gradleProj.getName();
        val type = "?";
        val version = "?";
        return ArtifactKey.of(groupId, artifactId, type, version);
    }

}

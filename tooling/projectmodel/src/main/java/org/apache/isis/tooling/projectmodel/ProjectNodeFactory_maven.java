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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.maven.model.Model;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.tooling.projectmodel.Dependency.Location;
import org.apache.isis.tooling.projectmodel.maven.MavenModelFactory;
import org.apache.isis.tooling.projectmodel.maven.SimpleModelResolver;

import lombok.NonNull;
import lombok.val;

class ProjectNodeFactory_maven {

    public static ProjectNode createProjectTree(final @NonNull File projRootFolder) {
        val modelResolver = new SimpleModelResolver(projRootFolder);
        val rootModel = modelResolver.getRootModel();
        val interpolate = false; //XXX interpolation is experimental
        val projTree = visitMavenProject(null, rootModel, modelResolver, interpolate);

        postProcessDependencyLocation(projTree);
        postProcessDependencyVersion(projTree);
        return projTree;
    }

    // -- HELPER

    private static void postProcessDependencyLocation(final @NonNull ProjectNode projTree) {
        // first pass: collect local artifacts
        // second pass: update all local dependencies' location to LOCAL
        val localArtifacts = new HashSet<String>();
        projTree.depthFirst(projModel->{
            localArtifacts.add(projModel.getArtifactCoordinates().toStringWithGroupAndId());
        });

        projTree.depthFirst(projModel->{
            projModel.getDependencies().stream()
            .filter(dep->localArtifacts.contains(dep.getArtifactCoordinates().toStringWithGroupAndId()))
            .forEach(localDep->localDep.setLocation(Location.LOCAL));
        });
    }

    private static void postProcessDependencyVersion(final @NonNull ProjectNode projTree) {

        // first pass: collect external artifacts, that provide a non-empty version
        // second pass: update all external dependencies' versions
        val externalVersionByArtifact = new HashMap<String, String>();
        projTree.depthFirst(projModel->{

            projModel.getDependencies().stream()
            .filter(dependency->dependency.getLocation().isExternal())
            .map(Dependency::getArtifactCoordinates)
            .filter(ArtifactCoordinates::isVersionResolved)
            .forEach(coors->{
                externalVersionByArtifact.put(
                        coors.toStringWithGroupAndId(),
                        coors.getVersion());
            });

        });

        // log.debug("externalVersionbyArtifact {}",  externalVersionByArtifact);

    }

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
                .artifactCoordinates(artifactCoordinatesOf(mavenProj))
                .name(_Strings.nullToEmpty(mavenProj.getName()))
                .description(_Strings.nullToEmpty(mavenProj.getDescription()))
                .projectDirectory(mavenProj.getProjectDirectory())
                .build();

        mavenProj.getDependencies()
        .stream()
        .map(ProjectNodeFactory_maven::toDependency)
        .forEach(projNode.getDependencies()::add);

        if(parent!=null) {
            parent.getChildren().add(projNode);
        }

        return projNode;
    }

    private static Dependency toDependency(final @NonNull org.apache.maven.model.Dependency dependency) {
        val artifactCoordinates = ArtifactCoordinates.of(
                dependency.getGroupId(),
                dependency.getArtifactId(),
                dependency.getType(),
                Optional.ofNullable(dependency.getVersion()).orElse(ArtifactCoordinates.MANAGED_VERSION) //TODO to resolve this requires interpolation
                );

        return Dependency.builder()
                .artifactCoordinates(artifactCoordinates)
                .location(Location.EXTERNAL) // just priming here to be overwritten in post-processing if required
                .shortName(ArtifactShortNameFactory.toShortName(artifactCoordinates))
                .build();
    }

    static ArtifactCoordinates artifactCoordinatesOf(final @NonNull Model mavenProj) {
        val groupId = MavenModelFactory.getGroupId(mavenProj);
        val artifactId = mavenProj.getArtifactId();
        val type = mavenProj.getPackaging();
        val version = MavenModelFactory.getVersion(mavenProj);
        return ArtifactCoordinates.of(groupId, artifactId, type, version);
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




}

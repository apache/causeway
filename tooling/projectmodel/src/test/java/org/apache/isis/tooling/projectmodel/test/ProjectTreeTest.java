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
package org.apache.isis.tooling.projectmodel.test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.tooling.projectmodel.ProjectNode;
import org.apache.isis.tooling.projectmodel.ProjectNodeFactory;
import org.apache.isis.tooling.projectmodel.ProjectVisitor;

import lombok.val;

class ProjectTreeTest {
    
    File projRootFolder;
    
    @BeforeEach
    void setUp() throws Exception {
        projRootFolder = new File("./").getAbsoluteFile().getParentFile().getParentFile().getParentFile();
        System.out.println("running ProjectTreeTest at " + projRootFolder.getAbsolutePath());
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test @Disabled("for now we are missing some build.gradle files")
    void testGradle() {
        
        val projTree = ProjectNodeFactory.gradle(projRootFolder);
        
        val artifactKeys = new HashSet<String>();
        
        ProjectVisitor projectVisitor = projModel -> {
            artifactKeys.add(toString(projModel));
            System.out.println(toString(projModel));
        };

        projTree.depthFirst(projectVisitor);
        
        assertHasSomeArtifactKeys(artifactKeys);
        
    }
    
    @Test
    void testMaven() {
        
        val projTree = ProjectNodeFactory.maven(projRootFolder);
        
        val artifactKeys = new HashSet<String>();
        
        ProjectVisitor projectVisitor = projModel -> {
            artifactKeys.add(toString(projModel));
            System.out.println(toString(projModel));
        };
        
        projTree.depthFirst(projectVisitor);
        
        assertHasSomeArtifactKeys(artifactKeys);
    }
    
    private static String toString(ProjectNode node) {
        val artifactKey = node.getArtifactKey();
        val groupId = artifactKey.getGroupId();
        val artifactId = artifactKey.getArtifactId();
        val type = artifactKey.getType();
        return String.format("%s:%s:%s", groupId, artifactId, type);
    }
    
    private void assertHasSomeArtifactKeys(Set<String> artifactKeys) {
        assertTrue(artifactKeys.size()>50);
        assertTrue(artifactKeys.contains("org.apache.isis.core:isis-core:pom"));
        assertTrue(artifactKeys.contains("org.apache.isis.core:isis-core-config:jar"));
        assertTrue(artifactKeys.contains("org.apache.isis.core:isis-core-metamodel:jar"));
        assertTrue(artifactKeys.contains("org.apache.isis.core:isis-core-runtime:jar"));
        
        for(val key : artifactKeys) {
            assertFalse(key.startsWith("?"), ()->"incomplete key " + key);
        }
        
    }


}

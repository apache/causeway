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
package domainapp.dom.tree;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.isis.applib.tree.TreeAdapter;

import lombok.val;

public class FileSystemTreeAdapter implements TreeAdapter<FileNode> {

    @Override
    public Optional<FileNode> parentOf(FileNode value) {
        if(value.getType()==FileNode.Type.FileSystemRoot) {
            return Optional.empty();
        }
        val parentFolderIfAny = value.asFile().getParentFile();
        if(parentFolderIfAny==null) {
            return Optional.empty(); // unexpected code reach, but just in case
        }
        return Optional.ofNullable(parentFolderIfAny)
                .map(FileNodeFactory::toFileNode);
    }

    @Override
    public int childCountOf(FileNode value) {
        return (int) streamChildFiles(value).count();
    }

    @Override
    public Stream<FileNode> childrenOf(FileNode value) {
        return streamChildFiles(value)
                .map(FileNodeFactory::toFileNode);
    }

    // -- HELPER

    private static Stream<File> streamChildFiles(FileNode value){
        val file = value.asFile();
        val childFiles = file.listFiles();
        if(childFiles==null) {
            return Stream.empty();
        }
        return Stream.of(childFiles)
                .filter(f->!f.isHidden());
    }

}

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
package demoapp.dom.domain.properties.PropertyLayout.navigable;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.applib.graph.tree.TreeAdapter;

import lombok.val;

//tag::class[]
public class FileSystemTreeAdapter implements TreeAdapter<FileNodeVm> {

    @Override
    public Optional<FileNodeVm> parentOf(FileNodeVm fileNode) {
        if(fileNode.getType() == FileNodeType.FILE_SYSTEM_ROOT) {
            return Optional.empty();
        }
        val parentFolderIfAny = fileNode.asFile().getParentFile();
        return Optional.ofNullable(parentFolderIfAny)
                .map(FileNodeVm::new);
    }

    @Override
    public int childCountOf(FileNodeVm value) {
        return (int) streamChildFiles(value).count();
    }

    @Override
    public Stream<FileNodeVm> childrenOf(FileNodeVm value) {
        return streamChildFiles(value)
                .map(FileNodeVm::new);
    }

    private static Stream<File> streamChildFiles(FileNodeVm fileNode){
        val file = fileNode.asFile();
        val childFiles = file.listFiles();
        return childFiles != null
                ? Stream.of(childFiles)
                        .filter(f -> !f.isHidden())
                : Stream.empty();
    }
}
//end::class[]

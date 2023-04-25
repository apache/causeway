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
package demoapp.dom.types.causeway.treenode;

import java.io.File;
import java.util.Optional;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@Named("demo.TreeNodeVm")
@XmlRootElement(name="TreeNodeVm")
@DomainObject(nature=Nature.VIEW_MODEL)
@ToString
@NoArgsConstructor
public class FileNodeVm implements HasAsciiDocDescription {

    public FileNodeVm(final File file) {
        this.path = file.getAbsolutePath();
        this.type = file.isDirectory()
                        ? file.getParent() == null  // ie root
                            ? FileNodeType.FILE_SYSTEM_ROOT
                            : FileNodeType.DIRECTORY
                        : FileNodeType.FILE;
    }

//tag::title[]
    @ObjectSupport public String title() {
        return Optional.ofNullable(getPath())
                .map(File::new)
                .map(x -> x.getName().length() != 0 ? x.getName() : x.toString())
                .orElse("(root)");
    }
//end::title[]

//tag::iconName[]
    @ObjectSupport public String iconName() {
        return type!=null ? type.name() : "";
    }
//end::iconName[]

//tag::properties[]
    @Property
    @PropertyLayout(labelPosition = LabelPosition.TOP, fieldSetId = "detail", sequence = "1")
    public FileNodeVm getParent() {
        return Optional.ofNullable(getPath())
                .map(File::new)
                .map(File::getParentFile)
                .map(FileNodeVm::new)
                .orElse(null);
    }

    @Property
    @PropertyLayout(labelPosition = LabelPosition.TOP, fieldSetId = "detail", sequence = "2")
    @Getter @Setter
    private String path;

    @Property
    @PropertyLayout(labelPosition = LabelPosition.TOP, fieldSetId = "detail", sequence = "3")
    @Getter @Setter
    private FileNodeType type;
//end::properties[]

    File asFile() {
        return new File(getPath());
    }

}

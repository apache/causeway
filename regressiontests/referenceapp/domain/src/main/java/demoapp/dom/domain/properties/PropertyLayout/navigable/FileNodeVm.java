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

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Navigable;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Named("demo.FileNodeVm")
@DomainObject(nature=Nature.VIEW_MODEL)
@DomainObjectLayout(cssClassFa="fa-sitemap")
@XmlRootElement(name="FileNodeVm")
@NoArgsConstructor
@ToString
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
        if(this.path == null) {
            return "(root)";
        }
        var file = asFile();
        return file.getName().length()!=0 ? file.getName() : file.toString();
    }
//end::title[]

//tag::iconName[]
    @ObjectSupport public String iconName() {
        return type!=null ? type.name() : "";
    }
//end::iconName[]

//tag::navigable[]
    @Property
    @PropertyLayout(
            navigable=Navigable.PARENT,             // <.>
            hidden=Where.EVERYWHERE                 // <.>
    )
    public FileNodeVm getParent() {
        var parentFile = asFile().getParentFile();
        return parentFile != null
                ? new FileNodeVm(parentFile)
                : null;
    }
//end::navigable[]

    @Property
    @PropertyLayout(labelPosition = LabelPosition.TOP, fieldSetId = "detail", sequence = "2")
    @Getter @Setter
    private String path;

    @Property
    @PropertyLayout(labelPosition = LabelPosition.TOP, fieldSetId = "detail", sequence = "3")
    @Getter @Setter
    private FileNodeType type;

    File asFile() {
        return new File(path);
    }

}

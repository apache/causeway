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

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Navigable;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

@XmlRootElement(name="FileNode") 
@DomainObject(nature=Nature.VIEW_MODEL)
@ToString
public class FileNode {

    public static enum Type {
        FileSystemRoot,
        Folder,
        File
    }

    @Getter @Setter protected String path;
    @Getter @Setter protected Type type;

    public String title() {
        if(path==null) {
            return null;
        }
        val file = asFile();
        return file.getName().length()!=0 ? file.getName() : file.toString();
    }

    public String iconName() {
        return type!=null ? type.name() : "";
    }

    // -- BREADCRUMB SUPPORT

    @PropertyLayout(navigable=Navigable.PARENT, hidden=Where.EVERYWHERE)
    public FileNode getParent() {
        val parentFile = asFile().getParentFile();
        return parentFile!=null ? FileNodeFactory.toFileNode(parentFile) : null;
    }

    // -- INIT

    void init(File file) {
        this.path = file.getAbsolutePath();
        if(file.isDirectory()) {
            type = isRoot(file) ? Type.FileSystemRoot : Type.Folder; 
        } else {
            type = Type.File;
        }
    }

    // -- HELPER

    File asFile() {
        return new File(path);
    }

    private static boolean isRoot(File file) {
        return file.getParent()==null;
    }

}

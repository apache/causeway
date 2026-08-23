
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
package demoapp.dom.types.causeway;

import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom.types.causeway.blobs.CausewayBlobs;
import demoapp.dom.types.causeway.clobs.CausewayClobs;
import demoapp.dom.types.causeway.localresourcepaths.CausewayLocalResourcePaths;
import demoapp.dom.types.causeway.markups.CausewayMarkups;
import demoapp.dom.types.causeway.passwords.CausewayPasswords;
import demoapp.dom.types.causeway.treenode.FileNodeVm;

@Named("demo.CausewayTypesMenu")
@DomainService
@DomainObjectLayout(
        named="Causeway Types"
)
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
public class CausewayTypesMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-file-alt")
    public CausewayBlobs blobs(){
        return new CausewayBlobs();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-file-alt")
    public CausewayClobs clobs(){
        return new CausewayClobs();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-external-link-alt")
    public CausewayLocalResourcePaths localResourcePaths(){
        return new CausewayLocalResourcePaths();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-code")
    public CausewayMarkups markups(){
        return new CausewayMarkups();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-key")
    public CausewayPasswords passwords(){
        return new CausewayPasswords();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-sitemap")
    public FileNodeVm treeNodes(){
        return new FileNodeVm();
    }

}

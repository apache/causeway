
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

import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom.types.causeway.blobs.IsisBlobs;
import demoapp.dom.types.causeway.clobs.IsisClobs;
import demoapp.dom.types.causeway.localresourcepaths.IsisLocalResourcePaths;
import demoapp.dom.types.causeway.markups.CausewayMarkups;
import demoapp.dom.types.causeway.passwords.IsisPasswords;

@Named("demo.IsisTypesMenu")
@DomainService(
        nature=NatureOfService.VIEW
)
@DomainObjectLayout(
        named="Isis Types"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class IsisTypesMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-file-alt")
    public IsisBlobs blobs(){
        return new IsisBlobs();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-file-alt")
    public IsisClobs clobs(){
        return new IsisClobs();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-external-link-alt")
    public IsisLocalResourcePaths localResourcePaths(){
        return new IsisLocalResourcePaths();
    }
    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-code")
    public CausewayMarkups markups(){
        return new CausewayMarkups();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-key")
    public IsisPasswords passwords(){
        return new IsisPasswords();
    }


}

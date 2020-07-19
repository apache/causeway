
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
package demoapp.dom.types.isis;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;

import lombok.extern.log4j.Log4j2;

import demoapp.dom.types.isis.blobs.IsisBlobs;
import demoapp.dom.types.isis.clobs.IsisClobs;
import demoapp.dom.types.isis.images.IsisImages;
import demoapp.dom.types.isis.localresourcepaths.IsisLocalResourcePaths;
import demoapp.dom.types.isis.markups.IsisMarkups;
import demoapp.dom.types.isis.passwords.IsisPasswords;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.IsisTypesMenu")
@DomainObjectLayout(named="Isis Types")
@Log4j2
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

    @Action(
            semantics = SemanticsOf.SAFE,
            hidden = Where.EVERYWHERE // TODO: ISIS-2411
    )
    @ActionLayout(cssClassFa="fa-image")
    public IsisImages images(){
        return new IsisImages();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-external-link-alt")
    public IsisLocalResourcePaths localResourcePaths(){
        return new IsisLocalResourcePaths();
    }
    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-code")
    public IsisMarkups markups(){
        return new IsisMarkups();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-clock")
    public IsisPasswords passwords(){
        return new IsisPasswords();
    }


}

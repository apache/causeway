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
package demoapp.dom.types;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.applib.value.NamedWithMimeType;
import org.apache.isis.core.commons.internal.base._Bytes;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.resources._Resources;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.types.markup.MarkupDemo;
import demoapp.dom.types.text.TextDemo;
import demoapp.dom.types.wrapper.WrapperDemo;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.DataTypesMenu")
@DomainObjectLayout(named="DataTypes")
@Log4j2
public class DataTypesMenu {

    @Inject private FactoryService factoryService;

    @Action
    @ActionLayout(cssClassFa="fa-font")
    public TextDemo text(){
        val demo = factoryService.viewModel(TextDemo.class);

        demo.setString("a string (click me)");
        demo.setStringMultiline("A multiline string\nspanning\n3 lines. (click me)");

        demo.setStringReadonly("a readonly string (but allows text select)");
        demo.setStringMultilineReadonly("A readonly string\nspanning\n3 lines. (but allows text select)");

        return demo;
    }
    
    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-hashtag")
    public WrapperDemo wrappers(){
        return new WrapperDemo();
    }




    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-code")
    public MarkupDemo markup(){
        val demo = factoryService.viewModel(MarkupDemo.class);

        try {
            val htmlSource = _Strings.read(_Resources.load(MarkupDemo.class, "markup-embedded.html"), StandardCharsets.UTF_8);
            demo.setMarkup(new Markup(htmlSource));
        } catch (Exception e) {
            log.error("failed to create Markup from file resource", e);
        }

        return demo;
    }

}

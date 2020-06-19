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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
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

import demoapp.dom.types.blob.BlobDemo;
import demoapp.dom.types.clob.ClobDemo;
import demoapp.dom.types.markup.MarkupDemo;
import demoapp.dom.types.primitive.PrimitivesDemo;
import demoapp.dom.types.text.TextDemo;
import demoapp.dom.types.time.TemporalDemo;
import demoapp.dom.types.uuid.UuidDemo;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.FeaturedTypesMenu")
@DomainObjectLayout(named="Featured Types")
@Log4j2
public class FeaturedTypesMenu {

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
    
    @Action
    @ActionLayout(cssClassFa="fa-hashtag")
    public PrimitivesDemo primitives(){
        val demo = factoryService.viewModel(PrimitivesDemo.class);

        demo.setJavaLangByte(Byte.MAX_VALUE);
        demo.setJavaLangShort(Short.MAX_VALUE);
        demo.setJavaLangInteger(Integer.MAX_VALUE);
        demo.setJavaLangLong(Long.MAX_VALUE);

        demo.setJavaLangFloat(Float.MAX_VALUE);
        demo.setJavaLangDouble(Double.MAX_VALUE);

        return demo;
    }

    @Action
    @ActionLayout(cssClassFa="fa-clock")
    public TemporalDemo temporals(){
        val demo = factoryService.viewModel(TemporalDemo.class);

        demo.setJavaUtilDate(new Date());
        demo.setJavaSqlDate( new java.sql.Date(System.currentTimeMillis()));
        demo.setJavaSqlTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));

        demo.setJavaLocalDate(LocalDate.now());
        demo.setJavaLocalDateTime(LocalDateTime.now());
        demo.setJavaOffsetDateTime(OffsetDateTime.now());

        return demo;
    }

    @Action
    @ActionLayout(cssClassFa="fa-at")
    public UuidDemo uuid(){
        val demo = factoryService.viewModel(UuidDemo.class);
        demo.setUuid(UUID.randomUUID());
        return demo;
    }

    @Action
    @ActionLayout(cssClassFa="fa-cloud")
    public BlobDemo blobs(){
        val demo = factoryService.viewModel(BlobDemo.class);

        try {
            val bytes = _Bytes.of(_Resources.load(BlobDemo.class, "isis-logo-568x286.png"));
            demo.setLogo(Blob.of("isis-logo-568x286", NamedWithMimeType.CommonMimeType.PNG, bytes));
        } catch (Exception e) {
            log.error("failed to create Blob from image resource", e);
        }

        return demo;
    }
    
    @Action
    @ActionLayout(cssClassFa="fa-cloud")
    public ClobDemo clobs(){
        val demo = factoryService.viewModel(ClobDemo.class);

        try {
            val text = _Strings.read(_Resources.load(ClobDemo.class, "document.txt"), StandardCharsets.UTF_8);
            demo.setDocument(Clob.of("document", NamedWithMimeType.CommonMimeType.TXT, text));
        } catch (Exception e) {
            log.error("failed to create Clob from text resource", e);
        }

        return demo;
    }
    
    @Action
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

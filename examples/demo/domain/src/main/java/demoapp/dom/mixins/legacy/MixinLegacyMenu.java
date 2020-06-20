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
package demoapp.dom.mixins.legacy;

import java.util.ArrayList;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.factory.FactoryService;

import lombok.val;

import demoapp.dom.mixins.DemoItem;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.MixinLegacyMenu")
public class MixinLegacyMenu {

    @Inject private FactoryService factoryService;

    @Action
    @ActionLayout(
            cssClassFa="fa-bolt",
            describedAs="Opens the Mixin-Legacy-Demo page.")
    public MixinLegacyDemo mixinLegacyDemo(){
        val demo = factoryService.viewModel(MixinLegacyDemo.class);

        demo.setNote( "Update me! The button below is contributed by one of my mixins.");

        demo.collection.add(DemoItem.of("first", null));
        demo.collection.add(DemoItem.of("second", demo.collection.get(0)));
        demo.collection.add(DemoItem.of("third", demo.collection.get(1)));

        return demo;
    }

}
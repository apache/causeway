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
package org.apache.causeway.extensions.layoutgithub.gridloader.menu;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.extensions.layoutgithub.gridloader.CausewayModuleExtLayoutGithubGridLoader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Provides actions to managed the dynamic loading of layouts from a github source code repository.
 * <p>
 *
 * @since 2.x {@index}
 */
@Named(CausewayModuleExtLayoutGithubGridLoader.NAMESPACE + ".GridLoaderMenu")
@DomainService(nature = NatureOfService.VIEW)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.TERTIARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class GridLoaderMenu {

    public static abstract class ActionDomainEvent<T> extends CausewayModuleApplib.ActionDomainEvent<T> {}

    @Getter
    private boolean enabled;
    @Getter
    private Optional<String> branch = Optional.empty();


    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = enableDynamicLayoutLoading.ActionDomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            cssClassFa = "fa-solid fa-toggle-on",
            sequence = "100"
    )
    public class enableDynamicLayoutLoading {

        public class ActionDomainEvent extends GridLoaderMenu.ActionDomainEvent<enableDynamicLayoutLoading> {}

        @MemberSupport public void act(
                @Parameter(optionality = Optionality.OPTIONAL)
                final String branch) {
            GridLoaderMenu.this.enabled = true;
            GridLoaderMenu.this.branch = Optional.ofNullable(branch);
        }
        public String disableAct() {
            return GridLoaderMenu.this.enabled ? "Already enabled" : null;
        }
        public String default0Act() {
            return GridLoaderMenu.this.branch.orElse("main");
        }
    }


    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = enableDynamicLayoutLoading.ActionDomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            cssClassFa = "fa-solid fa-toggle-off",
            sequence = "100"
    )
    public class disableDynamicLayoutLoading {

        public class ActionDomainEvent extends GridLoaderMenu.ActionDomainEvent<enableDynamicLayoutLoading> {}

        @MemberSupport public void act() {
            GridLoaderMenu.this.enabled = false;
        }
        public String disableAct() {
            return GridLoaderMenu.this.enabled ? null : "Already disabled";
        }
    }

}

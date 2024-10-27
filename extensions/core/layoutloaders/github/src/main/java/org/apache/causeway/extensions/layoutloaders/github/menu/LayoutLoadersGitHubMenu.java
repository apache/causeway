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
package org.apache.causeway.extensions.layoutloaders.github.menu;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.layoutloaders.github.CausewayModuleExtLayoutLoadersGithub;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Provides actions to managed the dynamic loading of layouts from a github source code repository.
 * <p>
 *
 * @since 2.x {@index}
 */
@Named(CausewayModuleExtLayoutLoadersGithub.NAMESPACE + ".LayoutLoadersGitHubMenu")
@DomainService
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.TERTIARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class LayoutLoadersGitHubMenu {

    public static abstract class ActionDomainEvent<T> extends CausewayModuleApplib.ActionDomainEvent<T> {}

    final CausewayConfiguration causewayConfiguration;

    @Getter
    private boolean enabled;

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

        public class ActionDomainEvent extends LayoutLoadersGitHubMenu.ActionDomainEvent<enableDynamicLayoutLoading> {}

        @MemberSupport public void act() {
            LayoutLoadersGitHubMenu.this.enabled = true;
        }
        @MemberSupport public boolean hideAct() {
            return isNotConfigured();
        }
        @MemberSupport public String disableAct() {
            return LayoutLoadersGitHubMenu.this.enabled ? "Already enabled" : null;
        }

    }

    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = disableDynamicLayoutLoading.ActionDomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            cssClassFa = "fa-solid fa-toggle-off",
            sequence = "100"
    )
    public class disableDynamicLayoutLoading {

        public class ActionDomainEvent extends LayoutLoadersGitHubMenu.ActionDomainEvent<disableDynamicLayoutLoading> {}

        @MemberSupport public void act() {
            LayoutLoadersGitHubMenu.this.enabled = false;
        }
        @MemberSupport public boolean hideAct() {
            return isNotConfigured();
        }
        @MemberSupport public String disableAct() {
            return LayoutLoadersGitHubMenu.this.enabled ? null : "Already disabled";
        }
    }

    boolean isNotConfigured() {
        var layoutLoadersGitHub = causewayConfiguration.getExtensions().getLayoutLoaders().getGithub();
        return layoutLoadersGitHub.getRepository() == null ||
                layoutLoadersGitHub.getApiKey() == null;
    }

}

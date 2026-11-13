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
package org.apache.causeway.extensions.docgen.help;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.extensions.docgen.help.applib.HelpNode.HelpTopic;
import org.apache.causeway.extensions.docgen.help.menu.DocumentationMenu;
import org.apache.causeway.extensions.docgen.help.topics.domainobjects.CausewayEntityDiagramPage;
import org.apache.causeway.extensions.docgen.help.topics.domainobjects.DomainEntityDiagramPage;
import org.apache.causeway.extensions.docgen.help.topics.domainobjects.DomainEntityDiagramPage2;
import org.apache.causeway.extensions.docgen.help.topics.welcome.WelcomeHelpPage;

/**
 * Adds the {@link DocumentationMenu} with its auto-configured menu entries.
 * @since 2.0 {@index}
 */
@Configuration
@Import({
    // menu providers
    DocumentationMenu.class,

    // help pages, as required by the default RootHelpTopic below (in case when to be managed by Spring)
    WelcomeHelpPage.class,
    CausewayEntityDiagramPage.class,
    DomainEntityDiagramPage.class,
    DomainEntityDiagramPage2.class

})
// keep class-name in sync with CausewayExtSecmanRegularUserRoleAndPermissions
public class CausewayModuleExtDocgenHelp {

    // keep in sync with CausewayExtDocgenRoleAndPermissions.NAMESPACE
    public static final String NAMESPACE = "causeway.ext.docgen";

    /**
     * The help index (tree), if not provided already (somewhere else).
     *
     * @apiNote To override this, simply using {@code @Primary} will not be sufficient.
     *      One must also make sure the overriding config gets registered to the Spring context
     *      before this one.
     *      (see <a href="https://github.com/spring-projects/spring-framework/issues/18552">Spring issue 18552</a>)
     */
    @Bean(NAMESPACE + ".RootHelpTopic")
    @ConditionalOnMissingBean(HelpTopic.class)
    @Qualifier("Default")
    public HelpTopic rootHelpTopic(
            final WelcomeHelpPage welcomeHelpPage,
            final CausewayEntityDiagramPage causewayEntityDiagramPage,
            final DomainEntityDiagramPage domainEntityDiagramPage,
            final DomainEntityDiagramPage2 domainEntityDiagramPage2) {

        var root = HelpTopic.root("Topics");

        root.addPage(welcomeHelpPage);

        root.subTopic("Causeway")
            .addPage(causewayEntityDiagramPage);

        root.subTopic("Domain")
            .addPage(domainEntityDiagramPage)
            .addPage(domainEntityDiagramPage2);

        return root;
    }

}

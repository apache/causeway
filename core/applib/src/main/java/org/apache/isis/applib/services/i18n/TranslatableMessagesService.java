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
package org.apache.isis.applib.services.i18n;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Clob;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY
)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        named = "Prototyping"
)
public class TranslatableMessagesService {

    @Programmatic
    @PostConstruct
    public void init(final Map<String,String> config) {
        final Properties props = new Properties();
        for (final String key : config.keySet()) {
            props.put(key, config.get(key));
        }
        props.list(System.out);
    }

    @Programmatic
    @PreDestroy
    public void shutdown() {

    }


    @Action(
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            cssClassFa = "fa-download"
    )
    public Clob downloadPotFile(
            @ParameterLayout(named = ".pot file name")
            final String potFileName) {
        final Map<String, Collection<String>> messages = translationService.messages();
        final StringBuilder buf = new StringBuilder();
        for (String message : messages.keySet()) {
            final Collection<String> contexts = messages.get(message);
            for (String context : contexts) {
                buf.append("#: ").append(context).append("\n");
            }
            buf.append("msgid: \"").append(message).append("\"\n");
            buf.append("msgstr: \"\"\n");
            buf.append("\n\n\n");
        }
        return new Clob(potFileName, "text/plain", buf.toString());
    }

    public String default0DownloadPotFile() {
        return "myapp.pot";
    }

    // //////////////////////////////////////


    @Inject
    private TranslationService translationService;

}

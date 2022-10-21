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
package demoapp.dom.types.causeway.passwords.persistence;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.ActionLayout.Position;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.value.Password;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolder;
import demoapp.dom.types.causeway.passwords.holder.CausewayPasswordHolder2;

@Named("demo.CausewayPasswordEntity") // shared permissions with concrete sub class
@DomainObject
public abstract class CausewayPasswordEntity
implements
    HasAsciiDocDescription,
    CausewayPasswordHolder2,
    ValueHolder<Password> {

    @Override
    public Password value() {
        return getReadOnlyProperty();
    }

    // -- PASSWORD CHECKER DEMO

    @Inject private transient MessageService messageService;

    @Action
    @ActionLayout(associateWith = "readWriteProperty"
        , position = Position.PANEL
        , cssClass = "bg-warning")
    public CausewayPasswordEntity showPassword() {
        messageService.informUser(String
                .format("password: '%s'", getReadWriteProperty().getPassword()));
        return this;
    }

    @Action
    @ActionLayout(associateWith = "readWriteProperty"
        , position = Position.PANEL
        , promptStyle = PromptStyle.DIALOG_MODAL)
    public CausewayPasswordEntity checkPassword(final Password confirm) {
        if(getReadWriteProperty().checkPassword(confirm.getPassword())) {
            messageService.informUser("passwords did match");
        } else {
            messageService.warnUser("passwords did not match");
        }
        return this;
    }

}

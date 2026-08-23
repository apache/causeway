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

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.value.Password;

import lombok.RequiredArgsConstructor;

// This class is NOT generated
@Action
@ActionLayout(associateWith = "readWriteProperty"
        , position = ActionLayout.Position.PANEL
        , promptStyle = PromptStyle.DIALOG_MODAL
)
@RequiredArgsConstructor
public class CausewayPasswordEntity_checkPassword {

    private final CausewayPasswordEntity entity;

    public CausewayPasswordEntity act(final Password confirm) {
        if(entity.getReadWriteProperty().checkPassword(confirm.password())) {
            messageService.informUser("passwords did match");
        } else {
            messageService.warnUser("passwords did not match");
        }
        return entity;
    }
    @Inject private transient MessageService messageService;
}

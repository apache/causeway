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
package org.apache.causeway.testing.integtestsupport.applib.annotation;

import java.util.Locale;

import org.apache.causeway.applib.clock.VirtualClock;
import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.DateTimeFormat;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InteractAsUtils {

    public InteractionContext toInteractionContext(final InteractAs testWith) {
        val user = _Strings.isNotEmpty(testWith.userName())
                ? UserMemento.ofName(testWith.userName())
                : UserMemento.system();

        val mainLocale = _Strings.isNotEmpty(testWith.localeName())
                ? Locale.forLanguageTag(testWith.localeName())
                : Locale.getDefault();

        val virtualClock = _Strings.isNotEmpty(testWith.frozenDateTime())
                ? VirtualClock.frozenAt(DateTimeFormat.CANONICAL.parseDateTime(testWith.frozenDateTime()))
                : VirtualClock.system();

        return InteractionContext.ofUserWithSystemDefaults(user)
                .withLocale(UserLocale.valueOf(mainLocale))
                .withClock(virtualClock);
    }

}

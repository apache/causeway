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
package org.apache.isis.extensions.secman.applib.user.dom.mixins;

import javax.inject.Inject;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.user.UserCurrentSessionTimeZoneHolder;
import org.apache.isis.commons.internal.base._Temporals;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;

import lombok.RequiredArgsConstructor;

@Property(
        editing = Editing.DISABLED,
        hidden = Where.ALL_TABLES)
@PropertyLayout(
        fieldSetId = "regional",
        describedAs = "Timezone ID as stored in your current session. (Logout/Login to change.)",
        sequence = "2"
)
@RequiredArgsConstructor
public class ApplicationUser_timeZone {

    private final ApplicationUser mixee;

    @Inject private UserCurrentSessionTimeZoneHolder userCurrentSessionTimeZoneHolder;

    @MemberSupport
    @Nullable
    public String prop() {
        return userCurrentSessionTimeZoneHolder.getUserTimeZone()
            .map(_Temporals::formatZoneId)
            .orElse(null);
    }

    @MemberSupport public boolean hideProp() {
        // time-zone information only makes sense in the context of the current (logged on) user
        return !mixee.isForSelf();
    }

}

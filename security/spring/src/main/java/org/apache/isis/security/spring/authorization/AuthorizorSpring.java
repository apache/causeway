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

package org.apache.isis.security.spring.authorization;

import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.security.authentication.Authentication;
import org.apache.isis.core.security.authorization.standard.Authorizor;

import lombok.NonNull;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.security.AuthorizorSpring")
@Order(OrderPrecedence.EARLY)
@Qualifier("Spring")
public class AuthorizorSpring implements Authorizor {

    @Override
    public boolean isVisible(
            final Authentication authentication, 
            final Identifier identifier) {
        // TODO ask SecMan to resolve this
        return false;
    }

    @Override
    public boolean isUsable(
            final Authentication authentication, 
            final Identifier identifier) {
        // TODO ask SecMan to resolve this
        return false;
    }

    // -- HELPER

    //@Inject private UserService userService;
//    private boolean anyMatchOnRoles(
//            final @Nullable Authentication authentication, 
//            final @NonNull Predicate<String> predicate) {
//        if(authentication==null || authentication.getUser()==null) {
//            return false;
//        }
//        return authentication.getUser().streamRoleNames()
//                .anyMatch(predicate);
//    }

}

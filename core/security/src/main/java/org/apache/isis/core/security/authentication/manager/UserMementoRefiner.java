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

package org.apache.isis.core.security.authentication.manager;

import java.util.List;
import java.util.Map;

import org.springframework.lang.Nullable;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.exceptions.unrecoverable.NoAuthenticatorException;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Timing;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.Authenticator;
import org.apache.isis.core.security.authentication.standard.RandomCodeGenerator;
import org.apache.isis.core.security.authentication.standard.Registrar;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * SPI provided by the internal {@link AuthenticationManager}, allowing the {@link UserMemento} representing an
 * authenticated (logged in) user to be refined.
 *
 * <p>
 *     As {@link UserMemento} is immutable, the implementation must return a new instance.  This new instance replaces
 *     the {@link InteractionContext#getUser() user reference held} by {@link InteractionContext}.
 * </p>
 *
 * <p>
 *     Originally introduced to allow SecMan to update the {@link UserMemento#getMultiTenancyToken() multi-tenancy}
 *     token of {@link UserMemento}.
 * </p>
 */
public interface UserMementoRefiner {

    /**
     * Return either the same (unchanged) or an updated {@link UserMemento} to use instead of the original within the
     * owning {@link InteractionContext}.
     *
     * @param userMemento - to be refined
     * @return the userMemento that is refined, or else unchanged.
     */
    UserMemento refine(final UserMemento userMemento);

}

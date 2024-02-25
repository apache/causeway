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
 *
 */
package org.apache.causeway.persistence.jdo.datanucleus.changetracking;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.datanucleus.enhancement.Persistable;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.InteractionScope;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PreAndPostValue;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyValuePlaceholder;
import org.apache.causeway.persistence.commons.integration.changetracking.PreAndPostValueEvaluatorService;
import org.apache.causeway.persistence.jdo.datanucleus.entities.DnOidStoreAndRecoverHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Named("causeway.persistence.jdo.PreAndPostValueEvaluatorServiceJdo")
@Priority(PriorityPrecedence.MIDPOINT) // before the default
@Qualifier("jdo")
@InteractionScope   // see note above regarding this
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class PreAndPostValueEvaluatorServiceJdo implements PreAndPostValueEvaluatorService {

    @Override
    public boolean differ(PreAndPostValue papv) {

        // don't audit objects that were created and then immediately deleted within the same xactn
        if (papv.getPre() == PropertyValuePlaceholder.NEW
                && papv.getPost() == PropertyValuePlaceholder.DELETED) {
            return false;
        }
        // but do always audit objects that have just been created or deleted
        if (papv.getPre() == PropertyValuePlaceholder.NEW
                || papv.getPost() == PropertyValuePlaceholder.DELETED) {
            return true;
        }
        if (papv.getPre() instanceof Persistable) {
            Persistable prePersistable = (Persistable) papv.getPre();

            if (!(papv.getPost() instanceof Persistable)) {
                // must be different, so publish
                return true;
            }
            Persistable postPersistable = ((Persistable) papv.getPost());

            Optional<String> preOidIfAny = DnOidStoreAndRecoverHelper.forEntity(prePersistable).recoverOid();
            Optional<String> postOidIfAny = DnOidStoreAndRecoverHelper.forEntity(postPersistable).recoverOid();

            if (preOidIfAny.isPresent() || postOidIfAny.isPresent()) {
                // at least one of the Persistables is hollow
                return !Objects.equals(preOidIfAny, postOidIfAny);
            }

            // neither of the Persistables is hollow; should be safe to fall through.

        } else {

            if (papv.getPost() instanceof Persistable) {
                // must be different, so publish
                return true;
            }

            // should be safe to fall through.
        }

        // else - for updated objects - audit only if the property value has changed
        return !Objects.equals(papv.getPre(), papv.getPost());
    }

}

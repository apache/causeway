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
package org.apache.isis.persistence.jdo.integration.changetracking;

import java.util.UUID;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.isis.core.metamodel.services.objectlifecycle.PropertyChangeRecord;

import lombok.val;

final class _EntityPropertyChangeFactory {

    public static EntityPropertyChange createEntityPropertyChange(
            final java.sql.Timestamp timestamp,
            final String user,
            final TransactionId txId,
            final PropertyChangeRecord propertyChangeRecord) {

        val spec = propertyChangeRecord.getEntity().getSpecification();

        final Bookmark target = propertyChangeRecord.getBookmark();
        final String propertyId = propertyChangeRecord.getPropertyId();
        final String memberId = propertyChangeRecord.getMemberId();
        final String preValue = propertyChangeRecord.getPreAndPostValue().getPreString();
        final String postValue = propertyChangeRecord.getPreAndPostValue().getPostString();
        final String targetClass = IdentifierUtil.targetClassNameFor(spec);

        final UUID transactionId = txId.getInteractionId();
        final int sequence = txId.getSequence();

        return EntityPropertyChange.of(
                transactionId, sequence, targetClass, target,
                memberId, propertyId, preValue, postValue, user, timestamp);
    }
}

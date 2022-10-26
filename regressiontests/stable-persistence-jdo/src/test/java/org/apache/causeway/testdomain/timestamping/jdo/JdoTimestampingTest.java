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
package org.apache.causeway.testdomain.timestamping.jdo;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.testdomain.conf.Configuration_usingJdo;
import org.apache.causeway.testdomain.jdo.entities.JdoProduct;
import org.apache.causeway.testdomain.jdo.entities.JdoProductComment;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class
        },
        properties = {
                "logging.level.org.apache.causeway.persistence.jdo.datanucleus.changetracking.JdoLifecycleListener = DEBUG"
        })
@Transactional
class JdoTimestampingTest extends CausewayIntegrationTestAbstract {

    @Inject private RepositoryService repository;

    @Test
    void updatedByAt_shouldBeFilledInByTheTimestampingService() {

        val product = repository.allInstances(JdoProduct.class).listIterator().next();
        assertNotNull(product);

        val comment = new JdoProductComment();
        comment.setProduct(product);
        comment.setComment("Awesome Book!");

        repository.persist(comment);

        assertNotNull(comment.getUpdatedAt());
        assertNotNull(comment.getUpdatedBy());

        //[CAUSEWAY-3126] see if we can update the persistent entity,
        // without triggering a nested loop
        val firstUpdate = comment.getUpdatedAt();
        comment.setComment("Awesome Book, really!");
        repository.persist(comment);

        assertNotNull(comment.getUpdatedAt());
        val secondUpdate = comment.getUpdatedAt();

        assertNotEquals(firstUpdate.getNanos(), secondUpdate.getNanos());

    }

}

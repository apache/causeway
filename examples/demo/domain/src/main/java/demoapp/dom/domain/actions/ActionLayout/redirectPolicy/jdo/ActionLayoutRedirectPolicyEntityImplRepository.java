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
package demoapp.dom.domain.actions.ActionLayout.redirectPolicy.jdo;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.actions.ActionLayout.redirectPolicy.ActionLayoutRedirectPolicyEntityRepository;
import demoapp.dom.types.Samples;

@Profile("demo-jdo")
@Service
public class ActionLayoutRedirectPolicyEntityImplRepository
extends ValueHolderRepository<String, ActionLayoutRedirectPolicyEntityImpl> implements ActionLayoutRedirectPolicyEntityRepository {

    @Inject Samples<Blob> blobSamples;

    final Random random;

    protected ActionLayoutRedirectPolicyEntityImplRepository() {
        super(ActionLayoutRedirectPolicyEntityImpl.class);
        random = new Random(DateTime.now().getMillis());
    }

    @Override
    protected ActionLayoutRedirectPolicyEntityImpl newDetachedEntity(final String value) {
        var entity = new ActionLayoutRedirectPolicyEntityImpl(value);
        entity.setBlob(randomBlob());
        return entity;
    }

    private Blob randomBlob() {
        List<Blob> list = blobSamples.stream()
                    .filter(x -> NamedWithMimeType.CommonMimeType.PDF.matches(x.getMimeType()))
                    .collect(Collectors.toList());
        return list.get(random.nextInt(list.size()));
    }
}

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
package org.apache.causeway.testdomain.model.good;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;

import lombok.RequiredArgsConstructor;

/**
 * For (test) mixin descriptions see {@link ProperMixinContribution}.
 * <p>
 * Introduced for issue https://issues.apache.org/jira/browse/CAUSEWAY-3531
 * <p>
 * Verify: make sure this one does not get picked up as association (property)!
 */
@DomainObject(introspection = Introspection.ANNOTATION_OPTIONAL, nature = Nature.MIXIN, mixinMethod = "act")
@RequiredArgsConstructor
public class ProperMixinContribution_action4 {

    @SuppressWarnings("unused")
    private final ProperMixinContribution holder;

    @Action(semantics = SemanticsOf.SAFE)
    public Blob act() {
        return Blob.of("sample", CommonMimeType.BIN, null);
    }

}

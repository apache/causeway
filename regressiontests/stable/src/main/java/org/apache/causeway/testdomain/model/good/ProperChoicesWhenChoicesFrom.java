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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.commons.internal.collections._Lists;

@DomainObject(nature = Nature.VIEW_MODEL)
public class ProperChoicesWhenChoicesFrom {

    @Collection
    public List<String> getCandidates() {
        return _Lists.of("a", "b", "c");
    }

    // expected to pass MM validation, even though there are no member-support methods
    // that would provide parameter choices (for 'input'), instead @Action(choicesFrom = "candidates")
    // should be sufficient to derive both
    // 1. ActionParameterChoicesFacetFromChoicesFromFacet
    // 2. ActionParameterDefaultsFacetFromAssociatedCollection
    @Action(choicesFrom = "candidates")
    public List<String> appendACharacterToCandidates(final List<String> input) {
        return input.stream()
                .map(candidate->candidate + "!")
                .collect(Collectors.toList());
    }

}

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

import java.util.Objects;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;

/**
 * For (test) mixin as Java record with additional record components, see {@link ProperMixinContribution}.
 */
@Action
public record ProperMixinContribution_actionRecord2(
		ProperMixinContribution mixee,
		FactoryService factoryService, //arbitrary service 1 for testing proper constructor injection
		RepositoryService repositoryService //arbitrary service 2 for testing proper constructor injection
		) {

	// throws if not properly initialized
	public ProperMixinContribution_actionRecord2 {
		Objects.requireNonNull(mixee);
		Objects.requireNonNull(factoryService);
		Objects.requireNonNull(repositoryService);
	}

	@MemberSupport
    public Blob act() {
        return Blob.of("sample", CommonMimeType.BIN, null);
    }

}

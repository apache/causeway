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
package org.apache.causeway.testing.archtestsupport.applib.modulerules;

import java.util.List;

/**
 * Defines the rules for which subpackages or a module to check and whether the classes in those subpackages can access
 * the classes in other subpackages either for the same module, or for modules that are referenced
 * ({@link org.springframework.context.annotation.Import}ed) directly or transitively.
 *
 * @since 2.0 {@index}
 */
public interface Subpackage {

    /**
     * The name of the subpackage, for example &quot;dom&quot;, &quot;api&quot;, &quot;spi&quot; or &quot;fixtures&quot;.
     */
    String getName();


    /**
     * A list of the (names of the) subpackages where classes in the same module as this package have access.
     *
     * <p>
     * For example, the &quot;dom&quot; subpackage can probably be referenced from the &quot;menu&quot; subpackage,
     * but not vice versa.
     * </p>
     *
     * <p>
     * The special value of &quot;*&quot; is a wildcard meaning that all subpackages (in the same module) can access.
     * </p>
     */
    List<String> mayBeAccessedBySubpackagesInSameModule();

    /**
     * A list of the (names of the) subpackages where classes in the packages of other referencing modules may
     * have access.
     *
     * <p>
     * For example, in some cases the the &quot;dom&quot; subpackage may <i>not</i> be accessible from other
     * modules if the intention is to require all programmatic access through an &quot;api&quot; subpackage
     * (where the classes in <code>dom</code> implement interfaces defined in <code>api</code>).
     * </p>
     *
     * <p>
     * The special value of &quot;*&quot; is a wildcard meaning that all subpackages (in other modules) can access.
     * </p>
     */
    List<String> mayBeAccessedBySubpackagesInReferencingModules();

    default String packageIdentifier() {
        return "." + getName() + "..";
    }
}

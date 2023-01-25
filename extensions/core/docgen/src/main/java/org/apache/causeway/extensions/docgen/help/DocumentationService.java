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
package org.apache.causeway.extensions.docgen.help;

import org.apache.causeway.extensions.docgen.menu.DocumentationMenu;

/**
 * Provides the content for the {@link DocumentationMenu} entries.
 * <p>
 * Currently there is only one, namely (<i>help</i>).
 *
 * @see DocumentationMenu
 * @since 2.x {@index}
 */
public interface DocumentationService {

    /** Returns a view-model or value that represents the application's primary help page. */
    Object getHelp();

}

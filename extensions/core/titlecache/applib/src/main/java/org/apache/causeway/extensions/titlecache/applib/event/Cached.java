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
package org.apache.causeway.extensions.titlecache.applib.event;

import org.apache.causeway.applib.annotation.DomainObjectLayout;

/**
 * For classes whose title is to be cached, use {@link DomainObjectLayout#titleUiEvent()}
 * with a subclass of {@link org.apache.causeway.applib.CausewayModuleApplib.TitleUiEvent}
 * that implements this marker interface.
 *
 * <p>
 *     The cache configuration will by default be taken from
 *     {@link org.apache.causeway.core.config.CausewayConfiguration.Extensions.Titlecache}, but
 *     if the {@link CachedWithCacheSettings} is implemented, then these defaults can be fine-tuned.
 * </p>
 *
 * @see CachedWithCacheSettings
 *
 * @since 2.1 {@index}
 */
public interface Cached {
}

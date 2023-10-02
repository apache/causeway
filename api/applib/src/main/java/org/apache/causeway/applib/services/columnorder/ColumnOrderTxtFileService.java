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
package org.apache.causeway.applib.services.columnorder;

import org.apache.causeway.applib.services.tablecol.TableColumnOrderService;

/**
 * This is a utility service to support the usage of {@link TableColumnOrderService}, providing the ability to obtain
 * a zip of each of the <code>Xxx.columnOrder.txt</code> files for the specified domain object.
 *
 * <p>
 * The zip contains:
 * <ul>
 *     <li>DomainClass.columnOrder.txt</li> - as used for standalone collections of <code>DomainClass</code> itself
 *     <li>DomainClass#collection1.columnOrder.txt</li> - for <code>DomainClass</code>' collection with id <code>collection1</code>.
 *     <li>...</li>
 *     <li>DomainClass#collectionN.columnOrder.txt</li> - for <code>DomainClass</code>' collection with id <code>collectionN</code>.
 * </ul>
 * </p>
 *
 * <p>
 * These should be unzipped and copied in the domain class' package, and then their contents updated to specify the
 * order in which the respective object's properties will be shown in the standalone or parented collections.
 * </p>
 *
 * @see Object_downloadColumnOrderTxtFilesAsZip
 * @see TableColumnOrderService
 *
 * @since 2.0 {@index}
 */
public interface ColumnOrderTxtFileService {

    byte[] toZip(final Object domainObject);

}

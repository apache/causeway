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

/**
 * This package defines a marker {@link org.apache.causeway.applib.snapshot.Snapshottable interface}
 * that indicates that the implementing domain object can be &quot;snapshotted&quot;
 * into an XML format using a utility class provided by the framework.
 *
 * <p>
 * The extent of the data within the XML snapshot can be controlled programmatically,
 * typically by the object creating the snapshot.  If the responsibility for knowing
 * what should go into the XML is actually in the
 * {@link org.apache.causeway.applib.snapshot.Snapshottable}
 * domain object itself, then the {@link org.apache.causeway.applib.snapshot.SnapshottableWithInclusions}
 * interface can be used instead.
 *
 * <p>
 * The utility class that is used to create the snapshot is called
 * <tt>XmlSnapshot</tt>, and resides within the <tt>core.runtime</tt> module.
 * Best practice is to define a <tt>SnapshotService</tt>
 * interface as a domain service, and whose implementation will delegate to the
 * <tt>XmlSnapshot</tt>.  This approach ensures that the domain objects do not
 * have any coupling to the framework.
 */
package org.apache.causeway.applib.snapshot;
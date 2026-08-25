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
 * Provides an SPI for supplying a scheduled message of the day to viewers.
 *
 * <p>A viewer evaluates the candidate on each page render using the framework clock.
 * The message is active from its display start, inclusive, until its display start plus
 * its positive display duration, exclusive.</p>
 *
 * <p>The message title is plain text.
 * Its detail is trusted HTML that the framework does not sanitize, so providers that
 * obtain content from an external source are responsible for sanitizing that content.</p>
 */
package org.apache.causeway.applib.services.motd;

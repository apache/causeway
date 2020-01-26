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
 * The {@link org.apache.isis.applib.services.i18n.TranslationService} is the cornerstone of Apache Isis' i18n support.
 * Its role is to be able to provide translated versions of the various elements within the Apache Isis metamodel
 * (service and object classes, properties, collections, actions, action parameters) and also to translate business
 * rule (disable/valid) messages, and exceptions. These translations provide for both singular and plural forms.
 *
 *
 */
package org.apache.isis.applib.services.i18n;
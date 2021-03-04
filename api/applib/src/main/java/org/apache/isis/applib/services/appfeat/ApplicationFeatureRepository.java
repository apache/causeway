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
package org.apache.isis.applib.services.appfeat;

import java.util.Map;
import java.util.SortedSet;

/**
 * Provides the access to string representations of the packages, classes and
 * class members (collectively: "application features") of the domain classes
 * within the framework's internal metamodel.
 *
 * @since 1.x revised for 2.0 {@index}
 */
public interface ApplicationFeatureRepository  {

    //TODO[2553] why return String, when AppFeatId is now API
    SortedSet<String> namespaceNames();

//    SortedSet<String> namespaceNamesContainingSort(
//            ApplicationMemberSort memberSort);

    //TODO[2553] remove, only used by orphaned search, which can be done much simpler
    SortedSet<String> classNamesContainedIn(
            String packageFqn,
            ApplicationMemberSort memberSort);

    //TODO[2553] remove or rename->logicalTypeSimpleNamesRecursivelyContainedInNamespace
    SortedSet<String> classNamesRecursivelyContainedIn(
            String packageFqn);

    //TODO[2553] why return String, when AppFeatId is now API
    SortedSet<String> memberNamesOf(
            String packageFqn,
            String className,
            ApplicationMemberSort memberSort);

    
    Map<String, ApplicationFeatureId> getFeatureIdentifiersByName();

}

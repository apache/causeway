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
package org.apache.isis.applib.graph;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.DomainObject;

import lombok.Value;

/**
 * Fundamental building block for graph structures.
 *
 * @since 2.0 {@index}
 *
 * @param <T> type constraint for values contained by this edge's vertices
 */
@Value(staticConstructor = "of")
@DomainObject(logicalTypeName = IsisModuleApplib.NAMESPACE + ".graph.SimpleEdge")
public class SimpleEdge<T> implements Edge<T> {

    Vertex<T> from;
    Vertex<T> to;

}

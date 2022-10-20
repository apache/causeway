/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.core.metamodel.util.pchain;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

/**
 * Represents a unidirectionally linked ordered set of POJOs (chain), where the chain
 * starts at startNode. Each subsequent node is linked via de-referencing a
 * singular field (or no-arg method) that is annotated with {@code @Parent}.
 * <br/>
 *
 * startNode --@Parent--&gt; node2 --@Parent--&gt; node3 ...
 *
 * @since 2.0
 *
 */
public interface ParentChain {

    public static ParentChain of(SpecificationLoader specLoader){
        return new ParentChainDefault(specLoader);
    }

    /**
     * Returns the parent node of this {@code node} or {@code null} if {@code node} has no parent.
     * @param node
     */
    public Object parentOf(Object node);

    /**
     * Returns a Stream of nodes that are chained together by parent references. <br/>
     * The {@code startNode} is excluded from the Stream.  <br/><br/>
     * The chain stops either because there is no more resolvable parent,<br/>
     * or we reached the {@code maxChainLength},<br/>
     * or we reached a node that is already part of the chain.
     *
     * @param startNode
     * @param maxChainLength maximum length of the chain returned
     */
    public default Stream<Object> streamParentChainOf(Object startNode, int maxChainLength){
        final Set<Object> chain = new LinkedHashSet<>();

        Object next = startNode;

        chain.add(startNode); // for infinite loop detection

        while((next = parentOf(next))!=null) {
            final boolean doContinue = chain.add(next); // stops if the we get to a node we already traversed before
            if(!doContinue)
                break;
            if(chain.size()>=maxChainLength)
                break;
        }

        return chain.stream().skip(1);
    }


}

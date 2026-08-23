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
package demoapp.dom.progmodel.mixins;

import java.util.ArrayList;
import java.util.List;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Collection                                 // <.>
@CollectionLayout(paged = 10)
@RequiredArgsConstructor                    // <.>
public class CountHolder_fibonacciSequence {

    private final CountHolder holder;       // <.>

    public List<FibonacciNumberVm> coll() {
        var collection = new ArrayList<FibonacciNumberVm>();

        final int count = holder.getCount();
        for (int i = 0; i < count; i++) {
            final int n = i + 1;
            collection.add(FibonacciNumberVm.of(n, fibonacciFor(n), holder));
        }

        return collection;
    }
    private static int fibonacciFor(final int n) {
        // ...
//end::class[]
        if(n <= 1) {
            return n;
        }
        int fib = 1;
        int prevFib = 1;

        for(int i=2; i<n; i++) {
            int temp = fib;
            fib+= prevFib;
            prevFib = temp;
        }
        return fib;
//tag::class[]
    }
}
//end::class[]

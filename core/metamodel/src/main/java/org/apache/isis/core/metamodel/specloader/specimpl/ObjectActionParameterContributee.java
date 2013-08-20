/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;


public interface ObjectActionParameterContributee extends ObjectActionParameter {

    public static class Util {
        
        static <T> List<T> toList(final T[] inputArray) {
            return inputArray != null? Arrays.asList(inputArray): Collections.<T>emptyList();
        }

        static <T> List<T> update(List<T> input, final int requiredLength, final int index, final T elementAtIndex) {
            List<T> output;
            if(input.isEmpty()) {
                // nothing provided, so just create an empty list of the required length
                output = newList(requiredLength);
            } else if(input.size() == requiredLength) {
                // exactly correct length, so just use
                output = input;
            } else if(input.size() == requiredLength - 1) {
                // one short, so split the input at the index 
                output = Lists.newArrayList();
                output.addAll(input.subList(0, index));
                output.add(null); 
                output.addAll(input.subList(index, input.size()));
            } else {
                throw new IllegalArgumentException("Provided " + input.size() + " args for method taking " + requiredLength + " parameters");
            }

            // finally, overwrite the element
            output.set(index, elementAtIndex);
            return output;
        }

        static <T> List<T> newList(final int requiredLength) {
            List<T> output;
            output = Lists.newArrayList();
            for(int i=0; i<requiredLength; i++) {
                output.add(null);
            }
            return output;
        }
    }
}

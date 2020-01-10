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
package org.apache.isis.unittestsupport;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import org.apache.isis.commons.internal._Constants;
import org.apache.isis.unittestsupport.utils.IndentPrinter;

/**
 * Provides some basic infrastructure to iterate over all entity types and
 * apply some contract test.
 */
public abstract class AbstractApplyToAllContractTest {

    protected IndentPrinter out;

    protected AbstractApplyToAllContractTest(
            final String packagePrefix) {
        out = new IndentPrinter(_Constants.nopWriter);
    }

    public AbstractApplyToAllContractTest withLoggingTo(Writer out) {
        this.out = new IndentPrinter(out);
        return this;
    }

    public AbstractApplyToAllContractTest withLoggingTo(PrintStream out) {
        this.out = new IndentPrinter(new PrintWriter(out));
        return this;
    }

    @Test
    public void searchAndTest() throws Exception {

        Set<Class<?>> entityTypes =
                new TreeSet<>(new Comparator<Class<?>>() {
                    @Override
                    public int compare(Class<?> o1, Class<?> o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
        entityTypes.addAll(findTypes());

        for (Class<?> entityType : entityTypes) {
            out.println(entityType.getName());
            out.incrementIndent();
            try {
                applyContractTest(entityType);
            } finally {
                out.decrementIndent();
            }
        }
        out.println("DONE");
    }

    /**
     * By default, finds nothing.
     *
     * <p>
     * Can be overridden if need be.
     */
    protected Set<Class<?>> findTypes() {
        return Collections.emptySet();
    }

    protected abstract void applyContractTest(Class<?> entityType);



}

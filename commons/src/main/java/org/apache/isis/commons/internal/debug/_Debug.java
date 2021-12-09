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
package org.apache.isis.commons.internal.debug;

import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.debug.xray.XrayUi;

import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Utility for adding temporary debug code,
 * that needs to be removed later. Also integrates with {@link XrayUi}, if enabled.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0
 *
 * @deprecated not deprecated, but marked a such,
 * to indicate that any call to this class is temporary for debugging purposes
 * and should be removed ultimately
 */
@Deprecated // do not remove, see java-doc
@UtilityClass
public class _Debug {

    public void onCondition(
            final boolean condition,
            final Runnable runnable) {

        if(condition) {
            runnable.run();
        }
    }

    public void onClassSimpleNameMatch(
            final Class<?> correspondingClass,
            final String classSimpleName,
            final Runnable runnable) {
        onCondition(correspondingClass.getSimpleName().equals(classSimpleName), runnable);
    }

    public void dump(final Object x) {
        dump(x, 0);
    }

    /**
     * General purpose log entry.
     */
    public void log(final String format, final Object...args) {
        _XrayEvent.record(1, _IconResource.LOG, format, args);
    }

    // -- HELPER

    private void dump(Object x, final int indent) {
        if(x instanceof Iterable) {
            _NullSafe.streamAutodetect(x)
            .forEach(element->dump(element, indent+1));
            return;
        }
        if(x!=null
                && x.getClass().isArray()) {

            val array = _NullSafe.streamAutodetect(x)
            .map(e->""+e)
            .collect(Collectors.joining(", "));

            x = String.format("[%s]", array);
        }

        if(indent==0) {
            System.err.printf("%s%n", x);
        } else {
            val suffix = _Strings.padEnd("", indent, '-');
            System.err.printf("%s %s%n", suffix, x);
        }

    }

}

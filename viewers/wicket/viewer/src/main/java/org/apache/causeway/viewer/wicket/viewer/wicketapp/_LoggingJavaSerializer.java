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
package org.apache.causeway.viewer.wicket.viewer.wicketapp;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.wicket.serialize.java.JavaSerializer;

import org.apache.causeway.commons.internal.base._Timing;
import org.apache.causeway.commons.internal.reflection._Reflect;

import lombok.SneakyThrows;

/**
 * For debugging page serialization.
 *
 * <p>requires
 * <pre>--add-opens java.base/java.io=ALL-UNNAMED<pre>
 *
 * <p>usage within init() ...
 * <pre>getFrameworkSettings().setSerializer(new _LoggingJavaSerializer(getApplicationKey()));
 */
class _LoggingJavaSerializer extends JavaSerializer {

    public _LoggingJavaSerializer(final String applicationKey) {
        super(applicationKey);
    }

    // ObjectOutputStream variant, that reflectively calls writeObject0 private to the super class, for logging purposes.
    static class MyObjectOutputStream extends ObjectOutputStream {
        MyObjectOutputStream(final OutputStream outputStream) throws IOException {
            super(outputStream);
        }
        @Override
        protected final void writeObjectOverride(final Object obj) throws java.io.IOException {
            if(obj instanceof String str) {
                System.err.printf(">> \"%s\"%n", str);
            } else {
                System.err.printf(">> %s(%s)%n", obj.getClass().getSimpleName(), Integer.toHexString(System.identityHashCode(obj)));
            }
            superWriteObject0(obj, false);
        }

        @SneakyThrows
        private void enableOverride(final boolean ena) {
            _Reflect.setFieldOn(ObjectOutputStream.class.getDeclaredField("enableOverride"), this, ena);
        }
        @SneakyThrows
        private void superWriteObject0(final Object... args) {
            _Reflect.invokeMethodOn(ObjectOutputStream.class.getDeclaredMethod("writeObject0", new Class[] {Object.class, boolean.class}), this, args);
        }
    }

    @Override
    public byte[] serialize(final Object object) {
        var watch = _Timing.now();
        var bytes = super.serialize(object);
        watch.stop();
        System.err.printf("> %s (%d kB %s)%n", object.getClass().getSimpleName(), bytes.length/1000, watch);
        return bytes;
    }

    @Override
    public Object deserialize(final byte[] data) {
        var obj = super.deserialize(data);
        System.err.printf("< %s%n", obj.getClass().getSimpleName());
        return obj;
    }

    @Override
    protected final java.io.ObjectOutputStream newObjectOutputStream(final java.io.OutputStream out) throws java.io.IOException {
        return new MyObjectOutputStream(out);
    }

}

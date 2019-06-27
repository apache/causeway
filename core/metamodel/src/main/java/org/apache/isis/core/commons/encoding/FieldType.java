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

package org.apache.isis.core.commons.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.encoding.DataInputExtended;
import org.apache.isis.commons.internal.encoding.DataOutputExtended;
import org.apache.isis.commons.internal.encoding.Encodable;

import lombok.extern.log4j.Log4j2;

/**
 * Typesafe writing and reading of fields, providing some level of integrity
 * checking of encoded messages.
 *
 * <p>
 * The {@link #write(DataOutputExtended, Object)} writes out field type and then
 * the data for that field type. The field type is represented by this
 * enumberation, with the {@link FieldType#getIdx() index} being what is written
 * to the stream (hence of type <tt>byte</tt> to keep small).
 *
 * <p>
 * Conversely, the {@link #read(DataInputExtended)} reads the field type and
 * then the data for that field type.
 */
@Log4j2
public abstract class FieldType<T> {

    private static String LOG_INDENT = ". . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . ";
    private static final int NULL_BIT = 64; // 2 to the 6

    private static Map<Byte, FieldType<?>> cache = new HashMap<Byte, FieldType<?>>();
    private static int next = 0;

    static enum Indenting {
        INDENT_ONLY, INDENT_AND_OUTDENT;
    }

    public static FieldType<Boolean> BOOLEAN = new FieldType<Boolean>((byte) next++, Boolean.class, Indenting.INDENT_ONLY) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Boolean value) throws IOException {
            try {
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeBoolean(value);
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected Boolean doRead(final DataInputExtended input) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final boolean value = inputStream.readBoolean();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                return value;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<boolean[]> BOOLEAN_ARRAY = new FieldType<boolean[]>((byte) next++, boolean[].class, Indenting.INDENT_AND_OUTDENT) {
        @Override
        protected void doWrite(final DataOutputExtended output, final boolean[] values) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeInt(values.length);
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(values.length);
                }
                for (int i = 0; i < values.length; i++) {
                    outputStream.writeBoolean(values[i]);
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected boolean[] doRead(final DataInputExtended input) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataInputStream inputStream = input.getDataInputStream();
                final int length = inputStream.readInt();
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(length);
                }
                final boolean[] values = new boolean[length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = inputStream.readBoolean();
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
                return values;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<Byte> BYTE = new FieldType<Byte>((byte) next++, Byte.class, Indenting.INDENT_ONLY) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Byte value) throws IOException {
            try {
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeByte(value.byteValue());
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected Byte doRead(final DataInputExtended input) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final byte value = inputStream.readByte();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                return value;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<byte[]> BYTE_ARRAY = new FieldType<byte[]>((byte) next++, byte[].class, Indenting.INDENT_AND_OUTDENT) {
        @Override
        protected void doWrite(final DataOutputExtended output, final byte[] values) throws IOException {
            try {
                final DataOutputStream outputStream = output.getDataOutputStream();
                final int length = values.length;
                outputStream.writeInt(length);
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append("length:").append(length).append(" [BYTE ARRAY]"));
                }

                // rather than looping through the array,
                // we take advantage of optimization built into DataOutputStream
                outputStream.write(values);
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected byte[] doRead(final DataInputExtended input) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final int length = inputStream.readInt();
                if (log.isDebugEnabled()) {
                    final StringBuilder msg = new StringBuilder().append("length:").append(length).append(" [BYTE ARRAY]");
                    log(this, msg);
                }

                final byte[] bytes = new byte[length];
                readBytes(inputStream, bytes);
                return bytes;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        // rather than looping through the array,
        // we take advantage of optimization built into DataInputStream
        private void readBytes(final DataInputStream inputStream, final byte[] bytes) throws IOException {
            inputStream.read(bytes);
        }
    };

    public static FieldType<Short> SHORT = new FieldType<Short>((byte) next++, Short.class, Indenting.INDENT_ONLY) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Short value) throws IOException {
            try {
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeShort(value.shortValue());
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected Short doRead(final DataInputExtended input) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final short value = inputStream.readShort();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                return value;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<short[]> SHORT_ARRAY = new FieldType<short[]>((byte) next++, short[].class, Indenting.INDENT_AND_OUTDENT) {
        @Override
        protected void doWrite(final DataOutputExtended output, final short[] values) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeInt(values.length);
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(values.length);
                }

                for (int i = 0; i < values.length; i++) {
                    outputStream.writeShort(values[i]);
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected short[] doRead(final DataInputExtended input) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataInputStream inputStream = input.getDataInputStream();
                final int length = inputStream.readInt();
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(length);
                }

                final short[] values = new short[length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = inputStream.readShort();
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
                return values;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<Integer> INTEGER = new FieldType<Integer>((byte) next++, Integer.class, Indenting.INDENT_ONLY) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Integer value) throws IOException {
            try {
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeInt(value.intValue());
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected Integer doRead(final DataInputExtended input) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final int value = inputStream.readInt();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                return value;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<Integer> UNSIGNED_BYTE = new FieldType<Integer>((byte) next++, Integer.class, Indenting.INDENT_ONLY) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Integer value) throws IOException {
            try {
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeByte(value);
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected Integer doRead(final DataInputExtended input) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final int value = inputStream.readUnsignedByte();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                return value;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<Integer> UNSIGNED_SHORT = new FieldType<Integer>((byte) next++, Integer.class, Indenting.INDENT_ONLY) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Integer value) throws IOException {
            try {
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeShort(value);
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected Integer doRead(final DataInputExtended input) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final int value = inputStream.readUnsignedShort();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                return value;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<int[]> INTEGER_ARRAY = new FieldType<int[]>((byte) next++, int[].class, Indenting.INDENT_AND_OUTDENT) {
        @Override
        protected void doWrite(final DataOutputExtended output, final int[] values) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeInt(values.length);
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(values.length);
                }

                for (int i = 0; i < values.length; i++) {
                    outputStream.writeInt(values[i]);
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected int[] doRead(final DataInputExtended input) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataInputStream inputStream = input.getDataInputStream();
                final int length = inputStream.readInt();
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(length);
                }

                final int[] values = new int[length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = inputStream.readInt();
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
                return values;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<Long> LONG = new FieldType<Long>((byte) next++, Long.class, Indenting.INDENT_ONLY) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Long value) throws IOException {
            try {
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeLong(value.intValue());
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected Long doRead(final DataInputExtended input) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final long value = inputStream.readLong();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                return value;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };
    public static FieldType<long[]> LONG_ARRAY = new FieldType<long[]>((byte) next++, long[].class, Indenting.INDENT_AND_OUTDENT) {
        @Override
        protected void doWrite(final DataOutputExtended output, final long[] values) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeInt(values.length);
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(values.length);
                }

                for (int i = 0; i < values.length; i++) {
                    outputStream.writeLong(values[i]);
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected long[] doRead(final DataInputExtended input) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();

                final DataInputStream inputStream = input.getDataInputStream();
                final int length = inputStream.readInt();
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(length);
                }

                final long[] values = new long[length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = inputStream.readLong();
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
                return values;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<Character> CHAR = new FieldType<Character>((byte) next++, Character.class, Indenting.INDENT_ONLY) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Character value) throws IOException {
            try {
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeLong(value.charValue());
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected Character doRead(final DataInputExtended input) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final char value = inputStream.readChar();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                return value;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<char[]> CHAR_ARRAY = new FieldType<char[]>((byte) next++, char[].class, Indenting.INDENT_AND_OUTDENT) {
        // TODO: could perhaps optimize by writing out as a string
        @Override
        protected void doWrite(final DataOutputExtended output, final char[] values) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeInt(values.length);
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(values.length);
                }

                for (int i = 0; i < values.length; i++) {
                    outputStream.writeChar(values[i]);
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected char[] doRead(final DataInputExtended input) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataInputStream inputStream = input.getDataInputStream();
                final int length = inputStream.readInt();
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(length);
                }

                final char[] values = new char[length];
                for (int i = 0; i < values.length; i++) {
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                    values[i] = inputStream.readChar();
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
                return values;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<Float> FLOAT = new FieldType<Float>((byte) next++, Float.class, Indenting.INDENT_ONLY) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Float value) throws IOException {
            try {
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeFloat(value);
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected Float doRead(final DataInputExtended input) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final float value = inputStream.readFloat();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                return value;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<float[]> FLOAT_ARRAY = new FieldType<float[]>((byte) next++, float[].class, Indenting.INDENT_AND_OUTDENT) {
        @Override
        protected void doWrite(final DataOutputExtended output, final float[] values) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeInt(values.length);
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(values.length);
                }

                for (int i = 0; i < values.length; i++) {
                    outputStream.writeFloat(values[i]);
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected float[] doRead(final DataInputExtended input) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataInputStream inputStream = input.getDataInputStream();
                final int length = inputStream.readInt();
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(length);
                }

                final float[] values = new float[length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = inputStream.readFloat();
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
                return values;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<Double> DOUBLE = new FieldType<Double>((byte) next++, Double.class, Indenting.INDENT_ONLY) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Double value) throws IOException {
            try {
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeDouble(value);
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected Double doRead(final DataInputExtended input) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final double value = inputStream.readDouble();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                return value;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<double[]> DOUBLE_ARRAY = new FieldType<double[]>((byte) next++, double[].class, Indenting.INDENT_AND_OUTDENT) {
        @Override
        protected void doWrite(final DataOutputExtended output, final double[] values) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeInt(values.length);
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(values.length);
                }

                for (int i = 0; i < values.length; i++) {
                    outputStream.writeDouble(values[i]);
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected double[] doRead(final DataInputExtended input) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataInputStream inputStream = input.getDataInputStream();
                final int length = inputStream.readInt();
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(length);
                }

                final double[] values = new double[length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = inputStream.readDouble();
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
                return values;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<String> STRING = new FieldType<String>((byte) next++, String.class, Indenting.INDENT_ONLY) {
        @Override
        protected void doWrite(final DataOutputExtended output, final String value) throws IOException {
            try {
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeUTF(value);
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected String doRead(final DataInputExtended input) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final String value = inputStream.readUTF();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(value));
                }
                return value;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };
    public static FieldType<String[]> STRING_ARRAY = new FieldType<String[]>((byte) next++, String[].class, Indenting.INDENT_AND_OUTDENT) {
        @Override
        protected void doWrite(final DataOutputExtended output, final String[] values) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeInt(values.length);
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(values.length);
                }

                for (int i = 0; i < values.length; i++) {
                    // using FieldType to write out takes care of null handling
                    FieldType.STRING.write(output, values[i]);
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected String[] doRead(final DataInputExtended input) throws IOException {
            try {
                final StringBuilder buf = new StringBuilder();
                final DataInputStream inputStream = input.getDataInputStream();
                final int length = inputStream.readInt();
                if (log.isDebugEnabled()) {
                    buf.append("length: ").append(length);
                }

                final String[] values = new String[length];
                for (int i = 0; i < values.length; i++) {
                    // using FieldType to read in takes care of null handling
                    values[i] = FieldType.STRING.read(input);
                    if (log.isDebugEnabled()) {
                        buf.append(i == 0 ? ": " : ", ");
                        buf.append(values[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log(this, buf);
                }
                return values;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }
    };

    public static FieldType<Encodable> ENCODABLE = new FieldType<Encodable>((byte) next++, Encodable.class, Indenting.INDENT_AND_OUTDENT) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Encodable encodable) throws IOException {
            try {
                // write out class
                final String className = encodable.getClass().getName();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(className));
                }
                output.writeUTF(className);

                // recursively encode
                encodable.encode(output);
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected Encodable doRead(final DataInputExtended input) throws IOException {
            try {
                // read in class name ...
                final String className = input.readUTF();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append(className));
                }

                Class<?> cls;
                try {
                    // ...obtain constructor
                    cls = _Context.loadClass(className);

                    final Constructor<?> constructor = cls.getConstructor(new Class[] { DataInputExtended.class });

                    // recursively decode
                    return (Encodable) constructor.newInstance(new Object[] { input });
                } catch (final ClassNotFoundException ex) {
                    throw new FailedToDecodeException(ex);
                } catch (final IllegalArgumentException ex) {
                    throw new FailedToDecodeException(ex);
                } catch (final InstantiationException ex) {
                    throw new FailedToDecodeException(ex);
                } catch (final IllegalAccessException ex) {
                    throw new FailedToDecodeException(ex);
                } catch (final InvocationTargetException ex) {
                    throw new FailedToDecodeException(ex);
                } catch (final SecurityException ex) {
                    throw new FailedToDecodeException(ex);
                } catch (final NoSuchMethodException ex) {
                    throw new FailedToDecodeException(ex);
                }

            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected boolean checksStream() {
            return false;
        }
    };

    public static FieldType<Encodable[]> ENCODABLE_ARRAY = new FieldType<Encodable[]>((byte) next++, Encodable[].class, Indenting.INDENT_AND_OUTDENT) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Encodable[] values) throws IOException {
            try {
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeInt(values.length);
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append("length: ").append(values.length));
                }
                for (final Encodable encodable : values) {
                    // using FieldType to write out takes care of null handling
                    FieldType.ENCODABLE.write(output, encodable);
                }
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected <Q> Q[] doReadArray(final DataInputExtended input, final Class<Q> elementType) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final int length = inputStream.readInt();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append("length: ").append(length));
                }

                final Q[] values = (Q[]) Array.newInstance(elementType, length);
                for (int i = 0; i < values.length; i++) {
                    // using FieldType to read in takes care of null handling
                    values[i] = (Q) FieldType.ENCODABLE.read(input);
                }
                return values;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected boolean checksStream() {
            return false;
        }
    };

    public static FieldType<Serializable> SERIALIZABLE = new FieldType<Serializable>((byte) next++, Serializable.class, Indenting.INDENT_ONLY) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Serializable value) throws IOException {
            try {
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append("[SERIALIZABLE]"));
                }

                // write out as blob of bytes
                final ObjectOutputStream oos = new ObjectOutputStream(output.getDataOutputStream());
                oos.writeObject(value);
                oos.flush();
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected Serializable doRead(final DataInputExtended input) throws IOException {
            try {
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append("[SERIALIZABLE]"));
                }

                // read in a blob of bytes
                final ObjectInputStream ois = new ObjectInputStream(input.getDataInputStream());
                try {
                    return (Serializable) ois.readObject();
                } catch (final ClassNotFoundException ex) {
                    throw new FailedToDeserializeException(ex);
                }
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected boolean checksStream() {
            return false;
        }
    };

    public static FieldType<Serializable[]> SERIALIZABLE_ARRAY = new FieldType<Serializable[]>((byte) next++, Serializable[].class, Indenting.INDENT_AND_OUTDENT) {
        @Override
        protected void doWrite(final DataOutputExtended output, final Serializable[] values) throws IOException {
            try {
                final DataOutputStream outputStream = output.getDataOutputStream();
                outputStream.writeInt(values.length);
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append("length: ").append(values.length));
                }

                for (final Serializable value : values) {
                    // using FieldType to write out takes care of null handling
                    FieldType.SERIALIZABLE.write(output, value);
                }
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        protected <Q> Q[] doReadArray(final DataInputExtended input, final Class<Q> elementType) throws IOException {
            try {
                final DataInputStream inputStream = input.getDataInputStream();
                final int length = inputStream.readInt();
                if (log.isDebugEnabled()) {
                    log(this, new StringBuilder().append("length: ").append(length));
                }

                final Q[] values = (Q[]) Array.newInstance(elementType, length);
                for (int i = 0; i < values.length; i++) {
                    // using FieldType to read in takes care of null handling
                    values[i] = (Q) FieldType.SERIALIZABLE.read(input);
                }
                return values;
            } finally {
                if (log.isDebugEnabled()) {
                    unlog(this);
                }
            }
        }

        @Override
        protected boolean checksStream() {
            return false;
        }
    };

    public static FieldType<?> get(final byte idx) {
        return cache.get(idx);
    }

    private final byte idx;
    private final Class<T> cls;
    private final Indenting indenting;

    private FieldType(final byte idx, final Class<T> cls, final Indenting indenting) {
        this.idx = idx;
        this.cls = cls;
        this.indenting = indenting;
        cache.put(idx, this);
    }

    public byte getIdx() {
        return idx;
    }

    public Class<T> getCls() {
        return cls;
    }

    /**
     * Whether this implementation checks ordering in the stream.
     *
     * <p>
     * Broadly, the type safe ones do, the {@link Encodable} and
     * {@link Serializable} ones do not.
     */
    protected boolean checksStream() {
        return true;
    }

    public final T read(final DataInputExtended input) throws IOException {
        final DataInputStream inputStream = input.getDataInputStream();
        final byte fieldTypeIdxAndNullability = inputStream.readByte();

        final boolean isNull = fieldTypeIdxAndNullability >= NULL_BIT;
        final byte fieldTypeIdx = (byte) (fieldTypeIdxAndNullability - (isNull ? NULL_BIT : 0));
        try {
            final FieldType<?> fieldType = FieldType.get(fieldTypeIdx);
            if (fieldType == null || (fieldType.checksStream() && fieldType != this)) {
                throw new IllegalStateException("Mismatch in stream: expected " + this + " but got " + fieldType + " (" + fieldTypeIdx + ")");
            }

            if (isNull && log.isDebugEnabled()) {
                // only log if reading a null; otherwise actual value read
                // logged later
                log(this, new StringBuilder().append("(null)"));
            }

            if (isNull) {
                return null;
            } else {
                return doRead(input);
            }
        } finally {
            if (isNull && log.isDebugEnabled()) {
                // only unlog if reading a null
                unlog(this);
            }
        }
    }

    public final <Q> Q[] readArray(final DataInputExtended input, final Class<Q> elementType) throws IOException {
        final DataInputStream inputStream = input.getDataInputStream();
        final byte fieldTypeIdxAndNullability = inputStream.readByte();

        final boolean isNull = fieldTypeIdxAndNullability >= NULL_BIT;
        final byte fieldTypeIdx = (byte) (fieldTypeIdxAndNullability - (isNull ? NULL_BIT : 0));
        try {
            final FieldType<?> fieldType = FieldType.get(fieldTypeIdx);
            if (fieldType.checksStream() && fieldType != this) {
                throw new IllegalStateException("Mismatch in stream: expected " + this + " but got " + fieldType);
            }

            if (isNull && log.isDebugEnabled()) {
                // only log if reading a null; otherwise actual value read
                // logged later
                log(this, new StringBuilder().append("(null)"));
            }

            if (isNull) {
                return null;
            } else {
                return doReadArray(input, elementType);
            }

        } finally {
            if (isNull && log.isDebugEnabled()) {
                // only unlog if reading a null
                unlog(this);
            }
        }

    }

    public final void write(final DataOutputExtended output, final T value) throws IOException {
        byte fieldTypeIdxAndNullability = getIdx();
        final boolean isNull = value == null;
        if (isNull) {
            // set high order bit
            fieldTypeIdxAndNullability += NULL_BIT;
        }
        try {

            final DataOutputStream outputStream = output.getDataOutputStream();

            outputStream.write(fieldTypeIdxAndNullability);
            if (isNull && log.isDebugEnabled()) {
                // only log if writing a null; otherwise actual value logged
                // later
                log(this, new StringBuilder().append("(null)"));
            }

            if (!isNull) {
                doWrite(output, value);
            }
        } finally {
            if (isNull && log.isDebugEnabled()) {
                // only unlog if writing a null
                unlog(this);
            }
        }
    }

    protected T doRead(final DataInputExtended input) throws IOException {
        throw new UnsupportedOperationException("not supported for this field type");
    }

    protected <Q> Q[] doReadArray(final DataInputExtended input, final Class<Q> elementType) throws IOException {
        throw new UnsupportedOperationException("not supported for this field type");
    }

    protected abstract void doWrite(DataOutputExtended output, T value) throws IOException;

    private boolean isIndentingAndOutdenting() {
        return indenting == Indenting.INDENT_AND_OUTDENT;
    }

    // ///////////////////////////////////////////////////////
    // debugging
    // ///////////////////////////////////////////////////////

    private static ThreadLocal<int[]> debugIndent = new ThreadLocal<int[]>();

    private static void log(final FieldType<?> fieldType, final StringBuilder buf) {
        buf.insert(0, ": ");
        buf.insert(0, fieldType);
        if (fieldType.isIndentingAndOutdenting()) {
            buf.insert(0, "> ");
        }
        buf.insert(0, spaces(currentDebugLevel()));
        incrementDebugLevel();
        log.debug(buf.toString());
    }

    private static void unlog(final FieldType<?> fieldType) {
        unlog(fieldType, new StringBuilder());
    }

    private static void unlog(final FieldType<?> fieldType, final StringBuilder buf) {
        if (fieldType.isIndentingAndOutdenting()) {
            buf.insert(0, "< ");
        }
        decrementDebugLevel();
        if (fieldType.isIndentingAndOutdenting()) {
            buf.insert(0, spaces(currentDebugLevel()));
            log.debug(buf.toString());
        }
    }

    private static String spaces(final int num) {
        return LOG_INDENT.substring(0, num);
    }

    private static int currentDebugLevel() {
        return debugIndent()[0];
    }

    private static void incrementDebugLevel() {
        final int[] indentLevel = debugIndent();
        indentLevel[0] += 2;
    }

    private static void decrementDebugLevel() {
        final int[] indentLevel = debugIndent();
        indentLevel[0] -= 2;
    }

    private static int[] debugIndent() {
        int[] indentLevel = debugIndent.get();
        if (indentLevel == null) {
            indentLevel = new int[1];
            debugIndent.set(indentLevel);
        }
        return indentLevel;
    }

    // ///////////////////////////////////////////////////////
    // toString
    // ///////////////////////////////////////////////////////

    @Override
    public String toString() {
        return getCls().getSimpleName();
    }

}

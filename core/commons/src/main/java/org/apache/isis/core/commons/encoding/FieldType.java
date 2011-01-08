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

import org.apache.log4j.Logger;

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
 * Conversely, the {@link #read(DataInputExtended)} reads the field type
 * and then the data for that field type.
 */
public abstract class FieldType<T> {

	private static Logger LOG = Logger.getLogger(FieldType.class);

	private static String LOG_INDENT = ". . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . ";
	private static final int NULL_BIT = 64; // 2 to the 6

	private static Map<Byte, FieldType<?>> cache = new HashMap<Byte, FieldType<?>>();
	private static int next=0;

	static enum Indenting {
		INDENT_ONLY,
		INDENT_AND_OUTDENT;
	}

	public static FieldType<Boolean> BOOLEAN = new FieldType<Boolean>((byte) next++, Boolean.class, Indenting.INDENT_ONLY) {
		@Override
		protected void doWrite(DataOutputExtended output, Boolean value)
				throws IOException {
			try {
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeBoolean(value);
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected Boolean doRead(DataInputExtended input)
				throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				boolean value = inputStream.readBoolean();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				return value;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<boolean[]> BOOLEAN_ARRAY = new FieldType<boolean[]>((byte) next++, boolean[].class, Indenting.INDENT_AND_OUTDENT) {
		@Override
		protected void doWrite(DataOutputExtended output, boolean[] values)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeInt(values.length);
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(values.length);
				}
				for (int i = 0; i < values.length; i++) {
					outputStream.writeBoolean(values[i]);
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected boolean[] doRead(DataInputExtended input)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataInputStream inputStream = input.getDataInputStream();
				int length = inputStream.readInt();
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(length);
				}
				boolean[] values = new boolean[length];
				for (int i = 0; i < values.length; i++) {
					values[i] = inputStream.readBoolean();
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
				return values;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<Byte> BYTE = new FieldType<Byte>((byte) next++, Byte.class, Indenting.INDENT_ONLY) {
		@Override
		protected void doWrite(DataOutputExtended output, Byte value)
				throws IOException {
			try {
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeByte(value.byteValue());
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected Byte doRead(DataInputExtended input)
				throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				byte value = inputStream.readByte();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				return value;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<byte[]> BYTE_ARRAY = new FieldType<byte[]>((byte) next++, byte[].class, Indenting.INDENT_AND_OUTDENT) {
		@Override
		protected void doWrite(DataOutputExtended output, byte[] values)
				throws IOException {
			try {
				DataOutputStream outputStream = output.getDataOutputStream();
				int length = values.length;
				outputStream.writeInt(length);
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append("length:").append(length).append(" [BYTE ARRAY]"));
				}

				// rather than looping through the array,
				// we take advantage of optimization built into DataOutputStream
				outputStream.write(values);
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected byte[] doRead(DataInputExtended input)
				throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				int length = inputStream.readInt();
				if (LOG.isDebugEnabled()) {
					StringBuilder msg = new StringBuilder().append("length:").append(length).append(" [BYTE ARRAY]");
                    log(this, msg);
				}

				byte[] bytes = new byte[length];
				readBytes(inputStream, bytes);
				return bytes;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}

        // rather than looping through the array,
        // we take advantage of optimization built into DataInputStream
		@edu.umd.cs.findbugs.annotations.SuppressWarnings("RR_NOT_CHECKED")
        private void readBytes(DataInputStream inputStream, byte[] bytes) throws IOException {
            inputStream.read(bytes);
        }
	};

	public static FieldType<Short> SHORT = new FieldType<Short>((byte) next++, Short.class, Indenting.INDENT_ONLY) {
		@Override
		protected void doWrite(DataOutputExtended output, Short value)
				throws IOException {
			try {
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeShort(value.shortValue());
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected Short doRead(DataInputExtended input)
				throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				short value = inputStream.readShort();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				return value;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<short[]> SHORT_ARRAY = new FieldType<short[]>((byte) next++, short[].class, Indenting.INDENT_AND_OUTDENT) {
		@Override
		protected void doWrite(DataOutputExtended output, short[] values)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeInt(values.length);
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(values.length);
				}

				for (int i = 0; i < values.length; i++) {
					outputStream.writeShort(values[i]);
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected short[] doRead(DataInputExtended input)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataInputStream inputStream = input.getDataInputStream();
				int length = inputStream.readInt();
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(length);
				}

				short[] values = new short[length];
				for (int i = 0; i < values.length; i++) {
					values[i] = inputStream.readShort();
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
				return values;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<Integer> INTEGER = new FieldType<Integer>((byte) next++, Integer.class, Indenting.INDENT_ONLY) {
		@Override
		protected void doWrite(DataOutputExtended output, Integer value)
				throws IOException {
			try {
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeInt(value.intValue());
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected Integer doRead(DataInputExtended input)
				throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				int value = inputStream.readInt();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				return value;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<Integer> UNSIGNED_BYTE = new FieldType<Integer>((byte) next++, Integer.class, Indenting.INDENT_ONLY) {
		@Override
		protected void doWrite(DataOutputExtended output, Integer value)
				throws IOException {
			try {
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeByte(value);
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected Integer doRead(DataInputExtended input)
				throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				int value = inputStream.readUnsignedByte();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				return value;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<Integer> UNSIGNED_SHORT = new FieldType<Integer>((byte) next++, Integer.class, Indenting.INDENT_ONLY) {
		@Override
		protected void doWrite(DataOutputExtended output, Integer value)
				throws IOException {
			try {
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeShort(value);
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected Integer doRead(DataInputExtended input)
				throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				int value = inputStream.readUnsignedShort();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				return value;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<int[]> INTEGER_ARRAY = new FieldType<int[]>((byte) next++, int[].class, Indenting.INDENT_AND_OUTDENT) {
		@Override
		protected void doWrite(DataOutputExtended output, int[] values)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeInt(values.length);
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(values.length);
				}

				for (int i = 0; i < values.length; i++) {
					outputStream.writeInt(values[i]);
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected int[] doRead(DataInputExtended input)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataInputStream inputStream = input.getDataInputStream();
				int length = inputStream.readInt();
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(length);
				}

				int[] values = new int[length];
				for (int i = 0; i < values.length; i++) {
					values[i] = inputStream.readInt();
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
				return values;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<Long> LONG = new FieldType<Long>((byte) next++, Long.class, Indenting.INDENT_ONLY) {
		@Override
		protected void doWrite(DataOutputExtended output, Long value)
				throws IOException {
			try {
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeLong(value.intValue());
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected Long doRead(DataInputExtended input)
				throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				long value = inputStream.readLong();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				return value;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};
	public static FieldType<long[]> LONG_ARRAY = new FieldType<long[]>((byte) next++, long[].class, Indenting.INDENT_AND_OUTDENT) {
		@Override
		protected void doWrite(DataOutputExtended output, long[] values)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeInt(values.length);
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(values.length);
				}

				for (int i = 0; i < values.length; i++) {
					outputStream.writeLong(values[i]);
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected long[] doRead(DataInputExtended input)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();

				DataInputStream inputStream = input.getDataInputStream();
				int length = inputStream.readInt();
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(length);
				}

				long[] values = new long[length];
				for (int i = 0; i < values.length; i++) {
					values[i] = inputStream.readLong();
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
				return values;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<Character> CHAR = new FieldType<Character>((byte) next++, Character.class, Indenting.INDENT_ONLY) {
		@Override
		protected void doWrite(DataOutputExtended output, Character value)
				throws IOException {
			try {
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeLong(value.charValue());
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected Character doRead(DataInputExtended input)
				throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				char value = inputStream.readChar();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				return value;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<char[]> CHAR_ARRAY = new FieldType<char[]>((byte) next++, char[].class, Indenting.INDENT_AND_OUTDENT) {
		// TODO: could perhaps optimize by writing out as a string
		@Override
		protected void doWrite(DataOutputExtended output, char[] values)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeInt(values.length);
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(values.length);
				}

				for (int i = 0; i < values.length; i++) {
					outputStream.writeChar(values[i]);
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected char[] doRead(DataInputExtended input)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataInputStream inputStream = input.getDataInputStream();
				int length = inputStream.readInt();
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(length);
				}

				char[] values = new char[length];
				for (int i = 0; i < values.length; i++) {
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
					values[i] = inputStream.readChar();
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
				return values;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<Float> FLOAT = new FieldType<Float>((byte) next++, Float.class, Indenting.INDENT_ONLY) {
		@Override
		protected void doWrite(DataOutputExtended output, Float value)
				throws IOException {
			try {
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeFloat(value);
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected Float doRead(DataInputExtended input)
				throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				float value = inputStream.readFloat();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				return value;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<float[]> FLOAT_ARRAY = new FieldType<float[]>((byte) next++, float[].class, Indenting.INDENT_AND_OUTDENT) {
		@Override
		protected void doWrite(DataOutputExtended output, float[] values)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeInt(values.length);
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(values.length);
				}

				for (int i = 0; i < values.length; i++) {
					outputStream.writeFloat(values[i]);
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected float[] doRead(DataInputExtended input)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataInputStream inputStream = input.getDataInputStream();
				int length = inputStream.readInt();
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(length);
				}

				float[] values = new float[length];
				for (int i = 0; i < values.length; i++) {
					values[i] = inputStream.readFloat();
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
				return values;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<Double> DOUBLE = new FieldType<Double>((byte) next++, Double.class, Indenting.INDENT_ONLY) {
		@Override
		protected void doWrite(DataOutputExtended output, Double value)
				throws IOException {
			try {
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeDouble(value);
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected Double doRead(DataInputExtended input)
				throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				double value = inputStream.readDouble();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				return value;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<double[]> DOUBLE_ARRAY = new FieldType<double[]>((byte) next++, double[].class, Indenting.INDENT_AND_OUTDENT) {
		@Override
		protected void doWrite(DataOutputExtended output, double[] values)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeInt(values.length);
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(values.length);
				}

				for (int i = 0; i < values.length; i++) {
					outputStream.writeDouble(values[i]);
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected double[] doRead(DataInputExtended input)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataInputStream inputStream = input.getDataInputStream();
				int length = inputStream.readInt();
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(length);
				}

				double[] values = new double[length];
				for (int i = 0; i < values.length; i++) {
					values[i] = inputStream.readDouble();
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
				return values;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<String> STRING = new FieldType<String>((byte) next++, String.class, Indenting.INDENT_ONLY) {
		@Override
		protected void doWrite(DataOutputExtended output, String value)
				throws IOException {
			try {
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeUTF(value);
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected String doRead(DataInputExtended input)
				throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				String value = inputStream.readUTF();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(value));
				}
				return value;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};
	public static FieldType<String[]> STRING_ARRAY = new FieldType<String[]>((byte) next++, String[].class, Indenting.INDENT_AND_OUTDENT) {
		@Override
		protected void doWrite(DataOutputExtended output, String[] values)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeInt(values.length);
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(values.length);
				}

				for (int i = 0; i < values.length; i++) {
					// using FieldType to write out takes care of null handling
					FieldType.STRING.write(output, values[i]);
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected String[] doRead(DataInputExtended input)
				throws IOException {
			try {
				StringBuilder buf = new StringBuilder();
				DataInputStream inputStream = input.getDataInputStream();
				int length = inputStream.readInt();
				if (LOG.isDebugEnabled()) {
					buf.append("length: ").append(length);
				}

				String[] values = new String[length];
				for (int i = 0; i < values.length; i++) {
					// using FieldType to read in takes care of null handling
					values[i] = FieldType.STRING.read(input);
					if (LOG.isDebugEnabled()) {
						buf.append(i==0?": ":", ");
						buf.append(values[i]);
					}
				}
				if (LOG.isDebugEnabled()) {
					log(this, buf);
				}
				return values;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
	};

	public static FieldType<Encodable> ENCODABLE = new FieldType<Encodable>((byte) next++, Encodable.class, Indenting.INDENT_AND_OUTDENT) {
		@Override
		protected void doWrite(DataOutputExtended output, Encodable encodable) throws IOException {
			try {
				// write out class
				String className = encodable.getClass().getName();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(className));
				}
				output.writeUTF(className);

				// recursively encode
				encodable.encode(output);
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected Encodable doRead(DataInputExtended input) throws IOException {
			try {
				// read in class name ...
				String className = input.readUTF();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append(className));
				}

				Class<?> cls;
				try {
					// ...obtain constructor
					cls = Thread.currentThread().getContextClassLoader().loadClass(className);

					Constructor<?> constructor =
						cls.getConstructor(new Class[] { DataInputExtended.class });

					// recursively decode
					return (Encodable) constructor.newInstance(new Object[] { input });
				} catch (ClassNotFoundException ex) {
					throw new FailedToDecodeException(ex);
				} catch (final IllegalArgumentException ex) {
					throw new FailedToDecodeException(ex);
				} catch (final InstantiationException ex) {
					throw new FailedToDecodeException(ex);
				} catch (final IllegalAccessException ex) {
					throw new FailedToDecodeException(ex);
				} catch (final InvocationTargetException ex) {
					throw new FailedToDecodeException(ex);
				} catch (SecurityException ex) {
					throw new FailedToDecodeException(ex);
				} catch (NoSuchMethodException ex) {
					throw new FailedToDecodeException(ex);
				}

			} finally {
				if (LOG.isDebugEnabled()) {
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
		protected void doWrite(DataOutputExtended output, Encodable[] values) throws IOException {
			try {
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeInt(values.length);
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append("length: ").append(values.length));
				}
				for (Encodable encodable : values) {
					// using FieldType to write out takes care of null handling
					FieldType.ENCODABLE.write(output, encodable);
				}
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@SuppressWarnings("unchecked")
		@Override
		protected <Q> Q[] doReadArray(DataInputExtended input, Class<Q> elementType) throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				int length = inputStream.readInt();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append("length: ").append(length));
				}

				Q[] values = (Q[]) Array.newInstance(elementType, length);
				for (int i = 0; i < values.length; i++) {
					// using FieldType to read in takes care of null handling
					values[i] = (Q) FieldType.ENCODABLE.read(input);
				}
				return values;
			} finally {
				if (LOG.isDebugEnabled()) {
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
		protected void doWrite(DataOutputExtended output, Serializable value) throws IOException {
			try {
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append("[SERIALIZABLE]"));
				}

				// write out as blob of bytes
				ObjectOutputStream oos = new ObjectOutputStream(output.getDataOutputStream());
				oos.writeObject(value);
				oos.flush();
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected Serializable doRead(DataInputExtended input) throws IOException {
			try {
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append("[SERIALIZABLE]"));
				}

				// read in a blob of bytes
				ObjectInputStream ois = new ObjectInputStream(input.getDataInputStream());
				try {
					return (Serializable) ois.readObject();
				} catch (ClassNotFoundException ex) {
					throw new FailedToDeserializeException(ex);
				}
			} finally {
				if (LOG.isDebugEnabled()) {
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
		protected void doWrite(DataOutputExtended output, Serializable[] values) throws IOException {
			try {
				DataOutputStream outputStream = output.getDataOutputStream();
				outputStream.writeInt(values.length);
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append("length: ").append(values.length));
				}

				for (Serializable value : values) {
					// using FieldType to write out takes care of null handling
					FieldType.SERIALIZABLE.write(output, value);
				}
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@SuppressWarnings("unchecked")
		protected <Q> Q[] doReadArray(DataInputExtended input, Class<Q> elementType)
			throws IOException {
			try {
				DataInputStream inputStream = input.getDataInputStream();
				int length = inputStream.readInt();
				if (LOG.isDebugEnabled()) {
					log(this, new StringBuilder().append("length: ").append(length));
				}

				Q[] values = (Q[]) Array.newInstance(elementType, length);
				for (int i = 0; i < values.length; i++) {
					// using FieldType to read in takes care of null handling
					values[i] = (Q) FieldType.SERIALIZABLE.read(input);
				}
				return values;
			} finally {
				if (LOG.isDebugEnabled()) {
					unlog(this);
				}
			}
		}
		@Override
		protected boolean checksStream() {
			return false;
		}
	};



	public static FieldType<?> get(byte idx) {
		return cache.get(idx);
	}

	private final byte idx;
	private final Class<T> cls;
	private final Indenting indenting;

	private FieldType(byte idx, Class<T> cls, Indenting indenting) {
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
	 * Broadly, the type safe ones do, the {@link Encodable} and {@link Serializable} ones do not.
	 */
	protected boolean checksStream() {
		return true;
	}


	public final T read(DataInputExtended input)
			throws IOException {
		DataInputStream inputStream = input.getDataInputStream();
		byte fieldTypeIdxAndNullability = inputStream.readByte();

		boolean isNull = fieldTypeIdxAndNullability >= NULL_BIT;
		byte fieldTypeIdx = (byte) (fieldTypeIdxAndNullability - (isNull ? NULL_BIT : 0));
		try {
			FieldType<?> fieldType = FieldType.get(fieldTypeIdx);
			if (fieldType.checksStream() && fieldType != this) {
				throw new IllegalStateException("Mismatch in stream: expected "
						+ this + " but got " + fieldType);
			}

			if (isNull && LOG.isDebugEnabled()) {
				// only log if reading a null; otherwise actual value read logged later
				log(this, new StringBuilder().append("(null)"));
			}

			if (isNull) {
				return null;
			} else {
				return doRead(input);
			}
		} finally {
			if (isNull && LOG.isDebugEnabled()) {
				// only unlog if reading a null
				unlog(this);
			}
		}
	}

	public final <Q> Q[] readArray(DataInputExtended input, Class<Q> elementType)
		throws IOException {
		DataInputStream inputStream = input.getDataInputStream();
		byte fieldTypeIdxAndNullability = inputStream.readByte();

		boolean isNull = fieldTypeIdxAndNullability >= NULL_BIT;
		byte fieldTypeIdx = (byte) (fieldTypeIdxAndNullability - (isNull ? NULL_BIT : 0));
		try {
			FieldType<?> fieldType = FieldType.get(fieldTypeIdx);
			if (fieldType.checksStream() && fieldType != this) {
				throw new IllegalStateException("Mismatch in stream: expected "
						+ this + " but got " + fieldType);
			}

			if (isNull && LOG.isDebugEnabled()) {
				// only log if reading a null; otherwise actual value read logged later
				log(this, new StringBuilder().append("(null)"));
			}

			if (isNull) {
				return null;
			} else {
				return doReadArray(input, elementType);
			}

		} finally {
			if (isNull && LOG.isDebugEnabled()) {
				// only unlog if reading a null
				unlog(this);
			}
		}

	}


	public final void write(DataOutputExtended output, T value)
			throws IOException {
		byte fieldTypeIdxAndNullability = getIdx();
		boolean isNull = value == null;
		if (isNull) {
			// set high order bit
			fieldTypeIdxAndNullability += NULL_BIT;
		}
		try {

			DataOutputStream outputStream = output.getDataOutputStream();

			outputStream.write(fieldTypeIdxAndNullability);
			if (isNull && LOG.isDebugEnabled()) {
				// only log if writing a null; otherwise actual value logged later
				log(this, new StringBuilder().append("(null)"));
			}

			if(!isNull) {
				doWrite(output, value);
			}
		} finally {
			if (isNull && LOG.isDebugEnabled()) {
				// only unlog if writing a null
				unlog(this);
			}
		}
	}

	protected T doRead(DataInputExtended input)
			throws IOException {
		throw new UnsupportedOperationException("not supported for this field type");
	}

	protected <Q> Q[] doReadArray(DataInputExtended input, Class<Q> elementType)
			throws IOException {
		throw new UnsupportedOperationException("not supported for this field type");
	}

	protected abstract void doWrite(DataOutputExtended output, T value)
			throws IOException;


	private boolean isIndentingAndOutdenting() {
		return indenting == Indenting.INDENT_AND_OUTDENT;
	}

	/////////////////////////////////////////////////////////
	// debugging
	/////////////////////////////////////////////////////////

	private static ThreadLocal<int[]> debugIndent = new ThreadLocal<int[]>();
	private static void log(FieldType<?> fieldType, StringBuilder buf) {
		buf.insert(0, ": ");
		buf.insert(0, fieldType);
		if (fieldType.isIndentingAndOutdenting()) {
			buf.insert(0, "> ");
		}
		buf.insert(0, spaces(currentDebugLevel()));
		incrementDebugLevel();
		LOG.debug(buf.toString());
	}

	private static void unlog(FieldType<?> fieldType) {
		unlog(fieldType, new StringBuilder());
	}

	private static void unlog(FieldType<?> fieldType, StringBuilder buf) {
		if (fieldType.isIndentingAndOutdenting()) {
			buf.insert(0, "< ");
		}
		decrementDebugLevel();
		if (fieldType.isIndentingAndOutdenting()) {
			buf.insert(0, spaces(currentDebugLevel()));
			LOG.debug(buf.toString());
		}
	}

	private static String spaces(int num) {
		return LOG_INDENT.substring(0,num);
	}

	private static int currentDebugLevel() {
		return debugIndent()[0];
	}

	private static void incrementDebugLevel() {
		int[] indentLevel = debugIndent();
		indentLevel[0]+=2;
	}

	private static void decrementDebugLevel() {
		int[] indentLevel = debugIndent();
		indentLevel[0]-=2;
	}

	private static int[] debugIndent() {
		int[] indentLevel = debugIndent.get();
		if (indentLevel == null) {
			indentLevel = new int[1];
			debugIndent.set(indentLevel);
		}
		return indentLevel;
	}


	/////////////////////////////////////////////////////////
	// toString
	/////////////////////////////////////////////////////////

	@Override
	public String toString() {
		return getCls().getSimpleName();
	}

}

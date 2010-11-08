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


package org.apache.isis.core.metamodel.encoding;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;


public class DataOutputStreamExtended implements DataOutputExtended {

	private final DataOutputStream dataOutputStream;
	
    public DataOutputStreamExtended(final OutputStream output) {
    	dataOutputStream = new DataOutputStream(output);
    }
    
	public DataOutputStream getDataOutputStream() {
		return dataOutputStream;
	}

    
	//////////////////////////////////////////
	// Boolean, Char
	//////////////////////////////////////////

	public void writeBoolean(boolean value) throws IOException {
		FieldType.BOOLEAN.write(this, value);
	}

	public void writeBooleans(boolean[] value) throws IOException {
		FieldType.BOOLEAN_ARRAY.write(this, value);
	}
	
	public void writeChar(int value) throws IOException {
		FieldType.CHAR.write(this, (char)value);
	}
	
	public void writeChars(char[] value) throws IOException {
		FieldType.CHAR_ARRAY.write(this, value);
	}
	
	//////////////////////////////////////////
	// Integral Numbers
	//////////////////////////////////////////

	public void write(int value) throws IOException {
		writeByte((byte)value);
	}
	
	public void writeByte(int value) throws IOException {
		FieldType.BYTE.write(this, (byte) value);
	}

	public void write(byte[] value) throws IOException {
		writeBytes(value);
	}

	public void writeBytes(byte[] value) throws IOException {
		FieldType.BYTE_ARRAY.write(this, value);
	}

	public void writeShort(int value) throws IOException {
		FieldType.SHORT.write(this, (short) value);
	}

	public void writeShorts(short[] value) throws IOException {
		FieldType.SHORT_ARRAY.write(this, value);
	}

	public void writeInt(int value) throws IOException {
		FieldType.INTEGER.write(this, value);
	}
	
	public void writeInts(int[] value) throws IOException {
		FieldType.INTEGER_ARRAY.write(this, value);
	}
	
	public void writeLong(long value) throws IOException {
		FieldType.LONG.write(this, value);
	}
	
	public void writeLongs(long[] value) throws IOException {
		FieldType.LONG_ARRAY.write(this, value);
	}
	
	//////////////////////////////////////////
	// Floating Point Numbers
	//////////////////////////////////////////

	public void writeFloat(float value) throws IOException {
		FieldType.FLOAT.write(this, value);
	}
	
	public void writeFloats(float[] value) throws IOException {
		FieldType.FLOAT_ARRAY.write(this, value);
	}
	
	public void writeDouble(double value) throws IOException {
		FieldType.DOUBLE.write(this, value);
	}
	
	public void writeDoubles(double[] value) throws IOException {
		FieldType.DOUBLE_ARRAY.write(this, value);
	}
	
	//////////////////////////////////////////
	// Strings
	//////////////////////////////////////////

	public void writeUTF(String value) throws IOException {
		FieldType.STRING.write(this, value);
	}
	
	public void writeUTFs(String[] value) throws IOException {
		FieldType.STRING_ARRAY.write(this, value);
	}

	//////////////////////////////////////////
	// Encodable and Serializable
	//////////////////////////////////////////

	public void writeEncodable(Object encodable) throws IOException {
		FieldType.ENCODABLE.write(this, (Encodable) encodable);
	}
	
	public void writeEncodables(Object[] objects) throws IOException {
		Encodable[] encodables;
		if (objects == null) {
			encodables = null;
		} else {
			encodables = new Encodable[objects.length];
			for (int i = 0; i < encodables.length; i++) {
				encodables[i] = (Encodable) objects[i];
			}
		}
		FieldType.ENCODABLE_ARRAY.write(this, encodables);
	}
	
	public void writeSerializable(Object serializable) throws IOException {
		FieldType.SERIALIZABLE.write(this, (Serializable) serializable);
	}
	
	public void writeSerializables(Object[] objects) throws IOException {
		Serializable[] serializeables;
		if (objects == null) {
			serializeables = null;
		} else {
			serializeables = new Serializable[objects.length];
			for (int i = 0; i < serializeables.length; i++) {
				serializeables[i] = (Serializable) objects[i];
			}
		}
		FieldType.SERIALIZABLE_ARRAY.write(this, serializeables);
	}
	
	//////////////////////////////////////////
	// Other
	//////////////////////////////////////////

	public void write(byte[] b, int off, int len) throws IOException {
		dataOutputStream.write(b, off, len);
	}

	public void writeBytes(String str) throws IOException {
		dataOutputStream.writeBytes(str);
	}

	public void writeChars(String str) throws IOException {
		dataOutputStream.writeChars(str);
	}


	
	//////////////////////////////////////////
	// Flushable
	//////////////////////////////////////////

	public void flush() throws IOException {
		dataOutputStream.flush();
	}


}


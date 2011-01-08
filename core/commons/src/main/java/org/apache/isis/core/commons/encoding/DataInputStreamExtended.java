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
import java.io.IOException;
import java.io.InputStream;


public class DataInputStreamExtended implements DataInputExtended {
	
	private DataInputStream dataInputStream;

    public DataInputStreamExtended(final InputStream inputStream) {
        this.dataInputStream = new DataInputStream(inputStream);
    }
    
    public DataInputStream getDataInputStream() {
    	return dataInputStream;
    }
    

	//////////////////////////////////////////
	// Boolean, Char
	//////////////////////////////////////////
	
	public boolean readBoolean() throws IOException {
		return FieldType.BOOLEAN.read(this);
	}

	public boolean[] readBooleans() throws IOException {
		return FieldType.BOOLEAN_ARRAY.read(this);
	}


	public char readChar() throws IOException {
		return FieldType.CHAR.read(this);
	}
	
	public char[] readChars() throws IOException {
		return FieldType.CHAR_ARRAY.read(this);
	}


	//////////////////////////////////////////
	// Integral Numbers
	//////////////////////////////////////////

	public byte readByte() throws IOException {
		return FieldType.BYTE.read(this);
	}

	public byte[] readBytes() throws IOException {
		return FieldType.BYTE_ARRAY.read(this);
	}

	public short readShort() throws IOException {
		return FieldType.SHORT.read(this);
	}

	public short[] readShorts() throws IOException {
		return FieldType.SHORT_ARRAY.read(this);
	}
	
	public int readInt() throws IOException {
		return FieldType.INTEGER.read(this);
	}

	public int readUnsignedByte() throws IOException {
		return FieldType.UNSIGNED_BYTE.read(this);
	}
	
	public int readUnsignedShort() throws IOException {
		return FieldType.UNSIGNED_SHORT.read(this);
	}
	
	public int[] readInts() throws IOException {
		return FieldType.INTEGER_ARRAY.read(this);
	}
	
	public long readLong() throws IOException {
		return FieldType.LONG.read(this);
	}

	public long[] readLongs() throws IOException {
		return FieldType.LONG_ARRAY.read(this);
	}
	
	
	//////////////////////////////////////////
	// Floating Point Numbers
	//////////////////////////////////////////

	public float readFloat() throws IOException {
		return FieldType.FLOAT.read(this);
	}

	public float[] readFloats() throws IOException {
		return FieldType.FLOAT_ARRAY.read(this);
	}

	public double readDouble() throws IOException {
		return FieldType.DOUBLE.read(this);
	}

	public double[] readDoubles() throws IOException {
		return FieldType.DOUBLE_ARRAY.read(this);
	}
	

	//////////////////////////////////////////
	// Strings
	//////////////////////////////////////////

	public String readUTF() throws IOException {
		return FieldType.STRING.read(this);
	}

	public String[] readUTFs() throws IOException {
		return FieldType.STRING_ARRAY.read(this);
	}



	//////////////////////////////////////////
	// Encodable and Serializable
	//////////////////////////////////////////

	@SuppressWarnings("unchecked")
	public <T> T readEncodable(Class<T> encodableType) throws IOException {
		return (T)FieldType.ENCODABLE.read(this);
	}

	public <T> T[] readEncodables(Class<T> elementType) throws IOException {
		return FieldType.ENCODABLE_ARRAY.readArray(this, elementType);
	}

	@SuppressWarnings("unchecked")
	public <T> T readSerializable(Class<T> serializableType) throws IOException {
		return (T) FieldType.SERIALIZABLE.read(this);
	}
	
	public <T> T[] readSerializables(Class<T> elementType) throws IOException {
		return FieldType.SERIALIZABLE_ARRAY.readArray(this, elementType);
	}

	
	//////////////////////////////////////////
	// Other
	//////////////////////////////////////////

	public void readFully(byte[] b) throws IOException {
		dataInputStream.readFully(b);
	}

	public void readFully(byte[] b, int off, int len) throws IOException {
		dataInputStream.readFully(b, off, len);
	}

	@SuppressWarnings("deprecation")
	public String readLine() throws IOException {
		return dataInputStream.readLine();
	}

	public int skipBytes(int n) throws IOException {
		return dataInputStream.skipBytes(n);
	}

}


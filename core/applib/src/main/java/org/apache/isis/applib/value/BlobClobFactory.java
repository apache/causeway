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
package org.apache.isis.applib.value;

import static org.apache.isis.commons.internal.base._Strings.asFileNameWithExtension;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @since 2.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlobClobFactory {

	/**
	 * see https://stackoverflow.com/questions/4212861/what-is-a-correct-mime-type-for-docx-pptx-etc
	 */
	public static enum Type {
		txt("text/plain"),
		xml("xml/plain"),
		zip("application/zip"),
		json("application/json"), 
		xlsx("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
		;
		final String base;
		final MimeType mimeType;
		
		private Type(String baseType) {
			this.base = baseType;
			try {
				this.mimeType = new MimeType(baseType);
	        } catch (MimeTypeParseException e) {
	            throw new IllegalArgumentException(e);
	        }
		}
		
		public String getFileNameWithExtension(String fileName) {
			return asFileNameWithExtension(fileName, name());
		}
	}

	/**
	 * Returns a new {@link Blob} of given {@code type}, {@code fileName} and {@code content}.
	 * <p>
	 * {@code fileName} may or may not include the desired filename extension, anyway it 
	 * is guaranteed, that the resulting Blob has the appropriate extension as dictated by 
	 * the given {@code Type}'s name.
	 * <p>
	 * For more fine-grained control use one of the {@link Blob} constructors directly. 
	 * @param type
	 * @param fileName - may or may not include the desired filename extension
	 * @param content
	 * @return new {@link Blob}
	 */
	public static Blob blob(Type type, String fileName, byte[] content){
		return blob(type, fileName, type.name(), content);
	}
	
	/** To explicitly specify the fileNameExtension, otherwise use {@link #blob(Type, String, byte[])} */
	public static Blob blob(Type type, String fileName, String fileNameExtension, byte[] content){
		return new Blob(asFileNameWithExtension(fileName, fileNameExtension), type.mimeType, content);
	}
	
	/**
	 * Returns a new {@link Clob} of given {@code type}, {@code fileName} and {@code content}.
	 * <p>
	 * {@code fileName} may or may not include the desired filename extension, anyway it 
	 * is guaranteed, that the resulting Blob has the appropriate extension as dictated by 
	 * the given {@code Type}'s name.
	 * <p>
	 * For more fine-grained control use one of the {@link Clob} constructors directly. 
	 * @param type
	 * @param fileName - may or may not include the desired filename extension
	 * @param content
	 * @return new {@link Clob}
	 */
	public static Clob clob(Type type, String fileName, CharSequence content){
		return clob(type, fileName, type.name(), content);
	}
	
	/** To explicitly specify the fileNameExtension, otherwise use {@link #clob(Type, String, String)} */
	public static Clob clob(Type type, String fileName, String fileNameExtension, CharSequence content){
		return new Clob(asFileNameWithExtension(fileName, fileNameExtension), type.mimeType, content);
	}

	// -- SHORTCUTS - BLOB

	/** A shortcut, see {@link #blob(Type, String, byte[])} */
	public static Blob blobTxt(String fileName, byte[] content) {
		return blob(Type.txt, fileName, content);
	}

	/** A shortcut, see {@link #blob(Type, String, byte[])} */
	public static Blob blobXml(String fileName, byte[] content) {
		return blob(Type.xml, fileName, content);
	}
	
	/** A shortcut, see {@link #blob(Type, String, byte[])} */
	public static Blob blobZip(String fileName, byte[] content) {
		return blob(Type.zip, fileName, content);
	}
	
	/** A shortcut, see {@link #blob(Type, String, byte[])} */
	public static Blob blobJson(String fileName, byte[] content) {
		return blob(Type.json, fileName, content);
	}
	
	/** A shortcut, see {@link #blob(Type, String, byte[])} */
	public static Blob blobXlsx(String fileName, byte[] content) {
		return blob(Type.xlsx, fileName, content);
	}
	
	// -- SHORTCUTS - CLOB
	
	/** A shortcut, see {@link #clob(Type, String, String)} */
	public static Clob clobTxt(String fileName, CharSequence content) {
		return clob(Type.txt, fileName, content);
	}
	
	/** A shortcut, see {@link #clob(Type, String, String)} */
	public static Clob clobXml(String fileName, CharSequence content) {
		return clob(Type.xml, fileName, content);
	}
	
	/** A shortcut, see {@link #clob(Type, String, String)} */
	public static Clob clobJson(String fileName, CharSequence content) {
		return clob(Type.json, fileName, content);
	}
	
	
}

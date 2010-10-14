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


package org.apache.isis.commons.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceStreamSourceFileSystem extends ResourceStreamSourceAbstract {
	
	private String directory;

	public ResourceStreamSourceFileSystem(String directory) {
		this.directory = directory;
	}

	protected InputStream doReadResource(String resourcePath) throws FileNotFoundException {
		File file = new File(directory, resourcePath);
		return new FileInputStream(file);
	}

	@Override
	public OutputStream writeResource(String resourcePath) {
		File file = new File(directory, resourcePath);
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	public String getName() {
		return "file system (directory '" + directory + "')";
	}

}

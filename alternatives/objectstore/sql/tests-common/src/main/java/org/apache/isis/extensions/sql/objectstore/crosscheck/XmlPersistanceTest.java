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


/**
 * 
 */
package org.apache.isis.extensions.sql.objectstore.crosscheck;

import java.util.Properties;

import org.apache.isis.extensions.sql.objectstore.common.SqlIntegrationTestCommon;
import java.io.*;




public class XmlPersistanceTest extends SqlIntegrationTestCommon {

	  public static void deleteFiles( String directory, FilenameFilter extension ) {
		    File dir = new File(directory);

		    String[] list = dir.list(extension);
		    File file;
		    if (list.length == 0) return;

		    for (int i = 0; i < list.length; i++) {
		      file = new File(directory, list[i]);
		      file.delete();
		    }
	  }
	
	public void initialiseTests() {
		// Delete all XML files.
		deleteFiles("xml/objects", new FilenameFilter() {
			
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".xml");
			}
		});
	}
	
	@Override
	public Properties getProperties(){
		Properties properties = new Properties();
		properties.put("isis.persistence", "xml");
		properties.put("isis.logging.objectstore","on");
		return properties;
	}
	public  String getPropertiesFilename(){
		return "xml.properties";
	}
	@Override
	public String getSqlTeardownString(){
		return "SHUTDOWN;";
	}
	
	
}


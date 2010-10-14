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


package org.apache.isis.remoting.server;

import org.apache.isis.remoting.protocol.encoding.internal.ObjectEncoderDecoder;
import org.apache.isis.remoting.protocol.encoding.internal.ObjectEncoderDecoderDefault;
import org.apache.isis.runtime.viewer.IsisViewer;
import org.apache.isis.runtime.viewer.IsisViewerInstallerAbstract;

public abstract class SocketsViewerInstallerAbstract extends IsisViewerInstallerAbstract {

	public SocketsViewerInstallerAbstract(String name) {
		super(name);
	}

	@Override
    public IsisViewer doCreateViewer() {
        ObjectEncoderDecoderDefault encoder = 
        	ObjectEncoderDecoderDefault.create(getConfiguration());
        return createSocketsViewer(encoder);
    }
	
    protected abstract IsisViewer createSocketsViewer(ObjectEncoderDecoder objectEncoderDecoder);

}

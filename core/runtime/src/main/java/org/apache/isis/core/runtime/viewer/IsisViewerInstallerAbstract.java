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


package org.apache.isis.core.runtime.viewer;

import java.util.List;

import org.apache.isis.core.runtime.installers.InstallerAbstract;
import org.apache.isis.core.runtime.installers.InstallerLookup;


public abstract class IsisViewerInstallerAbstract extends InstallerAbstract implements IsisViewerInstaller {

	private InstallerLookup installerLookup;

	public IsisViewerInstallerAbstract(String name) {
		super(IsisViewerInstaller.TYPE, name);
	}
	
    public IsisViewer createViewer() {
        return injectDependenciesInto(doCreateViewer());
    }
    
    /**
     * Subclasses should override (or else override {@link #createViewer()} if they need to do anything more elaborate.
     */
    protected IsisViewer doCreateViewer() {
    	return null;
    }

    protected <T> T injectDependenciesInto(T candidate) {
    	return installerLookup.injectDependenciesInto(candidate);
    }
    
	public void setInstallerLookup(InstallerLookup installerLookup) {
		this.installerLookup = installerLookup;
	}

	
	@Override
	public List<Class<?>> getTypes() {
		return listOf(IsisViewer.class);
	}
}


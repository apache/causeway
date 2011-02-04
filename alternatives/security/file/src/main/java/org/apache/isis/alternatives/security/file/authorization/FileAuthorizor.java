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


package org.apache.isis.alternatives.security.file.authorization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationException;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.runtime.authorization.standard.AuthorizorAbstract;
import org.apache.isis.core.runtime.system.JmxBeanServer;
import org.apache.log4j.Logger;


public class FileAuthorizor extends AuthorizorAbstract implements FileAuthorizorMBean {

    private static final Logger LOG = Logger.getLogger(FileAuthorizor.class);

    private static final String NONE = "";
    private static final String RO = "-ro";
    private static final String RW = "-rw";
    
    private Map<String,List<String>> whiteListMap;
    private Map<String,List<String>> blackListMap;
    
    private final ResourceStreamSource resourceStreamSource;
    private final boolean learn;
    
    private final String whiteListResourceName;
    private InputStream whiteListInputResource;
    
    private final String blackListResourceName;
    private InputStream blackListInputResource;

    private boolean printedWarning;
    private boolean printedDebug;

    public FileAuthorizor(IsisConfiguration configuration) {
    	super(configuration);

        // read from config
    	this.resourceStreamSource = getConfiguration().getResourceStreamSource();
        
    	this.learn = getConfiguration().getBoolean(FileAuthorizationConstants.LEARN, FileAuthorizationConstants.LEARN_DEFAULT);
        whiteListResourceName = getConfiguration().getString(FileAuthorizationConstants.WHITELIST_RESOURCE_KEY,FileAuthorizationConstants.WHITELIST_RESOURCE_DEFAULT);
        Assert.assertTrue(whiteListResourceName.length() > 0);
        blackListResourceName = getConfiguration().getString(FileAuthorizationConstants.BLACKLIST_RESOURCE, FileAuthorizationConstants.BLACKLIST_RESOURCE_DEFAULT);
        
        findResources();
        
                
        JmxBeanServer.getInstance().register("FileAuthorizor", this);
    }


    private void findResources() {
        whiteListInputResource = resourceStreamSource.readResource(whiteListResourceName);
        if (whiteListInputResource == null) {
            throw new IsisException("Cannot read whitelist authorization file: " + whiteListResourceName);
        }
        
        if (blackListResourceName.length() > 0) {
            this.blackListInputResource = resourceStreamSource.readResource(blackListResourceName);
            if (blackListInputResource == null) {
                throw new IsisException("Blacklist authorization file exists, but it cannot be read: " + blackListResourceName);
            }
        } else {
        	blackListInputResource = null;
        }
    }

    
    ////////////////////////////////////////////////////////////////
    // init, shutdown
    ////////////////////////////////////////////////////////////////
    
    @Override
    public void init() {
        
        // initialize
        if (learn) {
            return;
        }
        whiteListMap = new HashMap<String,List<String>>();
        blackListMap = new HashMap<String,List<String>>();
        cacheAuthorizationDetails(whiteListMap, whiteListInputResource);
        if (blackListInputResource != null) {
            cacheAuthorizationDetails(blackListMap, blackListInputResource);
        }
    }

    public void reload() {
        Map<String,List<String>> whiteListMap = new HashMap<String,List<String>>();
        Map<String,List<String>> blackListMap = new HashMap<String,List<String>>();

        findResources();
        cacheAuthorizationDetails(whiteListMap, whiteListInputResource);
        if (blackListInputResource != null) {
            cacheAuthorizationDetails(blackListMap, blackListInputResource);
            this.blackListMap = blackListMap;
        }
        this.whiteListMap = whiteListMap;
    }


    private void cacheAuthorizationDetails(final Map<String,List<String>> map, final InputStream inputStream) {
        try {
        	if (LOG.isInfoEnabled()) {
        		LOG.info("loading authorization details from " + whiteListResourceName);
        	}
            final BufferedReader buffReader = 
            	new BufferedReader(new InputStreamReader(inputStream));
            for (String line; (line = buffReader.readLine()) != null;) {
                tokenizeLine(map, line);
            }
            buffReader.close();
        } catch (final Exception e) {
            throw new IsisException(e);
        }
    }

    private void tokenizeLine(final Map<String,List<String>> map, final String line) {
        if (line.trim().startsWith("#") || line.trim().length() == 0) {
            return;
        }
        final StringTokenizer tokens = new StringTokenizer(line.trim(), ":", false);
        if (tokens.countTokens() != 2) {
            throw new IsisConfigurationException("Invalid line: " + line);
        }
        final String token1 = tokens.nextToken();
        final String token2 = tokens.nextToken();
        final Identifier identifier = memberFromString(token1.trim());
        final List<String> roles = tokenizeRoles(token2);
        String identityString = identifier.toIdentityString(Identifier.CLASS_MEMBERNAME_PARAMETERS);
        map.put(identityString, roles);
    }

    private Identifier memberFromString(final String identifier) {
    	return Identifier.fromIdentityString(identifier);
    }

    private List<String> tokenizeRoles(final String allRoles) {
        final List<String> roles = new ArrayList<String>();
        final StringTokenizer tokens = new StringTokenizer(allRoles, "|", false);
        while (tokens.hasMoreTokens()) {
            String nextToken = tokens.nextToken();
            String trimmedNextToken = nextToken.trim();
            roles.add(trimmedNextToken);
        }
        return roles;
    }

    @Override
    public void shutdown() {
        if (learn) {
            writeMap();
        }
    }


    ////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////

    @Override
    public boolean isUsableInRole(final String role, final Identifier member) {
        return isAuthorized(role, member, new String[] { NONE, RW });
    }

    @Override
    public boolean isVisibleInRole(final String role, final Identifier member) {
        return isAuthorized(role, member, new String[] { NONE, RO, RW });
    }

    private boolean isAuthorized(final String role, final Identifier member, final String[] qualifiers) {
        if (learn) {
            return learn(role, member);
        }
        return isWhiteListed(role, member, qualifiers) && 
              !isBlackListed(role, member, qualifiers);
    }

    private boolean isWhiteListed(final String role, final Identifier member, final String[] qualifiers) {
        return isListed(whiteListMap, role, member, qualifiers);
    }

    private boolean isBlackListed(final String role, final Identifier member, final String[] qualifiers) {
        return isListed(blackListMap, role, member, qualifiers);
    }

    private boolean isListed(final Map<String,List<String>> map, final String role, final Identifier identifier, final String[] qualifiers) {
        if (map.isEmpty()) {// quick fail
            return false;
        }
        if (isQualifiedMatch(map, role, identifier.toIdentityString(Identifier.CLASS), qualifiers)) {
        	return true;
        }
        if (isQualifiedMatch(map, role, identifier.toIdentityString(Identifier.CLASS_MEMBERNAME), qualifiers)) {
        	return true;
        }
        if (isQualifiedMatch(map, role, identifier.toIdentityString(Identifier.CLASS_MEMBERNAME_PARAMETERS), qualifiers)) {
        	return true;
        }
        return false;
    }

    private boolean isQualifiedMatch(final Map<String,List<String>> map, final String role, final String key, final String[] qualifiers) {
        if (map.containsKey(key)) {
            final List<String> roles = map.get(key);
            for (int i = 0; i < qualifiers.length; i++) {
                final String qualifiedRole = role + qualifiers[i];
                if (roles.contains(qualifiedRole)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean learn(final String role, final Identifier member) {
        String identityString = member.toIdentityString(Identifier.CLASS_MEMBERNAME_PARAMETERS);
        if (whiteListMap.containsKey(identityString)) {
            final List<String> roles = whiteListMap.get(identityString);
            if (!roles.contains(role)) {
                roles.add(role);
            }
        } else {
            whiteListMap.put(identityString, Arrays.asList(new String[] { role }));
        }
        
        // REVIEW: might be too labour intensive
        writeMap();
        return true;
    }
    
    private void writeMap() {
        try {
            OutputStream whiteListOutputResource = resourceStreamSource.writeResource(whiteListResourceName);
            if (whiteListOutputResource == null) {
                if (!printedWarning) {
                	LOG.warn("unable to write out authorisation details");
                	printedWarning = true; // just to stop flooding log
                }
            	return;
            }
            if (LOG.isDebugEnabled() && !printedDebug) {
            	LOG.debug("writing authorisation details to " + whiteListResourceName);
            	printedDebug = true; // just to stop flooding log
            }
            final OutputStreamWriter fileWriter = new OutputStreamWriter(whiteListOutputResource);
            final BufferedWriter buffWriter = new BufferedWriter(fileWriter);
            Set<Entry<String, List<String>>> entrySet = whiteListMap.entrySet();
            for (int i = 0; i < entrySet.size(); i++) {
                final Map.Entry<String,List<String>> entry = (Map.Entry<String,List<String>>) entrySet.toArray()[i];
                final StringBuffer buff = new StringBuffer();
                buff.append(entry.getKey()).append(":");
                final List<String> roles = entry.getValue();
                for (int j = 0; j < roles.size(); j++) {
                    buff.append(roles.get(j));
                    if (j < roles.size() - 1) {
                        buff.append("|");
                    }
                }
                buffWriter.write(buff.toString());
                buffWriter.newLine();
            }
            buffWriter.flush();
            buffWriter.close();
        } catch (final IOException e) {
            throw new IsisException(e);
        }
    }

}

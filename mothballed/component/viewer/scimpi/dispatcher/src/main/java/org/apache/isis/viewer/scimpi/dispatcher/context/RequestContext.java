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

package org.apache.isis.viewer.scimpi.dispatcher.context;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.ErrorCollator;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.action.PropertyException;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugUsers;

public abstract class RequestContext {
    private static final Logger LOG = LoggerFactory.getLogger(RequestContext.class);
    static final String TRANSIENT_OBJECT_OID_MARKER = "~";

    public enum Scope {
        GLOBAL, SESSION, INTERACTION, REQUEST, ERROR
    };

    public enum Debug {
        ON, OFF, PAGE
    }

    public static Scope scope(final String scopeName) {
        final String name = scopeName.toUpperCase();
        if (name.equals(Scope.GLOBAL.toString())) {
            return Scope.GLOBAL;
        } else if (name.equals(Scope.SESSION.toString())) {
            return Scope.SESSION;
        } else if (name.equals(Scope.INTERACTION.toString())) {
            return Scope.INTERACTION;
        } else if (name.equals(Scope.REQUEST.toString())) {
            return Scope.REQUEST;
        }
        throw new IllegalArgumentException("Invalid scope name: " + scopeName);
    }

    public static Scope scope(final String scopeName, final Scope defaultScope) {
        if (scopeName == null || scopeName.trim().equals("")) {
            return defaultScope;
        } else {
            return scope(scopeName);
        }
    }

    public static final String RESULT = "_result";
    public static final String ERROR = "_error";
    public static final String BACK_TO = "_back_to";
    private static final Map<String, Object> globalVariables = new HashMap<String, Object>();
    private static final Scope[] SCOPES = new Scope[] { Scope.ERROR, Scope.REQUEST, Scope.INTERACTION, Scope.SESSION, Scope.GLOBAL };

    private final OidMarshaller oidMarshaller = new OidMarshaller();


    private final ObjectMapping objectMapping;
    private final VersionMapping versionMapping;
    private final Map<Scope, Map<String, Object>> variables;
    private final StringBuffer debugTrace = new StringBuffer();
    private final DebugUsers debugUsers;

    private String forwardTo;
    private String requestedFile;
    private String requestedParentPath;
    private AuthenticationSession session;
    private Debug debug;
    private String resourceFile;
    private String resourceParentPath;
    private ObjectAdapter collection;
    private boolean isUserAuthenticated;

    public RequestContext(final DebugUsers debugUsers) {
        this.debugUsers = debugUsers;

        String className = IsisContext.getConfiguration().getString("scimpi.object-mapping.class", DefaultOidObjectMapping.class.getName());
        objectMapping = InstanceUtil.createInstance(className, ObjectMapping.class);
        className = IsisContext.getConfiguration().getString("scimpi.version-mapping.class", DefaultVersionMapping.class.getName());
        versionMapping = InstanceUtil.createInstance(className, VersionMapping.class);
        variables = new HashMap<Scope, Map<String, Object>>();

        variables.put(Scope.GLOBAL, globalVariables);
        variables.put(Scope.SESSION, Maps.<String, Object>newHashMap());
        variables.put(Scope.INTERACTION, Maps.<String, Object>newHashMap());
        variables.put(Scope.REQUEST, Maps.<String, Object>newHashMap());
        variables.put(Scope.ERROR, Maps.<String, Object>newHashMap());
    }

    public void endHttpSession() {
        objectMapping.endSession();
        variables.get(Scope.SESSION).clear();
        session = null;
        clearSession();
    }

    // //////////////////////////////////////////////////////////////////
    // Mapped objects
    // //////////////////////////////////////////////////////////////////

    public ObjectAdapter getMappedObject(final String oidStr) {
        if (oidStr == null || oidStr.trim().equals("") || oidStr.trim().equals("null")) {
            return null;
        }
        if (oidStr.equals("collection")) {
            return collection;
        }
        final ObjectAdapter adapter = mappedObject(oidStr);
        if (adapter == null) {
            throw new ScimpiException("No object for " + oidStr);
        }
        return adapter;
    }

    public ObjectAdapter getMappedObjectOrResult(final String id) {
        return getMappedObjectOrVariable(id, RESULT);
    }

    public ObjectAdapter getMappedObjectOrVariable(String idOrData, final String name) {
        if (idOrData == null) {
            idOrData = (String) getVariable(name);
            if (idOrData == null) {
                throw new ScimpiException("No variable for " + name);
            }
        }
        if (idOrData.equals("collection")) {
            return collection;
        }
        return getMappedObject(idOrData);
    }

    public String mapObject(final ObjectAdapter object, final String scopeName, final Scope defaultScope) {
        final Scope scope = scopeName == null ? defaultScope : scope(scopeName);
        LOG.debug("mapping " + object + " " + scope);
        return objectMapping.mapObject(object, scope);
    }

    private ObjectAdapter mappedObject(String dataOrOid) {
        if (dataOrOid != null && dataOrOid.equals("")) {
            return null;
        }
        if (dataOrOid == null) {
            dataOrOid = RESULT;
        }

        if (dataOrOid.startsWith(TRANSIENT_OBJECT_OID_MARKER + "{")) {
            return objectMapping.mappedTransientObject(StringEscapeUtils.unescapeHtml(dataOrOid.substring(TRANSIENT_OBJECT_OID_MARKER.length())));
        }

        final String oidStr = dataOrOid;
        final TypedOid typedOid = getOidMarshaller().unmarshal(oidStr, TypedOid.class);
        if(typedOid instanceof RootOid) {
//        final String[] idParts = dataOrOid.split("@");
//        if (idParts.length == 2) {
            final ObjectAdapter mappedObject = objectMapping.mappedObject(oidStr);
            if (mappedObject != null) {
                getPersistenceSession().resolveImmediately(mappedObject);
            }
            return mappedObject;
        }

        //
        // else, handle aggregate
        //
        AggregatedOid aggregatedOid = (AggregatedOid) typedOid;
        final TypedOid parentOid = aggregatedOid.getParentOid();

        //final ObjectAdapter parentAdapter = objectMapping.mappedObject(idParts[0] + "@" + idParts[1]);
        final ObjectAdapter parentAdapter = objectMapping.mappedObject(parentOid.enString(getOidMarshaller()));
        getPersistenceSession().resolveImmediately(parentAdapter);

        //ObjectSpecId objectType = null;
        //final AggregatedOid aggregatedOid = new AggregatedOid(objectType, (TypedOid) parentAdapter.getOid(), idParts[2]);

        ObjectAdapter aggregatedAdapter = null;
        outer: for (final ObjectAssociation association : parentAdapter.getSpecification().getAssociations(Contributed.EXCLUDED)) {
            if (association.getSpecification().isParented()) {
                final ObjectAdapter objectAdapter = association.get(parentAdapter);
                if (objectAdapter == null) {
                    continue;
                }
                if (association.isOneToManyAssociation()) {
                    final ObjectAdapter coll = objectAdapter;
                    final CollectionFacet facet = coll.getSpecification().getFacet(CollectionFacet.class);
                    for (final ObjectAdapter element : facet.iterable(coll)) {
                        if (element.getOid().equals(aggregatedOid)) {
                            aggregatedAdapter = element;
                            break outer;
                        }
                    }
                } else {
                    if (objectAdapter.getOid().equals(aggregatedOid)) {
                        aggregatedAdapter = objectAdapter;
                        break;
                    }
                }
            } else if (association.isOneToManyAssociation()) {
                if (association.getId().equals(aggregatedOid.getLocalId())) {
                //if (association.getId().equals(idParts[2])) {
                    return association.get(parentAdapter);
                }
            }
        }
        return aggregatedAdapter;
    }


    public boolean isInternalRequest() {
        final String referrer = getHeader("Referer"); // Note spelling mistake
                                                      // is intentional
        return referrer != null && referrer.contains("localhost"); // TODO need
                                                                   // to look
                                                                   // for actual
                                                                   // domain
    }

    // //////////////////////////////////////////////////////////////////
    // Version
    // //////////////////////////////////////////////////////////////////

    public String mapVersion(final ObjectAdapter object) {
        final Version version = object.getVersion();
        return version == null ? "" : versionMapping.mapVersion(version);
    }

    public Version getVersion(final String id) {
        if (id.equals("")) {
            return null;
        }
        return versionMapping.getVersion(id);
    }

    // ////////////////////////////
    // Debug
    // ////////////////////////////
    public void append(final DebugBuilder debug) {
        debug.startSection("Scimpi Request");

        debug.appendTitle("User");
        final AuthenticationSession session = getSession();
        debug.appendln("Authentication Session", session);
        if (session != null) {
            debug.appendln("Name", session.getUserName());
            debug.appendln("Roles", session.getRoles());
        }

        debug.appendTitle("context");
        debug.appendln("Parent request path", requestedParentPath);
        debug.appendln("Requested file", requestedFile);
        debug.appendln("Parent resource path", resourceParentPath);
        debug.appendln("Resource file", resourceFile);
        debug.endSection();

        debug.startSection("Variables");
        append(debug, Scope.GLOBAL);
        append(debug, Scope.SESSION);
        append(debug, Scope.INTERACTION);
        append(debug, Scope.REQUEST);
        append(debug, Scope.ERROR);
        debug.endSection();

        debug.startSection("Object Mapping");
        objectMapping.append(debug);
        debug.endSection();
    }

    private void append(final DebugBuilder view, final Scope scope) {
        final Map<String, Object> map = variables.get(scope);
        final Iterator<String> keys = new TreeSet<String>(map.keySet()).iterator();
        if (keys.hasNext()) {
            view.appendTitle(scope + " scoped variables");
            while (keys.hasNext()) {
                final String key = keys.next();
                final Object object = map.get(key);
                final String mappedTo = "";
                view.appendln(key, object + mappedTo);
            }
        }
    }

    public void append(final DebugBuilder debug, final String list) {
        if (list.equals("variables")) {
            appendVariables(debug, Scope.GLOBAL);
            appendVariables(debug, Scope.SESSION);
            appendVariables(debug, Scope.INTERACTION);
            appendVariables(debug, Scope.REQUEST);
            appendVariables(debug, Scope.ERROR);
        } else if (list.equals("mappings")) {
            objectMapping.appendMappings(debug);
        }
    }

    private void appendVariables(final DebugBuilder debug, final Scope scope) {
        final Map<String, Object> map = variables.get(scope);
        final Iterator<String> names = new TreeSet(map.keySet()).iterator();
        if (names.hasNext()) {
            debug.startSection(scope.toString());
            while (names.hasNext()) {
                final String name = names.next();
                try {
                    final Object object = map.get(name);
                    String details = "";
                    if (object instanceof String) {
                        final ObjectAdapter mappedObject = mappedObject((String) object);
                        if (mappedObject != null) {
                            details = mappedObject.toString();
                        }
                    }
                    debug.appendln(name, object + "  " + details);
                } catch (final Exception e) {
                    debug.appendln(name, map.get(name));
                }
            }
            debug.endSection();
        }
    }

    public List<String> getDebugUsers() {
        return debugUsers.getNames();
    }

    // ////////////////////////////
    // Variables
    // ////////////////////////////

    public void clearVariables(final Scope scope) {
        variables.get(scope).clear();
    }

    public void changeScope(final String name, final Scope newScope) {
        for (final Scope element : SCOPES) {
            final Map<String, Object> map = variables.get(element);
            final Object object = map.get(name);
            if (object != null) {
                map.remove(name);
                addVariable(name, object, newScope);
                return;
            }
        }
    }

    public void clearVariable(String name, final Scope scope) {
        name = name != null ? name : RESULT;
        variables.get(scope).remove(name);
    }

    public void addVariable(final String name, final Object value, final String scope) {
        addVariable(name, value, scope(scope));
    }

    public void addVariable(String name, final Object value, final Scope scope) {
        name = name != null ? name : RESULT;
        if (scope == Scope.SESSION && value != null && !(value instanceof Serializable)) {
            throw new ScimpiException("SESSION scoped variable (" + name + ") must be serializable: " + value);
        }
        removeExistingVariable(name);
        variables.get(scope).put(name, value);
    }

    private void removeExistingVariable(final String name) {
        for (final Scope element : SCOPES) {
            final Map<String, Object> map = variables.get(element);
            final Object object = map.get(name);
            if (object != null) {
                map.remove(name);
                break;
            }
        }
    }

    public String getStringVariable(final String name) {
        final String value = (String) getVariable(name);
        if (value == null) {
            return null;
        } else {
            return replaceVariables(value);
        }
    }

    public Object getVariable(final String name) {
        for (final Scope element : SCOPES) {
            final Map<String, Object> map = variables.get(element);
            final Object object = map.get(name);
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    public String replaceVariables(String value) {
        final int start = value.indexOf("${");
        if (start == -1) {
            return value;
        } else {
            final int end = value.indexOf('}');
            if (end == -1) {
                throw new PropertyException("No closing brace in " + value.substring(start));
            } else if (end < start) {
                throw new PropertyException("Closing brace before opening brace in " + value.substring(end));
            }
            final String name = value.substring(start + 2, end);
            if (name != null) {
                final int pos = name.indexOf(":");
                final String variableName = pos == -1 ? name : name.substring(0, pos);
                final String qualifier = pos == -1 ? "none" : name.substring(pos);
                Object replacementValue;
                final boolean embed = qualifier.indexOf("embed") > -1;
                if (embed) {
                    replacementValue = "${" + variableName + "}";
                } else {
                    replacementValue = getParameter(variableName);
                    if (replacementValue == null) {
                        replacementValue = getVariable(variableName);
                    }
                    if (replacementValue == null) {
                        replacementValue = getBuiltIn(variableName);
                    }

                    if (replacementValue == null) {
                        final boolean ensureExists = qualifier.indexOf("optional") == -1;
                        if (ensureExists) {
                            throw new PropertyException("No value for the variable " + value.substring(start, end + 1));
                        } else {
                            replacementValue = "";
                        }
                    }
                }
                final boolean repeat = qualifier.indexOf("repeat") > -1;
                if (repeat) {
                    value = value.substring(0, start) + replacementValue + value.substring(end + 1);
                    return replaceVariables(value);
                } else {
                    final String remainder = replaceVariables(value.substring(end + 1));
                    value = value.substring(0, start) + replacementValue + remainder;
                    return value;
                }

            } else {
                throw new PropertyException("No variable name speceified");
            }
        }
    }

    private Object getBuiltIn(final String name) {
        if (name.equals("_session")) {
            return getSessionId();
        } else if (name.equals("_context")) {
            return getContextPath();
        } else if (name.equals("_this")) {
            return resourceFile;
        } else if (name.equals("_directory")) {
            return resourceParentPath;
        } else if (name.equals("_base")) {
            return getUrlBase() + getContextPath() + resourceParentPath + resourceFile;
            // return "http://localhost:8080" + resourceParentPath +
            // resourceFile;
        }
        return null;
    }

    public String encodedInteractionParameters() {
        final StringBuffer buffer = new StringBuffer();
        final Map<String, Object> map = variables.get(Scope.INTERACTION);
        final Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            final Entry<String, Object> entry = iterator.next();
            buffer.append("&amp;" + entry.getKey() + "=" + entry.getValue());
        }
        return buffer.toString();
    }

    public String interactionFields() {
        final StringBuffer buffer = new StringBuffer();
        final Map<String, Object> map = variables.get(Scope.INTERACTION);
        final Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            final Entry<String, Object> entry = iterator.next();
            buffer.append("<input type=\"hidden\" name=\"" + entry.getKey() + "\" value=\"" + entry.getValue() + "\" />\n");
        }
        return buffer.toString();
    }

    protected abstract String getSessionId();

    public abstract void addCookie(String name, String value, int minutesUtilExpires);

    public abstract String getCookie(String name);



    // /////////////////////////////////////////////////
    // Start/end request
    // /////////////////////////////////////////////////

    public void endRequest() throws IOException {
        getWriter().close();
        objectMapping.clear();
        variables.get(Scope.ERROR).clear();
        variables.get(Scope.REQUEST).clear();
        variables.get(Scope.INTERACTION).clear();
    }

    public void startRequest() {
        debugTrace.setLength(0);
        objectMapping.reloadIdentityMap();
        final String debugParameter = getParameter("debug");
        if (debugParameter != null) {
            if (debugParameter.equals("off")) {
                debug = Debug.OFF;
            } else if (debugParameter.equals("on")) {
                debug = Debug.ON;
            } else if (debugParameter.equals("page")) {
                debug = Debug.PAGE;
            }
        }
    }

    public abstract PrintWriter getWriter();

    // /////////////////////////////
    // Forwarding
    // /////////////////////////////
    public void forwardTo(final String forwardTo) {
        this.forwardTo = "/" + forwardTo;
    }

    public String forwardTo() {
        final String returnForwardTo = forwardTo;
        forwardTo = null;
        return returnForwardTo;
    }

    // /////////////////////////////
    // Parameters
    // /////////////////////////////
    public void addParameter(final String name, final String parameter) {
        if (name == null) {
            throw new ScimpiException("Name must be specified for parameter " + parameter);
        }
        addVariable(name, parameter, Scope.REQUEST);
    }

    public String getParameter(final String name) {
        final Object variable = getVariable(name);
        if (variable instanceof String || variable == null) {
            return (String) variable;
        } else {
            return variable.toString();
        }
    }

    public Iterator<Entry<String, Object>> interactionParameters() {
        final Map<String, Object> map = variables.get(Scope.REQUEST);
        final Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
        return iterator;
    }

    // ///////////////////////////////////////
    // Requested file
    // ///////////////////////////////////////

    /**
     * The requested file is the file that the browser requested. This may or
     * may not be the file that is actually processed and returned; that is the
     * {@link #getResourceFile()}.
     */
    public String getRequestedFile() {
        return requestedFile;
    }

    public void setRequestPath(final String filePath) {
        setRequestPath(filePath, null);
    }

    public void setRequestPath(final String filePath, String defaultGenericPath) {
        if (filePath == null) {
            defaultGenericPath = defaultGenericPath == null ? "" : defaultGenericPath;
            this.requestedFile = Dispatcher.GENERIC + defaultGenericPath + "." + Dispatcher.EXTENSION;
        } else if (filePath.startsWith("_generic")) {
            this.requestedParentPath = "/";
            LOG.debug("generic file, requested path cleared");
            this.requestedFile = filePath;
            LOG.debug("requested file set = " + filePath);

        } else {
            final int lastSlash = filePath.lastIndexOf('/');
            if (lastSlash == -1) {
                throw new ScimpiException("No slash in request path: " + filePath);
            }
            final String path = filePath.substring(0, lastSlash + 1);
            LOG.debug("requested path set = " + path);
            this.requestedParentPath = path;

            final String file = filePath.substring(lastSlash + 1);
            LOG.debug("requested file set = " + file);
            this.requestedFile = file;
        }
    }

    public void clearRequestedPath() {
        this.requestedParentPath = null;
        this.requestedFile = null;
    }

    /**
     * Returns the absolute file system path to the specified resource based on
     * the path used for the current request during the call to
     * {@link #setRequestPath(String)}. The return path can then be used to
     * access the specified resource. If the resource has a leading slash (/)
     * then that resource string is returned as the path.
     */
    public String requestedFilePath(final String resource) {
        if (resource.startsWith("/")) {
            return resource;
        } else {
            return requestedParentPath + resource;
        }
    }

    // ///////////////////////////////////////
    // Resource file
    // ///////////////////////////////////////

    /**
     * The resource file is the file on disk that is processed and returned to
     * the browser. This may or may not be the file that was actually requested
     * by the browser; that is the {@link #getRequestedFile()}.
     */
    public String getResourceFile() {
        return resourceFile;
    }

    public String getResourceParentPath() {
        return resourceParentPath;
    }

    public void setResourcePath(final String filePath) {
        if (filePath == null) {
            throw new ScimpiException("Path must be specified");
        } else {
            final int lastSlash = filePath.lastIndexOf('/');
            if (lastSlash == -1) {
                throw new ScimpiException("No slash in request path: " + filePath);
            }
            final String path = /* getContextPath() + */filePath.substring(0, lastSlash + 1);
            LOG.debug("resource path set = " + path);
            this.resourceParentPath = path;

            final String file = filePath.substring(lastSlash + 1);
            LOG.debug("resource file set = " + file);
            this.resourceFile = file;
        }
    }

    /**
     * Returns a uri for the specified resource based on the path used for the
     * current request (as set up during the call to
     * {@link #setResourcePath(String)}). Such a uri when used by the browser
     * will allow access to the specified resource. If the resource has a
     * leading slash (/) or the resource is for a generic page (starts with
     * "_generic") then that resource string is returned as the path.
     */
    public String fullUriPath(final String resource) {
        if (resource.startsWith("/") || resource.startsWith("_generic")) {
            return resource;
        } else {
            return getContextPath() + resourceParentPath + resource;
        }
    }

    /**
     * Returns the absolute file system path to the specified resource based on
     * the path used for the current request (as set up during the call to
     * {@link #setResourcePath(String)}). The return path can then be used to
     * access the specified resource. If the resource has a leading slash (/) or
     * the resource is for a generic page (starts with "_generic") then that
     * resource string is returned as the path.
     */
    public String fullFilePath(final String resource) {
        if (resource.startsWith("/") || resource.startsWith("_generic")) {
            return resource;
        } else {
            return resourceParentPath + resource;
        }
    }

    // //////////////////////////////////////////////////////////////////
    //
    // //////////////////////////////////////////////////////////////////

    public String mapObject(final ObjectAdapter object, final Scope scope) {
        if (object.isValue()) {
            return object.titleString();
        } else if (scope == Scope.INTERACTION && object.isTransient()) {
            return objectMapping.mapTransientObject(object);
        } else if (object.getOid() != null) {
            return objectMapping.mapObject(object, scope);
        } else {
            collection = object;
            return "collection";
        }
    }

    public void unmapObject(final ObjectAdapter object, final Scope scope) {
        objectMapping.unmapObject(object, scope);
    }

    public abstract String findFile(String fileName);

    public abstract InputStream openStream(String path);

    public abstract String imagePath(ObjectAdapter object);

    public abstract String imagePath(ObjectSpecification specification);

    public abstract void forward(String view);

    public abstract void redirectTo(String view);

    public abstract String getContextPath();

    public abstract String getUrlBase();

    public abstract String getHeader(String name);

    public abstract String getQueryString();

    public abstract void startHttpSession();

    public abstract String clearSession();

    public abstract boolean isAborted();

    public abstract String getErrorReference();

    public abstract String getErrorMessage();

    public abstract String getErrorDetails();

    public void setSession(final AuthenticationSession session) {
        this.session = session;
        addVariable("_auth_session", session, Scope.SESSION);
    }

    public AuthenticationSession getSession() {
        return session;
    }

    public abstract String getUri();

    public void raiseError(final int status, final ErrorCollator errorDetails) {
    }

    public void setContentType(final String string) {
    }

    public void setSessionData(final Map<String, Object> hashMap) {
        variables.put(Scope.SESSION, hashMap);
        session = (AuthenticationSession) getVariable("_auth_session");
        Boolean authenticated = (Boolean) getVariable("_authenticated");
        isUserAuthenticated = authenticated != null && authenticated.booleanValue();
    }

    public Map<String, Object> getSessionData() {
        return variables.get(Scope.SESSION);
    }

    public Debug getDebug() {
        return debug;
    }

    public boolean isDebugDisabled() {
        return !debugUsers.isDebugEnabled(getSession());
    }

    public boolean isDebug() {
        return getDebug() == Debug.ON;
    }

    public boolean showDebugData() {
        final Boolean variable = (Boolean) getVariable("debug-on");
        return variable != null && variable.booleanValue();
    }

    public String getDebugTrace() {
        return debugTrace.toString().replace('<', '[').replace('>', ']');
    }

    public void appendDebugTrace(final String line) {
        debugTrace.append(line);
    }

    public void clearTransientVariables() {
        objectMapping.endSession();
    }

    public void reset() {
    }

    public boolean isUserAuthenticated() {
        return isUserAuthenticated;
    }

    public void setUserAuthenticated(boolean isUserAuthenticated) {
        this.isUserAuthenticated = isUserAuthenticated;
        addVariable("_authenticated", isUserAuthenticated, Scope.SESSION);
    }


    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected OidMarshaller getOidMarshaller() {
        return oidMarshaller;
    }

}

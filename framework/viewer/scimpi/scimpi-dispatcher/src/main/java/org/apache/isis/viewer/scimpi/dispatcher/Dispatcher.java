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

package org.apache.isis.viewer.scimpi.dispatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebugHtmlString;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.debug.DebugTee;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.scimpi.dispatcher.action.ActionAction;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Debug;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugAction;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugUserAction;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugUsers;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugWriter;
import org.apache.isis.viewer.scimpi.dispatcher.debug.LogAction;
import org.apache.isis.viewer.scimpi.dispatcher.edit.EditAction;
import org.apache.isis.viewer.scimpi.dispatcher.edit.RemoveAction;
import org.apache.isis.viewer.scimpi.dispatcher.logon.LogonAction;
import org.apache.isis.viewer.scimpi.dispatcher.logon.LogoutAction;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Encoder;
import org.apache.isis.viewer.scimpi.dispatcher.processor.HtmlFileParser;
import org.apache.isis.viewer.scimpi.dispatcher.processor.ProcessorLookup;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.processor.SimpleEncoder;
import org.apache.isis.viewer.scimpi.dispatcher.processor.TagProcessingException;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;
import org.apache.isis.viewer.scimpi.dispatcher.view.Snippet;

public class Dispatcher {
    public static final String ACTION = "_action";
    public static final String EDIT = "_edit";
    public static final String REMOVE = "_remove";
    public static final String GENERIC = "_generic";
    public static final String EXTENSION = "shtml";
    private static final Logger LOG = Logger.getLogger(Dispatcher.class);
    public static final String COMMAND_ROOT = ".app";
    private final Map<String, Action> actions = new HashMap<String, Action>();
    private final Map<String, String> parameters = new HashMap<String, String>();
    private final ProcessorLookup processors = new ProcessorLookup();
    private final HtmlFileParser parser = new HtmlFileParser(processors);
    private final Encoder encoder = new SimpleEncoder();

    public void process(final RequestContext context, final String servletPath) {
        LOG.debug("processing request " + servletPath);
        final AuthenticationSession session = UserManager.startRequest(context);
        LOG.debug("exsiting session: " + session);

        IsisContext.getPersistenceSession().getTransactionManager().startTransaction();
        context.setRequestPath(servletPath);
        context.startRequest();

        try {
            processActions(context, false, servletPath);
            processTheView(context);
        } catch (final ScimpiNotFoundException e) {
            if (context.isInternalRequest()) {
                LOG.error("invalid page request (from within application): " + e.getMessage());
            } else {
                LOG.info("invalid page request (from outside application): " + e.getMessage());
            }

            try {
                // TODO pick options up from configuration
                // context.raiseError(404);
                // context.setRequestPath("/error/notfound_404.shtml");
                IsisContext.getMessageBroker().addWarning("Failed to find page " + servletPath + ". Please navigate from here");
                context.setRequestPath("/index.shtml");
                processTheView(context);
            } catch (final IOException e1) {
                throw new ScimpiException(e);
            }

        } catch (final NotLoggedInException e) {
            IsisContext.getMessageBroker().addWarning("You are not currently logged in! Please log in so you can continue.");
            context.setRequestPath("/login.shtml");
            try {
                processTheView(context);
            } catch (final IOException e1) {
                throw new ScimpiException(e1);
            }

        } catch (final Throwable e) {
            final String errorRef = Long.toString(System.currentTimeMillis(), 36).toUpperCase();

            LOG.info("error " + errorRef);
            LOG.debug(e.getMessage(), e);

            prepareErrorDetails(e, context, errorRef, servletPath);

            final PersistenceSession checkSession = IsisContext.getPersistenceSession();
            final IsisTransactionManager transactionManager = checkSession.getTransactionManager();
            if (transactionManager.getTransaction() != null && transactionManager.getTransaction().getState().canAbort()) {
                transactionManager.abortTransaction();
                transactionManager.startTransaction();
            }

            final Throwable ex = e instanceof TagProcessingException ? e.getCause() : e;
            if (ex instanceof ForbiddenException) {
                if (e instanceof TagProcessingException) {
                    context.addVariable("_security-context", ((TagProcessingException) e).getContext(), Scope.ERROR);
                }
                context.addVariable("_security-error", ex.getMessage(), Scope.ERROR);
                context.addVariable("_security-identifier", ((ForbiddenException) ex).getIdentifier(), Scope.ERROR);
                context.addVariable("_security-roles", ((ForbiddenException) ex).getRoles(), Scope.ERROR);

                // TODO allow these values to be got configuration
                // context.raiseError(403);
                // context.setRequestPath("/error/security_403.shtml");
                IsisContext.getMessageBroker().addWarning("You don't have the right permissions to perform this (#" + errorRef + ")" + "<span class=\"debug-link\" onclick=\"$('#security-dump').toggle()\" > ...</span>");
                context.clearVariables(Scope.REQUEST);
                context.setRequestPath("/index.shtml");
                context.setRequestPath("/error/security_403.shtml");
                try {
                    processTheView(context);
                } catch (final IOException e1) {
                    throw new ScimpiException(e);
                }
            } else {
                // TODO allow these values to be got configuration
                // context.raiseError(500);
                // context.setRequestPath("/error/server_500.shtml");

                final String message = "There was a error while processing this request (#" + errorRef + ")" + "<span class=\"debug-link\" onclick=\"$('#error-dump').toggle()\" > ...</span>";
                IsisContext.getMessageBroker().addWarning(message);
                context.clearVariables(Scope.REQUEST);
                context.setRequestPath("/index.shtml");
                try {
                    context.reset();
                    processTheView(context);
                } catch (final TagProcessingException e1) {
                    IsisContext.getMessageBroker().addWarning(message);
                    context.clearVariables(Scope.REQUEST);
                    context.setRequestPath("/error.shtml");
                    try {
                        context.reset();
                        processTheView(context);
                    } catch (final IOException e2) {
                        throw new ScimpiException(e2);
                    }
                } catch (final IOException e1) {
                    throw new ScimpiException(e1);
                }

                // context.forward("/error.shtml");
            }
        } finally {
            try {
                UserManager.endRequest(context.getSession());
            } catch (final Exception e1) {
                LOG.error("endRequest call failed", e1);
            }
        }
    }

    protected void processTheView(final RequestContext context) throws IOException {
        IsisTransactionManager transactionManager = IsisContext.getPersistenceSession().getTransactionManager();
        if (transactionManager.getTransaction().getState().canFlush()) {
            transactionManager.flushTransaction();
        }
        processView(context);
        // Note - the session will have changed since the earlier call if a user
        // has logged in or out in the action
        // processing above.
        transactionManager = IsisContext.getPersistenceSession().getTransactionManager();
        if (transactionManager.getTransaction().getState().canCommit()) {
            IsisContext.getPersistenceSession().getTransactionManager().endTransaction();
        }

        context.endRequest();
        UserManager.endRequest(context.getSession());
    }

    private void prepareErrorDetails(final Throwable exception, final RequestContext requestContext, final String errorRef, final String servletPath) {
        final DebugString debugText = new DebugString();
        final DebugHtmlString debugHtml = new DebugHtmlString();
        final DebugBuilder debug = new DebugTee(debugText, debugHtml);

        try {
            debug.startSection("Exception");
            debug.appendException(exception);
            debug.endSection();
        } catch (final RuntimeException e) {
            debug.appendln("NOTE - an exception occurred while dumping an exception!");
            debug.appendException(e);
        }

        if (IsisContext.getCurrentTransaction() != null) {
            final List<String> messages = IsisContext.getMessageBroker().getMessages();
            final List<String> warnings = IsisContext.getMessageBroker().getWarnings();
            if (messages.size() > 0 || messages.size() > 0) {
                debug.startSection("Warnings/Messages");
                for (final String message : messages) {
                    debug.appendln("message", message);
                }
                for (final String message : warnings) {
                    debug.appendln("warning", message);
                }
            }
        }

        requestContext.append(debug);

        debug.startSection("Processing Trace");
        debug.appendPreformatted(requestContext.getDebugTrace());
        debug.endSection();
        debug.close();

        PrintWriter writer;
        try {
            final String directory = IsisContext.getConfiguration().getString(ConfigurationConstants.ROOT + "scimpi.error-snapshots", ".");
            final File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            writer = new PrintWriter(new File(dir, "error_" + errorRef + ".html"));
            final DebugWriter writer2 = new DebugWriter(writer, true);
            writer2.concat(debugHtml);
            writer2.close();
            writer.close();
        } catch (final FileNotFoundException e) {
            LOG.error("Failed to archive error page", e);
        }

        final String replace = "";
        final String withReplacement = "";
        final String message = exception.getMessage();
        requestContext.addVariable("_error-message", message == null ? "" : message.replaceAll(replace, withReplacement), Scope.ERROR);
        requestContext.addVariable("_error-details", debugHtml.toString().replaceAll(replace, withReplacement), Scope.ERROR);
        requestContext.addVariable("_error-ref", errorRef, Scope.ERROR);
        requestContext.clearTransientVariables();

        final String msg = "failed during request for " + servletPath;
        LOG.error(msg + " (#" + errorRef + ")\n" + message + "\n" + debugText + "\n" + msg);
    }

    public void addParameter(final String name, final String value) {
        parameters.put(name, value);
    }

    private String getParameter(final String name) {
        return parameters.get(name);
    }

    private void processActions(final RequestContext context, final boolean userLoggedIn, final String actionName) throws IOException {
        if (actionName.endsWith(COMMAND_ROOT)) {
            final int pos = actionName.lastIndexOf('/');
            final Action action = actions.get(actionName.substring(pos, actionName.length() - COMMAND_ROOT.length()));
            if (action == null) {
                throw new ScimpiException("No logic for " + actionName);
            }

            LOG.debug("processing action: " + action);
            action.process(context);
            final String fowardTo = context.forwardTo();
            if (fowardTo != null) {
                processActions(context, true, fowardTo);
            }
        }
    }

    private void processView(final RequestContext context) throws IOException {
        String file = context.getRequestedFile();
        if (file == null) {
            LOG.warn("No view specified to process");
            return;
        }
        if (file.endsWith(COMMAND_ROOT)) {
            return;
        }
        file = determineFile(context, file);
        final String fullPath = context.requestedFilePath(file);
        LOG.debug("processing file " + fullPath);
        context.setResourcePath(fullPath);

        context.setContentType("text/html");

        context.addVariable("title", "Untitled Page", Scope.REQUEST);
        final Stack<Snippet> tags = loadPageTemplate(context, fullPath);
        final Request request = new Request(file, context, encoder, tags, processors);
        request.appendDebug("processing " + fullPath);
        try {
            request.processNextTag();
            noteIfMessagesHaveNotBeenDisplay(context);
            IsisContext.getUpdateNotifier().clear();
        } catch (final RuntimeException e) {
            IsisContext.getMessageBroker().getMessages();
            IsisContext.getMessageBroker().getWarnings();
            IsisContext.getUpdateNotifier().clear();
            throw e;
        }
        final String page = request.popBuffer();
        final PrintWriter writer = context.getWriter();
        writer.write(page);
        if (context.getDebug() == Debug.PAGE) {
            final DebugWriter view = new DebugWriter(writer, false);
            context.append(view);
        }
    }

    public void noteIfMessagesHaveNotBeenDisplay(final RequestContext context) {
        final List<String> messages = IsisContext.getMessageBroker().getMessages();
        if (messages.size() > 0) {
            // TODO write out all messages
            context.getWriter().println("Note - messages existed but where not displayed");
        }
        final List<String> warnings = IsisContext.getMessageBroker().getWarnings();
        if (warnings.size() > 0) {
            // TODO write out all warning
            context.getWriter().println("Note - warnings existed but where not displayed");
        }
    }

    private String determineFile(final RequestContext context, String file) {
        final String fileName = file.trim();
        if (fileName.startsWith(GENERIC)) {
            final Object result = context.getVariable(RequestContext.RESULT);
            final ObjectAdapter mappedObject = MethodsUtils.findObject(context, (String) result);
            if (mappedObject == null) {
                throw new ScimpiException("No object mapping for " + result);
            }
            if (fileName.equals(GENERIC + "." + EXTENSION)) {
                final Facet facet = mappedObject.getSpecification().getFacet(CollectionFacet.class);
                if (facet != null) {
                    final ObjectSpecification specification = mappedObject.getSpecification();
                    final TypeOfFacet facet2 = specification.getFacet(TypeOfFacet.class);
                    file = findFileForSpecification(context, facet2.valueSpec(), "collection", EXTENSION);
                } else {
                    final ObjectAdapter mappedObject2 = mappedObject;
                    if (mappedObject2.isTransient()) {
                        file = findFileForSpecification(context, mappedObject.getSpecification(), "edit", EXTENSION);
                    } else {
                        file = findFileForSpecification(context, mappedObject.getSpecification(), "object", EXTENSION);
                    }
                }
            } else if (fileName.equals(GENERIC + EDIT + "." + EXTENSION)) {
                file = findFileForSpecification(context, mappedObject.getSpecification(), "edit", EXTENSION);
            } else if (fileName.equals(GENERIC + ACTION + "." + EXTENSION)) {
                final String method = context.getParameter("method");
                file = findFileForSpecification(context, mappedObject.getSpecification(), method, "action", EXTENSION);
            }
        }
        return file;
    }

    private String findFileForSpecification(final RequestContext context, final ObjectSpecification specification, final String name, final String extension) {
        return findFileForSpecification(context, specification, name, name, extension);
    }

    private String findFileForSpecification(final RequestContext context, final ObjectSpecification specification, final String name, final String defaultName, final String extension) {

        String find = findFile(context, specification, name, extension);
        if (find == null) {
            find = "/generic/" + defaultName + "." + extension;
        }
        return find;
    }

    private String findFile(final RequestContext context, final ObjectSpecification specification, final String name, final String extension) {
        final String className = specification.getShortIdentifier();
        String fileName = context.findFile("/" + className + "/" + name + "." + extension);
        if (fileName == null) {
            final List<ObjectSpecification> interfaces = specification.interfaces();
            for (int i = 0; i < interfaces.size(); i++) {
                fileName = findFile(context, interfaces.get(i), name, extension);
                if (fileName != null) {
                    return fileName;
                }
            }
            if (specification.superclass() != null) {
                fileName = findFile(context, specification.superclass(), name, extension);
            }
        }
        return fileName;
    }

    private Stack<Snippet> loadPageTemplate(final RequestContext context, final String path) throws IOException, FileNotFoundException {
        // TODO cache stacks and check for them first
        copyParametersToVariableList(context);
        LOG.debug("parsing source " + path);
        return parser.parseHtmlFile(path, context);
    }

    private void copyParametersToVariableList(final RequestContext context) {
        /*
         * Enumeration parameterNames = context.getParameterNames(); while
         * (parameterNames.hasMoreElements()) { String name = (String)
         * parameterNames.nextElement(); if (!name.equals("view")) {
         * context.addVariable(name, context.getParameter(name), Scope.REQUEST);
         * } }
         */
    }

    public void init(final String dir, final DebugUsers debugUsers) {
        addAction(new ActionAction());

        // TODO remove
        addAction(new DebugAction(this));
        addAction(new DebugUserAction(debugUsers));
        addAction(new EditAction());
        addAction(new RemoveAction());
        addAction(new LogonAction());
        addAction(new LogoutAction());
        addAction(new LogAction());

        final String configFile = getParameter("config");
        if (configFile != null) {
            final File file = new File(dir, configFile);
            if (file.exists()) {
                loadConfigFile(file);
            } else {
                throw new ScimpiException("Configuration file not found: " + configFile);
            }
        }

        processors.init();
        processors.addElementProcessor(new org.apache.isis.viewer.scimpi.dispatcher.view.debug.Debug(this));
    }

    private void loadConfigFile(final File file) {
        try {
            Document document;
            final SAXReader reader = new SAXReader();
            document = reader.read(file);
            final Element root = document.getRootElement();
            for (final Iterator i = root.elementIterator(); i.hasNext();) {
                final Element element = (Element) i.next();

                if (element.getName().equals("actions")) {
                    for (final Iterator actions = element.elementIterator("action"); actions.hasNext();) {
                        final Element action = (Element) actions.next();
                        final String className = action.getText();
                        final Action instance = (Action) InstanceUtil.createInstance(className);
                        addAction(instance);
                    }
                }

                if (element.getName().equals("processors")) {
                    for (final Iterator processors = element.elementIterator("processor"); processors.hasNext();) {
                        final Element processor = (Element) processors.next();
                        final String className = processor.getText();
                        final ElementProcessor instance = (ElementProcessor) InstanceUtil.createInstance(className);
                        this.processors.addElementProcessor(instance);
                    }
                }

            }
        } catch (final MalformedURLException e) {
            throw new IsisException(e);
        } catch (final DocumentException e) {
            throw new IsisException(e);
        }

    }

    private void addAction(final Action action) {
        actions.put("/" + action.getName(), action);
        action.init();
    }

    public void debug(final DebugBuilder debug) {
        debug.startSection("Actions");
        final Set<String> keySet = actions.keySet();
        final ArrayList<String> list = new ArrayList<String>(keySet);
        Collections.sort(list);
        for (final String name : list) {
            debug.appendln(name, actions.get(name));
        }
        /*
         * new ArrayList<E>(actions.keySet().iterator()) Iterator<String> names
         * = Collections.sort().iterator(); while (names.hasNext()) { String
         * name = names.next(); view.appendRow(name, actions.get(name)); }
         */
        final Iterator<Action> iterator = actions.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().debug(debug);
        }

        processors.debug(debug);
    }
}

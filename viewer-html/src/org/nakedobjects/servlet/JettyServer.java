package org.nakedobjects.servlet;

import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.ObjectStoreException;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mortbay.http.HttpContext;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.util.MultiException;


public class JettyServer {	
	static NakedObjectStore objectStore;

	public static void main(String[] args) throws MultiException, ObjectStoreException, IOException {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ALL);
	    
		Server server = new Server();
		SocketListener listener = new SocketListener();
		listener.setPort(2080);
		server.addListener(listener);

		HttpContext context = new HttpContext();
		context.setContextPath("/");

		// servlets
		// Create a servlet container
		ServletHandler servlets = new ServletHandler();
		context.addHandler(servlets);

		// Map a servlet onto the container
		servlets.addServlet("logon", "/logon/*", "org.nakedobjects.servlet.LogonServlet");
		servlets.addServlet("classes", "/classes/*", "org.nakedobjects.servlet.ClassViewerServlet");
		servlets.addServlet("instances", "/instances/*", "org.nakedobjects.servlet.InstancesServlet");

		
		/*
		 * 
		servlets.addServlet("nakedobjects", "/nakedobjects/*", "org.nakedobjects.servlet.NakedObjectsServlet");
		
		servlets.addServlet("object", "/object/*", "org.nakedobjects.servlet.ObjectViewerServlet");
		servlets.addServlet("classes", "/classes/*", "org.nakedobjects.servlet.ClassViewerServlet");
		servlets.addServlet("newinstance", "/newinstance/*", "org.nakedobjects.servlet.NewInstanceServlet");
		servlets.addServlet("logout", "/logout/*", "org.nakedobjects.servlet.LogoutServlet");
		*/
		
		// Serve static content from the context
		//		String home = System.getProperty("jetty.home", ".");
		//		context.setResourceBase(home + "/demo/webapps/jetty/tut/");
		//		context.addHandler(new ResourceHandler());
		 
		context.setResourceBase("D:\\no-development\\nakedobjects\\viewer-html\\src\\web");
		context.addHandler(new ResourceHandler());
		server.addContext(context);

		server.start();
	}
}

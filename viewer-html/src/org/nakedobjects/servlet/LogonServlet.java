package org.nakedobjects.servlet;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationPropertiesLoader;
import org.nakedobjects.example.ecs.fixtures.BookingsFixture;
import org.nakedobjects.example.ecs.fixtures.CitiesFixture;
import org.nakedobjects.example.ecs.fixtures.ClassesFixture;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.NakedObjectSpecificationImpl;
import org.nakedobjects.object.defaults.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.help.HelpManagerAssist;
import org.nakedobjects.object.help.SimpleHelpManager;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.defaults.LocalObjectManager;
import org.nakedobjects.object.persistence.defaults.SimpleOidGenerator;
import org.nakedobjects.object.persistence.defaults.TransientObjectStore;
import org.nakedobjects.object.reflect.PojoAdapterFactoryImpl;
import org.nakedobjects.object.reflect.PojoAdapterHashImpl;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.control.SimpleSession;
import org.nakedobjects.reflector.java.fixture.JavaFixtureBuilder;
import org.nakedobjects.reflector.java.reflect.JavaReflectorFactory;
import org.nakedobjects.utility.StartupException;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class LogonServlet extends AbstractObjectViewerServlet {
    private WebApplicationContext context;
    
    
	public String getServletInfo() {
		return "Logon servlet";
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		ServletOutputStream out = response.getOutputStream();
		DataInputStream fileInputStream = null;

		try {
				// login
				HttpSession session = request.getSession();
				session.invalidate();
				session = request.getSession();
	/*			
//				NakedObjectStore os = new XmlObjectStore("/home/rcm/nakedobjects/eclipse/library/data/");
				NakedObjectStore os = new XmlObjectStore("/home/rcm/nakedobjects/eclipse/expenses/xml-expenses-data/");
				os.init();
				AbstractNakedObject.init(os);
				os.setUpdateNotifier(new NullUpdateNotifier());
				session.setAttribute("os", os);
			*/	
				String username = request.getParameter("user");
				String password = request.getParameter("password");
/*
				Session.initSession(os, null);
				Session.logon(user, password);
*/
				session.setAttribute("user", username);

				session.setAttribute("naked-objects", NakedObjectsSession);
				session.setAttribute("context", context);
				
				/*		
				NakedClassList classes = new NakedClassList("");
//				classes.addClass(org.nakedobjects.example.library.Member.class);
//				classes.addClass(org.nakedobjects.example.library.Book.class);
//				classes.addClass(org.nakedobjects.example.library.Loan.class);
//				
				classes.addClass(Workspace.class);
				classes.addClass(Claim.class);
*/				
	//			session.setAttribute("classes", classes);
				
				Vector history = new Vector();
				session.setAttribute("history", history);
				
				out.println("<p>Logged in to naked objects server");
			
				response.sendRedirect("nakedobjects");
		
			
		} catch (IOException e) {
			e.printStackTrace();

			return;
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException ignoree) {
				}
			}
		}
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		
        Configuration configuration = new Configuration(new ConfigurationPropertiesLoader("nakedobjects.properties", false));

        NakedObjects nakedObjects = new  NakedObjectsClient();
        nakedObjects.setConfiguration(configuration);

        JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();

        JavaObjectFactory objectFactory = new JavaObjectFactory();
        objectFactory.setContainer(container);

        container.setObjectFactory(objectFactory);

        TransientObjectStore objectStore = new TransientObjectStore();

        OidGenerator oidGenerator = new SimpleOidGenerator();

        LocalObjectManager objectManager = new LocalObjectManager();
        objectManager.setObjectStore(objectStore);
        objectManager.setObjectFactory(objectFactory);
        objectManager.setOidGenerator(oidGenerator);

        nakedObjects.setObjectManager(objectManager);

        NakedObjectSpecificationLoaderImpl specificationLoader = new NakedObjectSpecificationLoaderImpl();

        nakedObjects.setSpecificationLoader(specificationLoader);
        
        LocalReflectionFactory reflectionFactory = new LocalReflectionFactory();
        HelpManagerAssist helpManager = new HelpManagerAssist();
        helpManager.setDecorated(new SimpleHelpManager());
        reflectionFactory.setHelpManager(helpManager);
        
        JavaReflectorFactory reflectorFactory = new JavaReflectorFactory();

        PojoAdapterFactoryImpl pojoAdapterFactory = new PojoAdapterFactoryImpl();
        pojoAdapterFactory.setPojoAdapterHash(new PojoAdapterHashImpl());
        pojoAdapterFactory.setReflectorFactory(reflectorFactory);
        nakedObjects.setPojoAdapterFactory(pojoAdapterFactory);
        
        NakedObjectSpecificationImpl.setReflectionFactory(reflectionFactory);
        specificationLoader.setReflectorFactory(reflectorFactory);

        reflectorFactory.setObjectFactory(objectFactory);

        nakedObjects.setSession(new SimpleSession());


        try {
            objectManager.init();
        } catch (StartupException e) {
            throw new NakedObjectRuntimeException(e);
        }

        JavaFixtureBuilder builder = new JavaFixtureBuilder();        
        CitiesFixture cities;
        builder.addFixture(cities = new CitiesFixture());
        builder.addFixture(new BookingsFixture(cities));
        builder.addFixture(new ClassesFixture());
        builder.installFixtures();

        String[] classes = builder.getClasses();
        context = new WebApplicationContext();
        context.setName("ECS Example");
        for (int i = 0; i < classes.length; i++) {
            context.addClass(classes[i]);
        }

		
		config.getServletContext().log("Initializing the logon servlet");
	}

	protected String objectLink(NakedObject object, NakedObject returnObject) {
		String param = "id=" + serialNumber(object) + "&return=" +
			serialNumber(returnObject);

		return "<a href=\"object?" + param + "\">" + image(object, 16) + object.titleString().toString() +
		"</a> ";
	}
}

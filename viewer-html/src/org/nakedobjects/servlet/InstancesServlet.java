package org.nakedobjects.servlet;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.persistence.defaults.SerialOid;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class InstancesServlet extends AbstractObjectViewerServlet {
    public String getServletInfo() {
        return "Instances servlet";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        ServletOutputStream out = response.getOutputStream();
        DataInputStream fileInputStream = null;

        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");
/*
            HttpSession session = request.getSession(false);
            if(session == null) {
            	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Out of session");
            }
            NakedObjectManager os = (NakedObjectManager) session.getAttribute("om");
    */         
            String cls = request.getParameter("class");
            String returnId = request.getParameter("return");

            NakedObjectSpecification spec = NakedObjects.getSpecificationLoader().loadSpecification(cls);
			out.println("<h2>" + spec.getPluralName() + "</h2>");

            NakedCollection c = NakedObjects.getObjectManager().allInstances(spec, false); 
            Enumeration e = c.elements();
            while (e.hasMoreElements()) {
                NakedObject instance = (NakedObject) e.nextElement();
                out.println("<p> <a href=\"object?id=" + serialNumber(instance) +
                    "\">" + image(instance, 16) + instance.titleString().toString() +
                    "</a> ");
            }

            out.println("<hr>");
            
            out.println("<p><small>To <a href=\"/classes\">classes</a> list</small><p>");
            out.println("<p><small>Log <a href=\"/logout\">out</a></small><p>");
            
            if (returnId != null) {
                NakedObject returnObject = NakedObjects.getObjectManager().getObject(new SerialOid(
                            Long.valueOf(returnId).longValue()), null);
                String param = "id=" + returnId;

                out.println("<p><small>Return to <a href=\"object?" + param + "\">" +
                    image(returnObject, 16) + returnObject.titleString().toString() +
                    "<small></a> ");
            }

            out.flush();
        } catch (ObjectStoreException e) {
            out.println("<p>Exception in object store " + e);
        } catch (IOException e) {
            System.err.println("IO Error " + e);
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

        new NakedObjectsClient();
        config.getServletContext().log("Initializing the instances servlet");
    }

    protected String objectLink(NakedObject object, NakedObject returnObject) {
        String param = "id=" + serialNumber(object) +
            "&return=" + serialNumber(returnObject);

        return "<a href=\"object?" + param + "\">" + image(object, 16) +
        object.titleString().toString() + "</a> ";
    }
}

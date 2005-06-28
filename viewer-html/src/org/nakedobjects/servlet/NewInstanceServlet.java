package org.nakedobjects.servlet;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.persistence.defaults.SerialOid;

import java.io.DataInputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class NewInstanceServlet extends AbstractObjectViewerServlet {
    public String getServletInfo() {
        return "New instance servlet";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        ServletOutputStream out = response.getOutputStream();
        DataInputStream fileInputStream = null;

        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");

            String cls = request.getParameter("class");

            String returnId = request.getParameter("return");

            out.println("<p>New " + cls);

            HttpSession session = request.getSession(false);
            if(session == null) {
            	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Out of session");
            }
 //           NakedObjectStore os = (NakedObjectStore) session.getAttribute("os");
            
            //NakedObject object = (NakedObject) NakedObjects.getSpecificationLoader().loadSpecification(cls).acquireInstance();
            //object.created();
            NakedObjectManager objectManager = NakedObjects.getObjectManager();
            NakedObject object = objectManager.createInstance(cls);

            form(out, object);

            out.println("<hr>");

            if (returnId != null) {
                NakedObject returnObject = objectManager.getObject(new SerialOid(
                            Long.valueOf(returnId).longValue()), null);
                String param = "id=" + returnId;

                out.println("<p>Return to <a href=\"object?" + param + "\">" +
                    image(returnObject, 16) + returnObject.titleString().toString() +
                    "</a> ");
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

        config.getServletContext().log("Initializing the viewer servlet");
    }

    protected String objectLink(NakedObject object, NakedObject returnObject) {
        String param = "id=" + serialNumber(object) +
            "&return=" + serialNumber(returnObject);

        return "<a href=\"object?" + param + "\">" + image(object, 16) +
        object.titleString().toString() + "</a> ";
    }
}

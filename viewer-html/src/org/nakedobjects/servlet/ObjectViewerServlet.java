package org.nakedobjects.servlet;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
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


public class ObjectViewerServlet extends AbstractObjectViewerServlet {
    public String getServletInfo() {
        return "Object viewer servlet";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        ServletOutputStream out = response.getOutputStream();
        DataInputStream fileInputStream = null;

        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");

            String idParam = request.getParameter("id");
            long id = Long.valueOf(idParam).longValue();

            HttpSession session = request.getSession(false);
            if(session == null) {
            	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Out of session");
            }
            
            NakedObjectStore os = (NakedObjectStore) session.getAttribute("os");
            
            NakedObject object = os.getObject(new SerialOid(id), null);

            out.println("<p>object=<i>" + id + " " + object + "</i>");

            if (object == null) {
                out.println("<p>Invalid OID!");
            } else {
                form(out, object);
            }

            out.println("<hr>");

            out.println("<p><small>To <a href=\"/classes\">classes</a> list</small><p>");
            out.println("<p><small>Log <a href=\"/logout\">out</a></small><p>");
            
            String returnIdParam = request.getParameter("return");
            if (returnIdParam != null) {
            	long returnId = Long.valueOf(returnIdParam).longValue();
            	NakedObject returnObject = (NakedObject) os.getObject(new SerialOid(returnId), null);
                String param = "id=" + returnId;

                out.println("<p>Return to <a href=\"object?" + param + "\">" +
                    image(returnObject, 16) + returnObject.titleString().toString() +
                    "</a> ");
            }

            out.flush();
        } catch (IOException e) {
            System.err.println("IO Error " + e);
            e.printStackTrace();

            return;
        } catch (ObjectNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ObjectStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
        String param = "id=" + serialNumber(object) + "&return=" +
            serialNumber(returnObject);

        return "<a href=\"object?" + param + "\">" + image(object, 16) +
        object.titleString() + "</a> ";
    }
}

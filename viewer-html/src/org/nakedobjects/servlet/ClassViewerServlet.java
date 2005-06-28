package org.nakedobjects.servlet;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class ClassViewerServlet extends AbstractObjectViewerServlet {
	public String getServletInfo() {
		return "Class viewer servlet";
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		ServletOutputStream out = response.getOutputStream();
		DataInputStream fileInputStream = null;

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/html");
		out.println("<p>[ClassViewerServlet]");

		try {
			HttpSession session = request.getSession(false);
			if(session == null) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Out of session");
			}

			WebApplicationContext context = (WebApplicationContext) session.getAttribute("context");
			
		    out.println("<h1>" +  context.name() + "</h1>");
			
			Vector classes = context.getClasses();
			Enumeration e = classes.elements();
			while (e.hasMoreElements()) {
				NakedClass element = (NakedClass) e.nextElement();

				out.println("<p>" + image(element.getShortName(), 32) + element.title() + " <a href=\"/instances?class=" + element.getFullName() + "\">list</a>  <a href=\"/newinstance?class=" + element.getFullName() + "\">new</a>");
			}
			
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		config.getServletContext().log("Initializing the classes servlet");
	}

	protected String objectLink(NakedObject object, NakedObject returnObject) {
		String param = "id=" + ((SimpleOid) object.getOid()).getSerialNo() + "&return=" +
			((SimpleOid) returnObject.getOid()).getSerialNo();

		return "<a href=\"object?" + param + "\">" + image(object, 16) + object.title().toString() +
		"</a> ";
	}
}

package org.nakedobjects.servlet;

import java.io.DataInputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class LogoutServlet extends HttpServlet {
	public String getServletInfo() {
		return "Naked objects 'logout' servlet";
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		ServletOutputStream out = response.getOutputStream();
		DataInputStream fileInputStream = null;

		try {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/html");
			
			HttpSession session = request.getSession();
			String username = (String) session.getAttribute("user");
            UserManager.getInstance().logoff(username);
			
            session.invalidate();
			
			out.println("<p>" + username + " logged out.  Thank you for browsing.");

			out.println("<p><a href=\"/index.html\">Login again</a>");
			
			
			out.println("<p>Debug: user " + session.getAttribute("user"));
			
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

		config.getServletContext().log("Initializing the login servlet");
	}
}

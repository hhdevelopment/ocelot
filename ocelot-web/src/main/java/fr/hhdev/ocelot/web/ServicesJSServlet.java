/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.web;

import fr.hhdev.ocelot.Constants;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to serve ocelot-services.js ocelot-services is enerated by annotation process
 *
 * @author hhfrancois
 */
@WebServlet(urlPatterns = {"/" + Constants.OCELOT_SERVICES_JS})
public class ServicesJSServlet extends HttpServlet {

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType(Constants.JSTYPE);
		String minify = request.getParameter(Constants.MINIFY_PARAMETER);
		String filename = request.getServletContext().getInitParameter(Constants.OCELOT_SERVICES_JS);
		try (Writer out = response.getWriter()) {
			if (Constants.FALSE.equalsIgnoreCase(minify)) {
				try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						out.write(inputLine);
						out.write("\n");
					}
				}
			} else { // TODO Implement minification
				try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						out.write(inputLine);
						out.write("\n");
					}
				}
			}
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			  throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			  throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>

}

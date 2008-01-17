package org.eclipse.epp.usagedata.internal.recording.uploading.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UploadGoodServlet extends HttpServlet {
	private static final long serialVersionUID = 863695768810232317L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		PrintWriter writer = response.getWriter();
		writer.println("log:received!");
	}
}

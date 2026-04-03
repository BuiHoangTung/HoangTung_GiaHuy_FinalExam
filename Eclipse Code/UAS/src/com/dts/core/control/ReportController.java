package com.dts.core.control;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReportController extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException {
		try {
			System.out.println("Hello, world from Bui Hoang Tung");
			System.out.println("Hello, world from Gia Huy");
			res.sendRedirect("ReportDateSelector.jsp");
		} catch (IOException ioe) {
		}
	}
}

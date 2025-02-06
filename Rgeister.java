import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/regForm")
public class Register extends HttpServlet
{
	

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		PrintWriter out = resp.getWriter();

		//1. extract phone number from the register.jsp form
		String phoneNumber = req.getParameter("phoneNumber");
	
		try {
            // 2. create a webrequest to springMVC twilio service
			
			// form url
            URL url = new URL("http://localhost:8089/router/sendOTP"); // Replace with your server URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set HTTP method to POST
            connection.setRequestMethod("POST");

            // Set headers for JSON content
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // Enable output for sending JSON data
            connection.setDoOutput(true);

            // JSON payload
            String jsonInputString = "{ \"phoneNumber\": \"+91" + phoneNumber+ "\", \"userName\": \"mizhan\" }";

            // Write JSON data to output stream
            try (OutputStream os = connection.getOutputStream()) 
            {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get response status
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Optional: Handle response if needed
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("POST request sent successfully!");
                
                //3.  create cookie to store the phonenumber so that we can leverage it later to validate the otp
        		Cookie newCookie = new Cookie("phoneNumber", phoneNumber);		
        		resp.addCookie(newCookie);
        		
        		//4. redirect to entering the otp page
                resp.sendRedirect("/Registration_Form/LoginOtp.jsp");
            } 
            else 
            {
                System.out.println("POST request failed!");
            }

            // Close the connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
}

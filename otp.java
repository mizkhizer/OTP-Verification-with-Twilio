import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/otpNumber")
public class Otp extends HttpServlet
{
	
	// method to save the phone number and otp in db
	protected static void saveDb(String phoneNumber, String otp) 
	{
		try
		{
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/generateotp", "root", "mizan");
			
			PreparedStatement ps = conn.prepareStatement("insert into generateotp values(?,?,?)");       // ? this symbol we called as Positional Parameters
			ps.setString(1, phoneNumber);
			ps.setString(2, otp);
			ps.setString(3, LocalDateTime.now().toString());
			
			
			int count = ps.executeUpdate();
			System.out.println(count>0 ? "Successfully saved in db" : "Failed to save in Db");
	
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
	}
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		PrintWriter out = null;
		try 
		{
			out = resp.getWriter();
		} 
		catch (IOException e) 
		{
			
			e.printStackTrace();
		}
		
		// 1. retrieve the otp from the form welcomeotp.jsp
		String enterOtp = req.getParameter("otpGenerate");
		
		// 2. extract the phone number from the cookie saved
		Cookie[] cookies = req.getCookies();
		String cookieName = "phoneNumber";
		
		String phoneNumber = "";
		
		for ( int i=0; i<cookies.length; i++) {
		      Cookie cookie = cookies[i];
		      if (cookieName.equals(cookie.getName())) {
		    	  phoneNumber = cookie.getValue();
		    }	
		  }
		
		System.out.println("entered val = " + phoneNumber + " --" + enterOtp);
		String responseValue = "";
		
		// 3. validate otp
		try {
            // Server URL
            URL url = new URL("http://localhost:8089/router/validateOTP"); // Replace with your server URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set HTTP method to POST
            connection.setRequestMethod("POST");

            // Set headers for JSON content
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // Enable output for sending JSON data
            connection.setDoOutput(true);

            // JSON payload
            String jsonInputString = "{ \"oneTimePassword\": \"" + enterOtp + "\", \"phoneNumber\": \"" + phoneNumber+ "\" }";

            // Write JSON data to output stream
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get response status
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Optional: Handle response if needed
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("POST request sent successfully!!!");
                responseValue = connection.getResponseMessage();
                
    			Otp.saveDb( phoneNumber,  enterOtp);
    			// 5. redirect after the validation is successful
    			resp.sendRedirect("/Registration_Form/WelcomeOtp.jsp");
            } else {
                System.out.println("POST request failed!");
                resp.sendRedirect("/Registration_Form/error.jsp");
            }

            // Close the connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect("/Registration_Form/error.jsp");
        } 
		
	}
}

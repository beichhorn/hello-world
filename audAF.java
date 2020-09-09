//********************************************************************************//
//                                                                                //
// Title       : PW.java
//
// Description : Create user account information in JSON format                   //
//               to be sent to a API though HTTP.                                 //
// Author      : Bernard Eichhorn - Paragon Consulting Services                   //
// Date        : 7/13/2020                                                        //
//                                                                                //
//********************************************************************************//


import com.ibm.as400.access.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.*;
import java.util.*;
import java.sql.*;	

class audAF   {
	
	//*****************************************************************************//	
	// Get Database Connection
	//*****************************************************************************//
    public static Connection getConn () {
    	Connection dbConn = null;
    	
    	try {	
    		Properties properties = new Properties ();
    		properties.put("user", "BEICHHORN");
    		properties.put("password", "ROSETL");
    		Class.forName("com.ibm.db2.jdbc.app.DB2Driver");
    		dbConn = DriverManager.getConnection("jdbc:db2:*local", properties);	
    	}
    	catch (Exception e) {
          System.out.println(e);
          dbConn = null;
    	}
      return dbConn;
    }
    //*****************************************************************************//
    // Post data through HTTP
    //*****************************************************************************//
    public static void postData (String jsonInputString) {
    	
    	try  {
         // URL url = new URL("https://www.wnba.com/");
	     URL url = new URL("https://demo.securityeco.com/ibm");
	     HttpURLConnection conn  = (HttpURLConnection)url.openConnection();

	     conn.setRequestMethod("POST");
	     conn.setRequestProperty("Content-Type", "application/json; utf-8");
	     conn.setRequestProperty("Accept", "application/json");
	     conn.setDoOutput(true);

	     System.out.println(jsonInputString);
	     OutputStream os = conn.getOutputStream();
	     byte[] input = jsonInputString.getBytes("utf-8");
	     System.out.println(input.length);

	     os.write(input, 0, input.length);  // Output JSON
	     int rcode = conn.getResponseCode();
	     System.out.println(rcode);

	     BufferedReader br = new BufferedReader(
	     new InputStreamReader(conn.getInputStream(), "utf-8"));
	     StringBuilder response = new StringBuilder();
	     String responseLine = null;

	     while (br.readLine() != null) {
                  responseLine = br.readLine();
	          response.append(responseLine.trim());
             }
    	}
    	catch (Exception e) {
            System.out.println(e);
    	}
    	
    	return;
    	
    }

    //*****************************************************************************//
    //                                                                             //
    // Main method                                                                 //
    //                                                                             //
    //*****************************************************************************//
	
    public static void main (String[] arguments) {

         Connection dbConn      = null;
         String jsonInputString = null;
         Timestamp TimeWritten  = null;
         String TimeGenerated   = null;
         String DateTime        = null;
         String MachineName     = null;
         String Timezone        = "-0500";    // TEMPORARY
         String Source          = "QAUDJRN";
         String Account         = null;
         String Process         = null;
         String Entry           = null;
         // ArrayList<UsrSessionJSON> UserLoginSession;
         String UserLoginSession = null;
         String TimeStampSetVia = null;
         String AccountLoginIP  = null;
         int l                  = 0;
         String line            = null;

        try {
            
            dbConn = getConn();  // Get Database Connection 	
            Statement s = dbConn.createStatement();
            ResultSet rs = s.executeQuery("select * from bjelib.audaf where afonam<>'*N'");
            
           while (rs.next()) {

        	TimeWritten        = new Timestamp(System.currentTimeMillis());
            TimeGenerated      = rs.getString(5);
            MachineName        = rs.getString(15);
            Account            = rs.getString(14); 
            Process            = rs.getString(18);
            Entry              = rs.getString(31) + " " + rs.getString(32).trim() + " " +
            		             rs.getString(33) + " - Authority Failure";

            jsonInputString = "{" +
              "\"TimeWritten\": \""   + TimeWritten   + "\", " +
              "\"TimeGenerated\": \"" + TimeGenerated + "\", " +
              "\"DateTime\": \""      + DateTime      + "\", " +
              "\"MachineName\": \""   + MachineName   + "\", " +
              "\"Timezone\": \""      + Timezone      + "\", " +
              "\"Source\": \""        + Source        + "\", " +
              "\"Account\": \""       + Account       + "\", " +
              "\"Process\": \""       + Process       + "\", " +
              "\"Entry\": \""         + Entry         + "\", " +
              "\"UserLoginSession\": \"" + UserLoginSession + "\"}";

            postData(jsonInputString);

          }

          System.out.println("HELLO WORLD");
        }
        catch (Exception e) {
        	System.out.println(e); 	
        }

    }

}


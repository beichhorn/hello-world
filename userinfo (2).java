//********************************************************************************//
//                                                                                //
// Title       : userinfo.java
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

class userInfo {
	
	//*****************************************************************************//	
	// Get Database Connection 
	//*****************************************************************************//
    public static Connection getConn () { 
    	Connection dbConn = null; 
     String user = null;
     String pw   = null;
    	
    	try {	
      user = getUser();
      pw   = getPw();
    		Properties properties = new Properties ();
    		properties.put("user", user);
    		properties.put("password", pw);
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
	// Get DB User
	//*****************************************************************************//
  public static String getUser () {

    String user   = null;

    try {
    BufferedReader bf = new BufferedReader(new FileReader("/java/adlumin/DBAuth.txt"));
     user   = bf.readLine().trim();    // User  is on Line 1
    }
    catch (Exception e) {
     System.out.println(e);
     user = null;
    }

    return user;

   }
	//*****************************************************************************//	
	// Get Password
	//*****************************************************************************//
  public static String getPw   () {

    String pw     = null;

    try {
    BufferedReader bf = new BufferedReader(new FileReader("/java/adlumin/DBAuth.txt"));
     bf.readLine();
     pw     = bf.readLine().trim();    // Password is on Line 2
    }
    catch (Exception e) {
     System.out.println(e);
     pw   = null;
    }

    return pw;

   }
    //*****************************************************************************// 
    // Get Install Date 
    //*****************************************************************************//
    public static String getInstallDate (Connection dbConn, String Description) { 
    	
    	String InstallDate = null;
    	
    	try {
			// Get Creation Date and populate Install Date
    		String queryString = "select * from adlumin.userinfo " +
    		 "where odobnm = " +
    			"'" +  Description + "'";
        
    		Statement s2 = dbConn.createStatement();
    		ResultSet rs2 = s2.executeQuery(queryString); 
        
            if (rs2.next()) {
            	InstallDate = "20" +
            			rs2.getString(14).substring(4,6) + "-" + 
            			rs2.getString(14).substring(0,2) + "-" + 
            			rs2.getString(14).substring(2,4);   
            }  
            else InstallDate = "*ERROR";
    	}
        catch (Exception e) {
            System.out.println(e);
            InstallDate = null;
        }  
      return InstallDate;
    }
    
    //*****************************************************************************//
    // Post data through HTTP
    //*****************************************************************************//
    public static void postData (String jsonInputString) {
    	
    	try  {
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

    	Connection dbConn          = null;
        String jsonInputString     = null;
    	String AccountType         = null;
        String Description         = null;
        boolean Disabled           = false;
        String Domain              = null;
        String FullName            = null;
        String InstallDate         = null;
        boolean Lockout            = false;
        boolean PasswordChangeable = false;
        boolean PasswordExpires    = false;
        boolean PasswordRequired   = false;
        String SID                 = null;
        String SIDType             = null;
        String Status              = null;
        String Passwordlastset     = null;
        String Lastlogin           = null;
        
        try {
        	
          dbConn = getConn();  // Get Database Connection 	
          Statement s = dbConn.createStatement();
          //sultSet rs = s.executeQuery("select * from qsys2.user_info where user_name = 'VEER'");
          ResultSet rs = s.executeQuery("select * from qsys2.user_info where user_name not like 'Q%'");
          while (rs.next()) {
            
        	Disabled           = false;  
            Lockout            = false;
            PasswordChangeable = false;
            PasswordExpires    = false;
            PasswordRequired   = false;
                        
            AccountType = rs.getString(11);
            Description = rs.getString(1);
            
            Status      = rs.getString(4);
            if (Status.equals("*DISABLED")) {
              Disabled = true;
              Lockout = true;
            }
            
            Domain = rs.getString(66);
            FullName = rs.getString(25); 
            
            InstallDate = getInstallDate(dbConn, Description);
            
            if (rs.getString(60).equals("YES"))
                PasswordChangeable = true;
            
            if (rs.getString(10).equals("YES"))
              PasswordExpires = true;	
            
            if (rs.getString(68).equals("NO"))
              PasswordRequired = true;
            
            SID             = "N/A";
            SIDType         = "User Account";
            Passwordlastset = rs.getString(5);
            Lastlogin       = rs.getString(2);
            
            jsonInputString = "{" +
              "\"AccountType\": \"" + AccountType + "\", " +
              "\"Caption\": \"N/A\", " +
              "\"Description\": \"" + Description + "\", " +
              "\"Disabled\": \"" + Disabled + "\", " +
              "\"Domain\": \"" + Domain + "\", " +
              "\"FullName\": \"-" + FullName + "\", " +
              "\"InstallDate\": \"" + InstallDate + "\", " +
              "\"LocalAccount\": \"true\", " +
              "\"Lockout\": \"" + Lockout + "\", " +
              "\"PasswordChangeable\": \"" + PasswordChangeable + "\", " +
              "\"PasswordExpires\": \"" + PasswordExpires + "\", " + 
              "\"PasswordRequired\": \"" + PasswordRequired + "\", " +
              "\"SID\": \"" + SID + "\", " +
              "\"SIDType\": \"" + SIDType + "\", " +
              "\"status\": \"" + Status + "\", " +
              "\"passwordlastset\": \"" + Passwordlastset + "\", " +
              "\"lastlogin\": \"" + Lastlogin + "\"}";
            
            postData(jsonInputString);
            
          }
          
          System.out.println("USER INFO");
        }
        catch (Exception e) {
        	System.out.println(e); 	
        }

    }

}


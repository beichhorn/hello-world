//********************************************************************************//
//                                                                                //
// Title       : shortBurst.java                                                  //
// Description : Create a "Short Burst" of server information in JSON format      //
//               to be sent to a API though HTTP.                                 //
// Author      : Bernard Eichhorn - Paragon Consulting Services                   //
// Date        : 7/13/2020                                                        //
//                                                                                //
//********************************************************************************//

import java.net.URL;
import java.net.HttpURLConnection;
import java.util.UUID;
import java.io.*;

class shortBurst {

    //*****************************************************************************//
    // Get Operating System                                                        //
    //*****************************************************************************//
    public static String getOs () {

         String line = null;
         String os   = null;

         try {
           BufferedReader bf = new BufferedReader(new FileReader("/java/adlumin/dspjob.txt"));
           line = bf.readLine();  // Operating System is on Line 1
           os = line.substring(9,15);
         }
         catch (Exception e) {
           System.out.println(e);
           os = "*ERROR";
         }

         return os;
    }

    //*****************************************************************************//
    // Get Adapter Address (MAC Address)
    //*****************************************************************************//
    public static String getAdptAdr() {

         String line = null;
         String checkString = null;
         String adapterAddress = null;

         try {
           BufferedReader bf2 = new BufferedReader(new FileReader("/java/adlumin/macadr.txt"));
           while ((line = bf2.readLine()) !=null) {
             checkString = line.substring(40,47);
             if (checkString.equals("ADPTADR")) {
               adapterAddress = line.substring(52,64);
               break;
             }  // End loop when "ADPTADR" (Adapter Address is found)
           }
         }
         catch (Exception e) {
           System.out.println(e);
           adapterAddress = "*ERROR";
         }

         return adapterAddress;
    }

    //*****************************************************************************//
    // Get IP Address
    //*****************************************************************************//

    public static String getIpAdr() {

         String line = null;
         String ip  = null;

         try {
           BufferedReader bf3 = new BufferedReader(new FileReader("/java/adlumin/ip.txt"));
           line = bf3.readLine();
           line = bf3.readLine();  // IP Address is on Line 2
           ip = line.substring(39,52);
         }
         catch (Exception e) {
           System.out.println(e);
           ip = "*ERROR";
         }

         return ip;
    }

    //*****************************************************************************//
    // Get HTTP URL Connection
    //*****************************************************************************//

    public static HttpURLConnection getConn() {

         URL url                = null;
         HttpURLConnection conn = null;

         try {
              url = new URL("https://demo.securityeco.com/ibm");
              conn  = (HttpURLConnection)url.openConnection();
              conn.setRequestMethod("POST");
              conn.setRequestProperty("Content-Type", "application/json; utf-8");
              conn.setRequestProperty("Accept", "application/json");
              conn.setDoOutput(true);

         }
         catch (Exception e) {
              System.out.println(e);
              conn = null;
         }

         return conn;

    }

    //*****************************************************************************//
    // Get POST data through HTTP
    //*****************************************************************************//

    public static void postData (String jsonInputString) {

        try {

              HttpURLConnection conn  = getConn();    // Get HTTP Connection

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

    }

    //*****************************************************************************//
    //                                                                             //
    // Main method                                                                 //
    //                                                                             //
    //*****************************************************************************//
    public static void main (String[] arguments) {

        try {

         UUID uuid = UUID.randomUUID();

         String OS               = getOs();      // Get Operating System
         String adapterAddress   = getAdptAdr(); // Get Adapter Address (MAC Address)
         String ip               = getIpAdr();   // Get IP Address

         String jsonInputString = "{" +
         "\"agentid\": \"" + uuid + "\", " +
         "\"computername\": \"" + arguments[1] + "\", " +
         "\"mac_address\": \"" + adapterAddress + "\", " +
         "\"os\": \"" + OS + "\", " +
         "\"architecture\": \"True\", " +
         "\"tzoffset\": \"-" + arguments[2] + "\", " +
         "\"ip\": \"" + ip + "\", " +
         "\"agentversion\": \"1.0.0.0\", " +
         "\"tennant_id\":" +
         "\"6ed3be-932c-40e6-9d80-37283527481c-fcf2ac21-28ec-4e0a-b140-9dd7\", " +
         "\"manufacturer\": \"International Business Machines Inc.\", " +
         "\"domain\": \"LOCAL\", " +
         "\"serialnumber\": \"" + arguments[0] + "\", " +
         "\"dnshostname\": \"" + arguments[1] + "\", " +
         "\"biosversion\": \"N/A\", " +
         "\"dccheck\": \"False\", " +
         "\"type\": \"IBM i\"}";

         postData(jsonInputString);

        }
        catch (Exception e) {
            System.out.println(e);
        }

    }

}


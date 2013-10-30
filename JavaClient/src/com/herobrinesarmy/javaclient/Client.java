
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * @author lukew4lker
 * 
 * Networking code: Used to communicate with the server.
 * Credit goes to Phoenix for the original code which I modified to be smaller
 * And more moduler.  
 * 
 */
public class Client {
    //Do CookieManager.setDefault(cookieManager); to activate.
    private CookieManager cookieManager = new CookieManager();
    //The channel of the ChatRoom
    private int channel = 0;
    //The ID of the last message in the ChatRoom
    private int lastMessageID = 0;
    //The format of encoding the ChatRoom uses for URLs
    private String enc = "UTF-8";

    private HttpURLConnection getData(URL addrs, HttpURLConnection conn) throws IOException {
        /* Generic method for getting data from the server. After this method is 
         * called, the connection is ready for an input stream.
         */
        try {
            conn = (HttpURLConnection) addrs.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
        } catch (IOException e) {
            return null;
        }
        return conn;
    }

    private HttpURLConnection postData(URL addrs, HttpURLConnection conn) throws IOException {
        /*Generic method for sending data to the server. After this method is 
         * called, the connection is ready for an output stream.
         */
        try {
            conn = (HttpURLConnection) addrs.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
        } catch (IOException e) {
            return null;
        }
        return conn;
    }

    public String checkAuth() throws IOException {
        /* Checks server to see if we are authed.
         * Returns "Yeah" if we are, "Nope" if we aren't.  
         */
        HttpURLConnection connAuth = null;
        URL addrs = new URL("http://herobrinesarmy.com/amiauth");
        HttpURLConnection conn = getData(addrs, connAuth);
        String result = "";
        Scanner reader = null;
        reader = new Scanner(conn.getInputStream());
        while (reader.hasNext()) {
            result += reader.nextLine();
        }
        return result;
    }

    public boolean authenticate(String username, char[] pwInput) throws IOException {
        /* Authenticate with the server.
         * Returns true if we are and false if not. 
         */
        String password = new String(pwInput);
        URL addrs = new URL("http://herobrinesarmy.com/auth.php");
        HttpURLConnection connLogin = null;
        HttpURLConnection conn = postData(addrs, connLogin);
        String query = "user=" + URLEncoder.encode(username, enc) + "&pass=" + URLEncoder.encode(password, enc);
        DataOutputStream outs = new DataOutputStream(conn.getOutputStream());
        outs.writeBytes(query);
        outs.close();
        //Receive a response from the server to enable auth.
        @SuppressWarnings("unused")
        int response = conn.getResponseCode();
        return checkAuth().equals("Yeah.");
    }

    public JSONObject poll() throws MalformedURLException, JSONException, IOException {
        /**
         * Used to poll the server to check for new messages other 
         * data including the following;
         * [*] IMID
         * [*] Users
         * [*] Timestamps
         * Returns a JSON object ready to be parsed.  
         */
        URL addrs = new URL("http://herobrinesarmy.com/update_chat2?&c=" + channel + "&l=" + lastMessageID);
        HttpURLConnection connPoll = null;
        HttpURLConnection conn = getData(addrs, connPoll);
        Scanner reader = new Scanner(conn.getInputStream());
        String result = "";
        while (reader.hasNext()) {
            result += reader.nextLine();
        }
        return new JSONObject(result.substring(1, result.length()));
    }
    public int postMessage(String message) throws MalformedURLException, UnsupportedEncodingException, IOException {
        /**
         * Used to post a message to the server. In order to successfully post a
         * message, you must have the correct IMID which you get inside the json
         * object returned from poll();. This code returns the 
         */
        URL addrs = new URL("http://herobrinesarmy.com/post_chat?o=1&c=" + channel + "&m=" + URLEncoder.encode(message, enc));
        HttpURLConnection connPostMesg = null;
        HttpURLConnection conn = getData(addrs, connPostMesg);
        //Again to make sure that the server completes the request
        @SuppressWarnings("unused")
        int response = conn.getResponseCode();
        return response; 
    }
}
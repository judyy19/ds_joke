package ds.joke;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/joke")
public class JokeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Retrieve parameters sent from the Android App
        String jokeCategory = request.getParameter("category");
        String jokeType = request.getParameter("type");

        // Set the response content type
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject result = new JSONObject();
        JSONObject error = new JSONObject();

        // ---- Check category ----
        if (jokeCategory == null || jokeCategory.isEmpty()) {
            jokeCategory = "Any";
        } else {
            // valid category
            String[] allowedCategories = {"Programming", "Misc", "Dark", "Pun", "Spooky", "Christmas", "Any"};
            boolean valid = false;
            for (String allowed : allowedCategories) {
                if (allowed.equalsIgnoreCase(jokeCategory.trim())) {
                    valid = true;
                    break;
                }
            }

            if (!valid) {
                error.put("error", "Invalid joke category. Must be one of: Programming, Misc, Dark, Pun, Spooky, Christmas, Any.");
                out.print(error.toString());
                return;
            }
        }

        // ---- Check type ----
        if (jokeType == null || (!jokeType.equals("single") && !jokeType.equals("twopart"))) {
            error.put("error", "Invalid joke type. Must be 'single' or 'twopart'.");
            out.print(error.toString());
            return; // finish the HTTP request process, stop processing the following code
        }

        // Build the API URL
        String apiUrl = "https://v2.jokeapi.dev/joke/" + jokeCategory + "?type=" + jokeType;

        try{

            // Send an HTTP GET request to the JokeAPI
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Read the returned JSON result
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                jsonBuilder.append(line);
            }
            in.close();

            JSONObject apiResponse = new JSONObject(jsonBuilder.toString());

            // Handle the joke result returned by the API
            if (apiResponse.getBoolean("error")) {
                result.put("error", "Oops! The API returned an error.");
            } else {
                String type = apiResponse.getString("type");
                result.put("type", type);

                if (type.equals("single")) {
                    String joke = apiResponse.getString("joke");
                    result.put("joke", joke);
                } else if (type.equals("twopart")) {
                    String setup = apiResponse.getString("setup");
                    String delivery = apiResponse.getString("delivery");
                    result.put("setup", setup);
                    result.put("delivery", delivery);
                }
            }
        } catch (Exception e) {
            result.put("error", "Failed to fetch joke from JokeAPI.");
            result.put("exception", e.getMessage());
        }

        // Output the result back to the Android App
        out.print(result.toString());
        out.flush();
    }

    @Override
    public void destroy() {
        // No special cleanup required
    }
}

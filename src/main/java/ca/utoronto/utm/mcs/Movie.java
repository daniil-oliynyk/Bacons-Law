package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class Movie implements HttpHandler {

    private Neo4jDB db;

    public Movie(Neo4jDB db) {
        this.db = db;
    }

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            } else if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleGet(HttpExchange r) throws IOException, JSONException {

        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String movieId;
        movieId = "";

        try{

            if(deserialized.has("movieId")) {
                movieId = deserialized.getString("movieId");
            }else{
                r.sendResponseHeaders(400,-1);
                return;
            }

            JSONObject response = db.getMovie(movieId);
            if(response == null){
                r.sendResponseHeaders(404,-1);
                return;
            }
            if(response.has("status")){
                r.sendResponseHeaders(404,-1);
                return;
            }

            r.sendResponseHeaders(200,response.toString().replace("\\\"","").length());
            OutputStream os = r.getResponseBody();
            os.write(response.toString().replace("\\\"","").getBytes());
            os.close();
            return;

        }catch (Exception e){
            e.printStackTrace();
            r.sendResponseHeaders(500,-1);
            return;

        }

    }

    public void handlePut(HttpExchange r) throws IOException, JSONException{
        /* TODO: Implement this.
           Hint: This is very very similar to the get just make sure to save
                 your result in memory instead of returning a value.*/
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String name;
        String movieId;


//      TODO: wrap in try catch
        name = "";
        movieId = "";

        try{
            if (deserialized.has("name")) {
                name = deserialized.getString("name");
            }else{
                r.sendResponseHeaders(400, -1);
                return;
            }
            if (deserialized.has("movieId")) {
                movieId = deserialized.getString("movieId");
            }else{
                r.sendResponseHeaders(400, -1);
                return;
            }
            int retVal = db.addMovie(name, movieId);
            if(retVal == 1){
                r.sendResponseHeaders(500,-1);
                return;
            }else if(retVal == 2){
                r.sendResponseHeaders(400,-1);
                return;
            }
            r.sendResponseHeaders(200, -1);
            return;
        } catch(Exception e){
            e.printStackTrace();
            r.sendResponseHeaders(500,-1);
            return;
        }

    }
}
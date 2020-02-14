package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class Actor implements HttpHandler {

    private Neo4jDB db;

    public Actor(Neo4jDB db) {
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
//        TODO:
        String body = Utils.convert((r.getRequestBody()));
        JSONObject deserialized = new JSONObject(body);

        String actorId;
        actorId = "";

        try{
            if(deserialized.has("actorId")) {
                actorId = deserialized.getString("actorId");
            }else{
                r.sendResponseHeaders(400,-1);
                return;
            }
            JSONObject response = db.getActor(actorId);
            if(response == null){
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
        String actorId;


//      TODO: wrap in try catch
        name = "";
        actorId = "";

        try{
            if (deserialized.has("name")) {
                name = deserialized.getString("name");
            }else{
                r.sendResponseHeaders(400,-1);
                return;
            }
            if (deserialized.has("actorId")){
                actorId = deserialized.getString("actorId");
            }else{
                r.sendResponseHeaders(400,-1);
                return;
            }

            int retVal = db.addActor(name, actorId);
            if(retVal == 1){
                r.sendResponseHeaders(500, -1);
                return;
            }else if(retVal == 2){
                r.sendResponseHeaders(400, -1);
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

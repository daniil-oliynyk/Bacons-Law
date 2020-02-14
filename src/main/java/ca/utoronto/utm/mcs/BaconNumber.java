package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class BaconNumber implements HttpHandler {

    private Neo4jDB db;

    public BaconNumber(Neo4jDB db) {
        this.db = db;
    }

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleGet(HttpExchange r) throws IOException, JSONException {

        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String actorId = "";

        try {
            if (deserialized.has("actorId")) {
                actorId = deserialized.getString("actorId");
            }else{
                r.sendResponseHeaders(400,-1);
                return;
            }
            JSONObject response = db.computeBaconNumber(actorId);

            if(response.has("noPath")){
                r.sendResponseHeaders(404,-1);
                return;
            }
            if(response.has("noActor")){
                r.sendResponseHeaders(400,-1);
                return;
            }

            if (response == null) {
                r.sendResponseHeaders(404, -1);
                return;
            }
            r.sendResponseHeaders(200, response.toString().replace("\\\"", "").length());
            OutputStream os = r.getResponseBody();
            os.write(response.toString().replace("\\\"", "").getBytes());
            os.close();
            return;

        } catch (Exception e) {
            e.printStackTrace();
            r.sendResponseHeaders(500, -1);
            return;
        }
    }
}
package ca.utoronto.utm.mcs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import org.neo4j.driver.v1.types.Path;

import java.util.List;

import static org.neo4j.driver.v1.Values.parameters;


public class Neo4jDB implements AutoCloseable
{
    private final Driver DB;

    public Neo4jDB(String uri, String user, String password )
    {
        DB = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    @Override
    public void close() throws Exception
    {
        DB.close();
    }

    //    TODO: create all the methods here
    public int addActor(String name, String actorId) {
        try(Session session = DB.session()){
            StatementResult result = session.run("match (n:actor) where n.id = $x return n", parameters("x", actorId));
            if(result.list().size() != 0){
                return 2;
            }
            session.run("MERGE (:actor {Name: $x, id: $y})", parameters("x", name, "y", actorId));
            return 0;
        }catch (Exception e){
            e.printStackTrace();
            return 1;
        }
    }
    public int addMovie(String name, String movieId){
        try(Session session = DB.session()){
            StatementResult result = session.run("match (m:movie) where m.id = $x return m", parameters("x", movieId));
                 if(result.list().size() != 0){
                     return 2;
                 }
            session.run("MERGE (:movie {Name: $x, id: $y})", parameters("x", name, "y", movieId));
            return 0;
        }catch(Exception e){
            e.printStackTrace();
            return 1;
        }
    }

    public int addRelationship(String actorId, String movieId){
        try(Session session = DB.session()){
            StatementResult result = session.run("match(a:actor),(m:movie) where a.id = $x and m.id = $y return a,m", parameters("x", actorId, "y", movieId));

            if(result.list().size() == 0){
                return 2;
            }
            session.run("match (a:actor),(b:movie) where a.id = $x and b.id = $y create (a)-[r:ACTED_IN]->(b)", parameters("x", actorId, "y", movieId));
            return 0;
        }catch (Exception e){
            e.printStackTrace();
            return 1;
        }
    }

    public JSONObject getActor(String actorId){

        try(Session session = DB.session()){

            JSONObject JSONresponse = new JSONObject();

            StatementResult result2 = session.run("match (a:actor) where a.id = $x" +
                    " return a.Name as name", parameters("x", actorId));
            Record actor = result2.single();

            StatementResult result = session.run(" match (a:actor)-[:ACTED_IN]->(m:movie) " +
                    "where a.id = $x " +
                    "return m.id as movieId", parameters("x", actorId));


            List<Record> movieRecords = result.list();
            String[] movies = new String[movieRecords.size()];
            for (int i=0; i<movieRecords.size(); i++) {
                Record movieRecord = movieRecords.get(i);
                movies[i] = movieRecord.get("movieId").asString();
            }
            JSONresponse.put("name", actor.get("name"));
            JSONresponse.put("actorId", actorId);
            JSONresponse.put("movies", new JSONArray(movies));

            return JSONresponse;

        } catch(NoSuchRecordException | JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject getMovie(String movieId){

        try(Session session = DB.session()){

            JSONObject JSONresponse = new JSONObject();

            StatementResult result = session.run("match (m:movie) where m.id = $x" +
                    " return m.Name as name", parameters("x", movieId));
            Record movie = result.single();
            if(movie.size() == 0){
                JSONresponse.put("status", "fail");
                return JSONresponse;
            }

            StatementResult result2 = session.run("match (a:actor)-[:ACTED_IN]->(m:movie) " +
                    "where m.id = $x " +
                    "return a.id as actorId", parameters("x", movieId));

            List<Record> actorRecords = result2.list();
            String[] actors = new String[actorRecords.size()];
            for (int i=0; i<actorRecords.size(); i++) {
                Record record = actorRecords.get(i);
                actors[i] = record.get("actorId").asString();
            }

            JSONresponse.put("name", movie.get("name"));
            JSONresponse.put("movieId", movieId);
            JSONresponse.put("actors", new JSONArray(actors));

            return JSONresponse;

        } catch (NoSuchRecordException | JSONException e) { e.printStackTrace(); return null; }
    }

    public JSONObject hasRelationship(String actorId, String movieId) {

        try (Session session = DB.session()) {
            JSONObject JSONresponse = new JSONObject();

            StatementResult actorExist = session.run("match (a:actor {id: $x}) return a.id",parameters("x", actorId));
            if(actorExist.list().size() == 0){
                JSONresponse.put("noActor", true);
                return JSONresponse;
            }
            StatementResult movieExist = session.run("match (m:movie {id: $x}) return m.id", parameters("x", movieId));
            if(movieExist.list().size() == 0){
                JSONresponse.put("noMovie", true);
                return JSONresponse;
            }
            StatementResult result = session.run("match (a:actor { id: $x })-[r:ACTED_IN]->(m:movie { id: $y }) " +
                    "return type(r) as true", parameters("x", actorId, "y", movieId));


            try{
                Record record = result.single();
            }catch(NoSuchRecordException e){
                JSONresponse.put("actorId", actorId);
                JSONresponse.put("movieId", movieId);
                JSONresponse.put("hasRelationship", false);
                return JSONresponse;

            }

            JSONresponse.put("actorId", actorId);
            JSONresponse.put("movieId", movieId);
            JSONresponse.put("hasRelationship", true);

            return JSONresponse;

        } catch (JSONException e) { return null; }
    }

    public JSONObject computeBaconNumber(String actorId){
        JSONObject JSONresponse = new JSONObject();
        String KevinBacon = "nm0000102";

        try(Session session = DB.session()){
            if(actorId.equals("nm0000102")){
                JSONresponse.put("baconNumber", "0");
                return JSONresponse;
            }
            StatementResult actorExist = session.run("match(a:actor) where a.id = $x return a.id", parameters("x", actorId));
            if(actorExist.list().size() == 0){
                JSONresponse.put("noActor", "true");
                return  JSONresponse;
            }

            StatementResult result = session.run("MATCH \n" +
                    "  path = shortestPath(\n" +
                    "    (a1:actor)-[*]-(a2:actor)\n" +
                    "  )\n" +
                    "WHERE \n" +
                    "  a1.id = $kb and a2.id = $x " +
                    "RETURN\n" +
                    "  nodes(path) as nodes", parameters("kb", KevinBacon, "x", actorId));

            try {
                Record nodeRecords = result.list().get(0);
                int BaconNumber = (nodeRecords.get(0).size() - 1) / 2;

                String baconNumber = String.valueOf(BaconNumber);
                JSONresponse.put("baconNumber", baconNumber);
                return JSONresponse;
            }catch (IndexOutOfBoundsException e){
                e.printStackTrace();
                JSONresponse.put("noPath", "true");
                return  JSONresponse;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject computeBaconPath(String actorId){
        String KevinBacon = " nm0000102";

        try(Session session = DB.session()){

            if(actorId.equals("nm0000102")){
                JSONObject response = new JSONObject();
                response.put("baconNumber", 0);
                JSONObject[] kevinBaconPath = new JSONObject[1];
                JSONObject temp = new JSONObject();
                temp.put("actorId", "nm0000102");
                StatementResult result = session.run("match (a:actor {id: \"nm0000102\"})-[*]->(m:movie) return m.id");
                if(!result.hasNext()){
                    temp.put("movieId", "");
                    kevinBaconPath[0] = temp;
                    response.put("baconPath", kevinBaconPath);
                    return response;
                }
                Record r = result.next();

                temp.put("movieId",r.get("movieId"));
                kevinBaconPath[0] = temp;
                response.put("baconPath", kevinBaconPath);
                return response;

            }
            StatementResult result = session.run("MATCH p=shortestPath(\n" +
                    "  (bacon:actor {Name:\"Kevin Bacon\"})-[*]-(a:actor {id:$x})\n" +
                    ")\n" +
                    "RETURN p", parameters( "x", actorId));


            JSONObject JSONresponse = new JSONObject();

            try {
                Record nodeRecords = result.list().get(0);
                Path path = nodeRecords.get(0).asPath();
                if (path.length() == 0) {
                    JSONresponse.put("noPath", "true");
                    return JSONresponse;
                }
                JSONObject[] baconPath = new JSONObject[path.length()];

                String actor;
                String movie;
                int i = 0;
                for (Path.Segment segment : path) {

                    JSONObject temp = new JSONObject();
                    if (segment.start().hasLabel("actor")) {
                        actor = segment.start().get("id").asString();
                        movie = segment.end().get("id").asString();
                        temp.put("actorId", actor);
                        temp.put("movieId", movie);
                        baconPath[i] = temp;
                    } else if (segment.start().hasLabel("movie")) {
                        movie = segment.start().get("id").asString();
                        actor = segment.end().get("id").asString();
                        temp.put("actorId", actor);
                        temp.put("movieId", movie);
                        baconPath[i] = temp;
                    }

                    i++;
                }

                JSONresponse.put("baconPath", baconPath);
                return JSONresponse;
            }catch (IndexOutOfBoundsException e){
                e.printStackTrace();
                JSONresponse.put("noPath", "true");
                return JSONresponse;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}

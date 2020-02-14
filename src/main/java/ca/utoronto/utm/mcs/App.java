package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class App 
{
    static int PORT = 8080;

    public static void main(String[] args) throws IOException
    {

        Neo4jDB DB = new Neo4jDB("bolt://localhost:7687", "neo4j", "1234");

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);

        server.createContext("/api/v1/addActor", new Actor(DB));
        server.createContext("/api/v1/addMovie", new Movie(DB));
        server.createContext("/api/v1/addRelationship", new Relationship(DB));
        server.createContext("/api/v1/getActor", new Actor(DB));
        server.createContext("/api/v1/getMovie", new Movie(DB));
        server.createContext("/api/v1/hasRelationship", new Relationship(DB));
        server.createContext("/api/v1/computeBaconNumber", new BaconNumber(DB));
        server.createContext("/api/v1/computeBaconPath", new BaconPath(DB));

    }

}

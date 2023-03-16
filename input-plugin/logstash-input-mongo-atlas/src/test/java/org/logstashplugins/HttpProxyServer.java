package org.logstashplugins;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class HttpProxyServer {
    private static final int BUFFER_SIZE = 8192;
    public static final String PROXY_GOT_REQUEST = "PROXY_GOT_REQUEST";
    public static final String MONGO_ATLAS_URL = "cloud.mongodb.com:443";

    private int port = 8080;
    Consumer<String> consumer;

    public HttpProxyServer(int port, Consumer<String> consumer) {
        this.port = port;
        this.consumer = consumer;
        try(ServerSocket serverSocket = new ServerSocket(port)){

            System.out.println("HttpProxy for MongoAtlas input plugin test is listening on port " + port + " ...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort());
                handleClientRequest(clientSocket);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 1234; // The port to listen on
        System.out.println("HttpProxy for MongoAtlas input plugin test is going to listen on port " + port + " ...");
        Consumer<String> consumer = new Consumer<String>() {
            @Override
            public void accept(String msg) {
                System.out.println("Got results");
                System.out.println("Message is "+msg);
            }
        };
        HttpProxyServer server = new HttpProxyServer(port, consumer);
    }

    private void handleClientRequest(Socket clientSocket) throws IOException {
        InputStream clientIn = clientSocket.getInputStream();

        // Read the client's request
        byte[] requestBuffer = new byte[BUFFER_SIZE];
        int bytesRead = clientIn.read(requestBuffer);
        String request = new String(requestBuffer, 0, bytesRead);
        System.out.println("Received request from client:\n" + request);

        // Extract the requested URL
        String[] requestLines = request.split("\r\n");
        String requestedUrl = null;
        for (String line : requestLines) {
            if (line.startsWith("GET ") || line.startsWith("POST ") || line.startsWith("CONNECT ")) {
                requestedUrl = line.split("\\s")[1];
                break;
            }
        }
        System.out.println("Requested URL: " + requestedUrl);
        if (MONGO_ATLAS_URL.equalsIgnoreCase(requestedUrl)){
            consumer.accept(PROXY_GOT_REQUEST);
        }

//
//        // Forward the request to the server
//        OutputStream clientOut = clientSocket.getOutputStream();
//        Socket serverSocket = new Socket(requestedUrl.split(":")[0], 80);
//        OutputStream serverOut = serverSocket.getOutputStream();
//        serverOut.write(requestBuffer, 0, bytesRead);
//
//        // Read the server's response
//        InputStream serverIn = serverSocket.getInputStream();
//        byte[] responseBuffer = new byte[BUFFER_SIZE];
//        int numBytesRead = serverIn.read(responseBuffer);
//        while (numBytesRead != -1) {
//            // Forward the response back to the client
//            clientOut.write(responseBuffer, 0, numBytesRead);
//            numBytesRead = serverIn.read(responseBuffer);
//        }

        // Close the sockets
        clientSocket.close();
//        serverSocket.close();
        System.out.println("Closed connection");
    }
}
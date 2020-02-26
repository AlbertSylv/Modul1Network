package webserver.dat.sem2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 The purpose of ClientMain is to...

 @author kasper
 */
public class ClientMain {

    public static void main( String[] args ) throws Exception {
//        for (int i = 0; i < 100; i++) {
//        picoClient01();   
//        }
        
        
    }

    private static void picoClient01() throws Exception {
        String hostName = "localhost";
        int portNumber = 8080;
        String message = "GET /albert.html HTTP/1.0\r\n\r\n";
        ExecutorService workingJack = Executors.newFixedThreadPool(4); 
        
        workingJack.submit(() -> 
        {
            try {
                System.out.println( "Connect to: " + hostName + ":" + portNumber );
                Socket mySocket = new Socket( hostName, portNumber );
                sendMessagePrintResult( mySocket, message );
            } catch ( Exception ex ) {
                System.out.println( "Uuups: " + ex.getLocalizedMessage() );
            }
        });
    }

    private static void picoClient02() throws Exception {
        String hostName = "whois.iana.org";
        int portNumber = 43;
        String message = "dr.dk\r\n";
        try {
            System.out.println( "Connect to: " + hostName + ":" + portNumber );
            Socket mySocket = new Socket( hostName, portNumber );
            sendMessagePrintResult( mySocket, message );
            // Send message to server
            // print response
        } catch ( Exception ex ) {
            System.out.println( "Uuups: " + ex.getLocalizedMessage() );
        }
    }
    
    private static void picoClient03() throws Exception {
        String hostName = "itu.dk";
        int portNumber = 7;
        String message = "Hello 2. semester\r\n";
        try {
            System.out.println( "Connect to: " + hostName + ":" + portNumber );
            Socket mySocket = new Socket( hostName, portNumber );
            sendMessagePrintResult( mySocket, message );
            // Send message to server
            // print response
        } catch ( Exception ex ) {
            System.out.println( "Uuups: " + ex.getLocalizedMessage() );
        }
    }

    private static void sendMessagePrintResult( Socket mySocket, String message ) throws IOException {
        PrintWriter out = new PrintWriter( mySocket.getOutputStream(), true );
        BufferedReader in = new BufferedReader(
                new InputStreamReader( mySocket.getInputStream() ) );
        // Send message to server
        out.println( message );
        // print response
        System.out.println( "------" );
        String line;
        while ( ( line = in.readLine() ) != null ) {
            System.out.println( line );
        }
    }

}

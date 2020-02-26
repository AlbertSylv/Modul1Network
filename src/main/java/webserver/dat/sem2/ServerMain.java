package webserver.dat.sem2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 The purpose of ServerMain is to...

 @author kasper
 */
public class ServerMain{

    public static void main( String[] args ) throws Exception {
        
        picoServer06();

        
    }

    /*
    Plain server that just answers what date it is.
    It ignores all path and parameters and really just tell you what date it is
     */
    private static void picoServer01() throws Exception {
        final ServerSocket server = new ServerSocket( 65080 );
        System.out.println( "Listening for connection on port 65080 ...." );
        while ( true ) { // spin forever } }
            try ( Socket socket = server.accept() ) {
                Date today = new Date();
                String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + today;
                socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
            }

        }
    }

    /*
    Same server, but this one writes to system.out to show what info we get
    from the browser/client when we it sends a request to the server.
    It still just tell the browser what time it is.
     */
    private static void picoServer02()  {
        ServerSocket server = null;
        
        int option = 1;
        int port= 8080;
        while (option != 0) 
        {

            try {
                server = new ServerSocket( port );
                option = 0;
            } 
            catch (IOException ioexception) 
            {
                port++;
            }
        }
            
        
        System.out.println( "Listening for connection on port: " + port );
        while ( true ) { // keep listening (as is normal for a server)
            try ( Socket socket = server.accept() ) {
                System.out.println( "-----------------" );
                System.out.println( "Client: " + socket.getInetAddress().getHostName() );
                System.out.println( "-----------------" );
                BufferedReader br = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
                String line;
                while ( !( ( line = br.readLine() ).isEmpty() ) ) {
                    System.out.println( line );
                }
                System.out.println( ">>>>>>>>>>>>>>>" );
                Date today = new Date();
                String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + today;
                socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
                System.out.println( "<<<<<<<<<<<<<<<<<" );
                
            }
            catch(BindException e)
            {
                System.out.println(e.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        
            
            

        }
    }

    /*
    This server uses a HttpRequest object to *parse* the text-request into a
    java object we can then use to examine the different aspect of the request
    using the getters of the HttpRequest object.
    It still just returns the date to the client.
     */
    private static void picoServer03() throws Exception {
        final ServerSocket server = new ServerSocket( 8080 );
        System.out.println( "Listening for connection on port 8080 ...." );
        int count = 0;
        while ( true ) { // keep listening (as is normal for a server)
            try ( Socket socket = server.accept() ) {
                System.out.println( "---- Request: " + count++ + " --------" );
                HttpRequest req = MakeHTTPReq(socket);

                System.out.println( "Method: " + req.getMethod() );
                System.out.println( "Protocol: " + req.getProtocol() );
                System.out.println( "Path: " + req.getPath() );
                System.out.println( "Parameters:" );
                for ( Entry e : req.getParameters().entrySet() ) {
                    System.out.println( "    " + e.getKey() + ": " + e.getValue() );
                }
                System.out.println( "Headers:" );
                for ( Entry e : req.getHeaders().entrySet() ) {
                    System.out.println( "    " + e.getKey() + ": " + e.getValue() );
                }


                System.out.println( "---- BODY ----" );
                System.out.println( req.getBody() );
                System.out.println( "==============" );
                Date today = new Date();
                String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + today;
                socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
            }
        }
    }

    /*
    This server uses the path of the HttpRequest object to return a html file to
    the browser. See the notes on Java ressources.
     */
    private static void picoServer04() throws Exception {
        final ServerSocket server = new ServerSocket( 8080 );
        System.out.println( "Listening for connection on port 8080 ...." );
        String root = "pages";
        while ( true ) { // keep listening (as is normal for a server)
            try ( Socket socket = server.accept() ) {
                MakeResponse(socket, root);
            }
        }
//        System.out.println( getFile("adding.html") );
    }

    /*
    This server has exception handling - so if something goes wrong we do not
    have to start it again. (this is a yellow/red thing for now)
     */
    private static void picoServer05() throws Exception {
        final ServerSocket server = new ServerSocket( 8080 );
        System.out.println( "Listening for connection on port 8080 ...." );
        String root = "pages";
        
         ExecutorService workingJack = Executors.newFixedThreadPool(4);
        
        while ( true ) 
        { // keep listening (as is normal for a server)
            Socket socket = server.accept();
                workingJack.submit(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                        try 
                        {
                            System.out.println(Thread.currentThread().getId());
                            MakeResponse(socket, root);
                        } catch (Exception ex) {
                            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
                        }finally {
                        if ( socket != null ) {
                            try {
                                socket.close();
                            } catch (IOException ex) {
                                Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        }
                }
        });
    
        
//        System.out.println( getFile("adding.html") );
    }
    }

    private static void MakeResponse(Socket socket, String root) throws IOException, Exception {
        System.out.println( "-----------------" );
        HttpRequest req = MakeHTTPReq(socket);
        String path = root + req.getPath();
        String html = getResourceFileContents( path );
        String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + html;
        socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
        System.out.println( "<<<<<<<<<<<<<<<<<" );
    }

    /*
    This server requires static files to be named ".html" or ".txt". Other path
    names is assumed to be a name of a service.
     */
    private static void picoServer06() throws Exception {
        final ServerSocket server = new ServerSocket( 8080 );
        System.out.println( "Listening for connection on port 8080 ...." );
        String root = "pages";
        int count = 0;
        
        ExecutorService workingJack = Executors.newFixedThreadPool(4);  
        while ( true ) { // keep listening (as is normal for a server)
            Socket socket = server.accept();

            workingJack.submit(() -> 
            {
                HttpRequest req;
                String path;
                try {
                    req = MakeHTTPReq(socket);
                    path =  req.getPath();                
                    if ( path.endsWith( ".html" ) || path.endsWith( ".txt" ) )
                    {
                        try 
                        {
                        String pathfile = root + req.getPath();
                        MakeResponse2(count, socket, pathfile);
                        } 
                        catch ( Exception ex ) 
                        {
                        String httpResponse = "HTTP/1.1 500 Internal error\r\n\r\n"
                                + "UUUUPS: " + ex.getLocalizedMessage();
                            try 
                            {
                            socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
                            } catch (IOException ex1) 
                            {
                            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                    }
                    else
                    {
                        try 
                        {
                        MakeResponse3(socket, req, path);
                        } 
                        catch (Exception ex) 
                        {
                        Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex); 
                        }
                    } 
                } catch (IOException ex) {
                    Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
                }
                
          
            });
        
        

    }
    }

    private static HttpRequest MakeHTTPReq(Socket socket) throws IOException {
        HttpRequest req = new HttpRequest( socket.getInputStream() );
        return req;
    }
    
                

    private static void MakeResponse2(int count, Socket socket, String path) throws Exception, IOException {

        
        
        System.out.println( "---- reqno: " + count + " ----" );
        UUID uuid = UUID.randomUUID();
        
            System.out.println("In thread: " + Thread.currentThread().getId() + ", Task ID: " + uuid.toString() + " started");
            String html = getResourceFileContents( path );
            String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + html;
            socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
            if ( socket != null ) 
            {
                try 
                {
                    
                    socket.close();
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        
            System.out.println("In thread: " + Thread.currentThread().getId() + ", Task ID: " + uuid.toString() + " ended");
        
    }

    private static void MakeResponse3(Socket socket, HttpRequest req, String path) throws Exception, IOException {
        String res = "";
        UUID uuid = UUID.randomUUID();
        System.out.println("In thread: " + Thread.currentThread().getId() + ", Task ID: " + uuid.toString() + " started");
        
            switch ( path ) {
                case "/addournumbers":
                    res = addOurNumbers( req );
                    break;
                case "/mulournumbers":
                    res = mulOurNumbers( req );
                    break;
                default:
                    res = "Unknown path: " + path;
            }
            String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + res;
            socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
            
            if ( socket != null ) 
            {
                try 
                {
                    socket.close();
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("In thread: " + Thread.currentThread().getId() + ", Task ID: " + uuid.toString() + " ended");
    }
    

    /*
    It is not part of the curriculum (pensum) to understand this method.
    You are more than welcome to bang your head on it though.
    */
    private static String getResourceFileContents( String fileName ) throws Exception {
        //Get file from resources folder
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource( fileName );
        File file = new File( url.getFile() );
        String content = new String( Files.readAllBytes( file.toPath() ) );
        return content;

    }
    
    private static String generateHTML(String file, String a, String b, String c){
        String res = file;
        res = res.replace( "$0", a);
        res = res.replace( "$1", b);
        res = res.replace( "$2", c );
        return res;
    }
    

    
    private static String generateHTMLvarargs(String file, String ... args){
        String res = file;

            for (int i = 0; i < args.length; i++) 
            {
                res = res.replace( "$" + Integer.toString(i), args[i]);
            }

        return res;
    }
    
     private static String mulOurNumbers( HttpRequest req ) throws Exception {
        String tmp = getResourceFileContents( "pages/result.tmpl" );
        String first = req.getParameter( "firstnumber" );
        String second = req.getParameter( "secondnumber" );
        int fi = Integer.parseInt( first );
        int si = Integer.parseInt( second );
        int ti = fi*si;
        
        String res = generateHTML(tmp, first, second, String.valueOf(ti));

        return res;
    }
    
    private static String addOurNumbers( HttpRequest req ) throws Exception {
        String tmp = getResourceFileContents( "pages/result.tmpl" );
        String first = req.getParameter( "firstnumber" );
        String second = req.getParameter( "secondnumber" );
        int fi = Integer.parseInt( first );
        int si = Integer.parseInt( second );
        int ti = fi+si;
        
        String res = generateHTML(tmp, first, second,String.valueOf(ti));

        return res;
    }
    
//    private static String addOurNumbers( HttpRequest req ) throws Exception {
//        
//        String tmp = getResourceFileContents( "pages/result.tmpl" );
//        String first = req.getParameter( "firstnumber" );
//        String second = req.getParameter( "secondnumber" );
//        Map<String, String> map = new HashMap<>();
//        int antpar = req.getParameters().size();
//        String[] array = new String[antpar + 1];
//        map = req.getParameters();
//        
//        int i = 0;
//        int sum=0;
//        for (String x: map.values()) {
//            array[i]=x;
//            sum += Integer.parseInt(x);
//            i++;
//        }
//        array[i]=String.valueOf(sum);
//        String res = generateHTMLvarargs(tmp, "first", "second","third");
//        String res = generateHTMLvarargs(tmp, array);
//        int fi = Integer.parseInt( first );
//        int si = Integer.parseInt( second );
//        String res = RES;
//        res = res.replace( "$0", first);
//        res = res.replace( "$1", second);
//        res = res.replace( "$2", String.valueOf( fi+si ) );
//        return res;
//    }
    
//    private static String mulOurNumbers( HttpRequest req ) throws Exception {
//        
//        String tmp = getResourceFileContents( "pages/result.tmpl" );
//        String first = req.getParameter( "firstnumber" );
//        String second = req.getParameter( "secondnumber" );
//        Map<String, String> map = new HashMap<>();
//        int antpar = req.getParameters().size();
//        String[] array = new String[antpar + 1];
//        map = req.getParameters();
//        
//        int i = 0;
//        int sum = 0;
//        for (String x: map.values()) {
//            array[i]=x;
//           
//            i++;
//        }
//        int b = 1;
//        for (int j = 0; j < array.length; j++) {
//            b = b * Integer.parseInt(array[j]);
//        }
//
//        array[i] = String.valueOf(sum);
//        //String res = generateHTMLvarargs(tmp, "first", "second","third");
//        String res = generateHTMLvarargs(tmp, array);
//        int fi = Integer.parseInt( first );
//        int si = Integer.parseInt( second );
//        String res = RES;
//        res = res.replace( "$0", first);
//        res = res.replace( "$1", second);
//        res = res.replace( "$2", String.valueOf( fi+si ) );
//        return res;
//    }

    private static String RES = "<!DOCTYPE html>\n"
            + "<html lang=\"da\">\n"
            + "    <head>\n"
            + "        <title>Adding form</title>\n"
            + "        <meta charset=\"UTF-8\">\n"
            + "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
            + "    </head>\n"
            + "    <body>\n"
            + "        <h1>Super: Resultatet af $0 + $1 blev: $2</h1>\n"
            + "        <a href=\"adding.html\">Læg to andre tal sammen</a>\n"
            + "    </body>\n"
            + "</html>\n";

}
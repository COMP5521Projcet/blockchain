package client3777;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import client3777.AddressBook;
import client3777.Message;
import client3777.ServerThread;

public class main {
	public static void main(String[] args){
		AddressBook myBook = new AddressBook("127.0.0.1:3777","127.0.0.1:3778");
		Message.sendIP(myBook.neighbours.get(0),myBook.myIP);
		Message.askNeighbours(myBook.neighbours.get(0),myBook.myIP);
		String block = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		//Message.sendBlock(myBook.neighbours.get(0),myBook.myIP, block);
		Message.broadcastBlock(myBook.neighbours, myBook.myIP, block);
		//Message.sendNeighbours(myBook.neighbours.get(0), myBook.myIP, myBook.neighbours);
		 Socket s=null;
		    ServerSocket ss2=null;
		    System.out.println("Server Listening......");
		    try{
		        ss2 = new ServerSocket(myBook.myIP.getPort()); // can also use static final PORT_NUM , when defined

		    }
		    catch(IOException e){
		    e.printStackTrace();
		    System.out.println("Server error");

		    }

		    while(true){
		        try{
		            s= ss2.accept();
		            System.out.println("connection Established");
		            ServerThread st=new ServerThread(s,myBook);
		            st.start();

		        }

		    catch(Exception e){
		        e.printStackTrace();
		        System.out.println("Connection Error");

		    }
		    }
	}
	
}
class ServerThread extends Thread{  

    String line=null;
    BufferedReader  is = null;
    PrintWriter os=null;
    Socket s=null;
    AddressBook myBook= null;

    public ServerThread(Socket s,AddressBook myBook){
        this.s=s;
        this.myBook = myBook;
    }

    public void run() {
    try{
        is= new BufferedReader(new InputStreamReader(s.getInputStream()));
        os=new PrintWriter(s.getOutputStream());

    }catch(IOException e){
        System.out.println("IO error in server thread");
    }

    try {
        line=is.readLine();
        while(line.compareTo("QUIT")!=0){
        	line = line.substring(2);
        	Message.readResponse(line,myBook.myIP,myBook);
            //os.println(line);
            //os.flush();
            System.out.println("Response to Client  :  "+line);
            line=is.readLine();
        }   
    } catch (IOException e) {

        line=this.getName(); //reused String line for getting thread name
        System.out.println("IO Error/ Client "+line+" terminated abruptly");
    }
    catch(NullPointerException e){
        line=this.getName(); //reused String line for getting thread name
        System.out.println("Client "+line+" Closed");
    }

    finally{    
    try{
        System.out.println("Connection Closing..");
        if (is!=null){
            is.close(); 
            System.out.println(" Socket Input Stream Closed");
        }

        if(os!=null){
            os.close();
            System.out.println("Socket Out Closed");
        }
        if (s!=null){
        s.close();
        System.out.println("Socket Closed");
        }

        }
    catch(IOException ie){
        System.out.println("Socket Close Error");
    }
    }//end finally
    }
}
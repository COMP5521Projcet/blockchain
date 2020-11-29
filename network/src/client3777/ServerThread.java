package client3777;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
//每次收获信息都会创立一个thread
public class ServerThread extends Thread {

    String line=null;
    BufferedReader  is = null;
    PrintWriter os=null;
    Socket s=null;
    AddressBook myBook= null;
    Block block =null;

    public ServerThread(Socket s,AddressBook myBook,Block block){
        this.s=s;
        this.myBook = myBook;
        this.block = block;
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
        line = line.substring(2);
        while(line.compareTo("QUIT")!=0){	
        	System.out.println("Receive  :  "+line);
        	Message.readResponse(line,myBook.myIP,myBook,block);
            line=is.readLine();
            line = line.substring(2);
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

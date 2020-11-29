package client3777;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
//建立一个socket通信thread，要求socket通信可以一直监听，
public class SocketThread extends Thread {
	int port;
	AddressBook myBook;
	Block block;
	public SocketThread(int port,AddressBook myBook,Block block) {
		this.port = port;
		this.myBook = myBook;
		this.block = block;
	}
	public void run() {
		 Socket s=null;
		    ServerSocket ss2=null;
		    System.out.println("Server Listening......");
		    try{
		        ss2 = new ServerSocket(this.port); // can also use static final PORT_NUM , when defined

		    }
		    catch(IOException e){
		    e.printStackTrace();
		    System.out.println("Server error");

		    }

		    while(true){
		        try{
		            s= ss2.accept();
		            System.out.println("connection Established");
		            ServerThread st=new ServerThread(s,this.myBook,this.block);
		            st.start();

		        }

		    catch(Exception e){
		        e.printStackTrace();
		        System.out.println("Connection Error");

		    }
		    }
	}
}

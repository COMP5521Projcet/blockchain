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
		AddressBook myBook = new AddressBook(args[0],args[1]);
		SocketThread st= null;
		Block block = new Block(args[2]);
		st = new SocketThread(myBook.myIP.getPort(),myBook,block);
		st.start();
		Message.sendIP(myBook.neighbours.get(0),myBook.myIP,block);
		Message.askNeighbours(myBook.neighbours.get(0),myBook.myIP);
		//Message.sendIP(myBook.neighbours.get(0),myBook.myIP,block);
		//String block = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		//Message.sendBlock(myBook.neighbours.get(0),myBook.myIP, block);
		//Message.broadcastBlock(myBook.neighbours, myBook.myIP, block);
		//Message.sendNeighbours(myBook.neighbours.get(0), myBook.myIP, myBook.neighbours);
		
	}
	
}

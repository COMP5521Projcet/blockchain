package client3777;

import java.io.DataOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.Socket;
import java.util.ArrayList;

public class Message {
	
	public enum Type {SENDIP,ASKNEIGHBOURS,SENDBLOCK,SENDNEIGHBOURS,BROADCASTBLOCK,RECEIVIP};
	
	public static void sendIP(IpAddress ipAddress, IpAddress myIpAddress) {
		try {
			JSONObject sampleObject = new JSONObject();
		    sampleObject.put("type", Type.SENDIP);
		    sampleObject.put("myIP", myIpAddress.getIpAddress());
		    socket(ipAddress, sampleObject.toString());
		}catch(Exception e) {
			System.out.println("Json error occurred.");
		    e.printStackTrace();
		}
		
	}
	
	public static void askNeighbours(IpAddress ipAddress, IpAddress myIpAddress) {
		try {
			JSONObject sampleObject = new JSONObject();
		    sampleObject.put("type", Type.ASKNEIGHBOURS);
		    sampleObject.put("myIP", myIpAddress.getIpAddress());
		    socket(ipAddress, sampleObject.toString());
		}catch(Exception e) {
			System.out.println("Json error occurred.");
		    e.printStackTrace();
		}
	}
	
	public static void sendNeighbours(IpAddress ipAddress, IpAddress myIpAddress,ArrayList<IpAddress> neighbours) {
		String str = "";
		for (int counter = 0; counter < neighbours.size(); counter++) { 
			String neigAddress = neighbours.get(counter).getIpAddress();
			if(!ipAddress.equal(neigAddress)) {
				str += neigAddress+"&";
			}
	    } 
		str = str.substring(0,str.length()-1);
		try {
			JSONObject sampleObject = new JSONObject();
		    sampleObject.put("type", Type.SENDNEIGHBOURS);
		    sampleObject.put("myIP", myIpAddress.getIpAddress());
		    sampleObject.put("neighbours", str);
		    socket(ipAddress, sampleObject.toString());
		}catch(Exception e) {
			System.out.println("Json error occurred.");
		    e.printStackTrace();
		}
	}
	
	public static void sendBlock(IpAddress ipAddress,IpAddress myIP, String block) {
		try {
			JSONObject sampleObject = new JSONObject();
		    sampleObject.put("type", Type.SENDBLOCK);
		    sampleObject.put("myIP", myIP.getIpAddress());
		    sampleObject.put("block", block);
		    socket(ipAddress, sampleObject.toString());
		}catch(Exception e) {
			System.out.println("Json error occurred.");
		    e.printStackTrace();
		}
	}
	
	public static void broadcastBlock(ArrayList<IpAddress> neighbours,IpAddress myIP,String block) {
		for (int counter = 0; counter < neighbours.size(); counter++) { 		      
			sendBlock(neighbours.get(counter),neighbours.get(counter),block);
	    }  
		
	}
	
	private static void socket(IpAddress ipAddress, String value ) {
		try{     
			Socket s= new Socket(ipAddress.getIP(),ipAddress.getPort());  
			DataOutputStream dout=new DataOutputStream(s.getOutputStream());  
			dout.writeUTF(value);  
			dout.flush();  
			dout.close();  
			s.close();  
		}catch(Exception e){
			System.out.println(e);
		}  
			  
	}
	
	public static void receiveIP(IpAddress ipAddress, IpAddress myIpAddress) {
		try {
			JSONObject sampleObject = new JSONObject();
		    sampleObject.put("type", Type.RECEIVIP);
		    sampleObject.put("myIP", myIpAddress.getIpAddress());
		    socket(ipAddress, sampleObject.toString());
		}catch(Exception e) {
			System.out.println("Json error occurred.");
		    e.printStackTrace();
		}
		
	}
	
	public static void readResponse(String line,IpAddress myIP,AddressBook book) {
		try {
			JSONObject obj = new JSONObject(line);
			String typeString = obj.getString("type");
			String ipString = obj.getString("myIP");
			IpAddress desIP = new IpAddress(ipString); 
			switch(typeString) {
			case "RECEIVIP": 
				break;
			case "SENDIP": 
				
				ArrayList<IpAddress> newNeighbours2 = new ArrayList<IpAddress>();
				newNeighbours2.add(desIP);
				book.addAddressBook(newNeighbours2);
				receiveIP(desIP,myIP);
				break;
			case "SENDBLOCK":
				String block = obj.getString("block");
				for (int counter = 0; counter < book.neighbours.size(); counter++) { 
					if(!desIP.equal(book.neighbours.get(counter).getIpAddress())) {
						sendBlock(book.neighbours.get(counter),book.neighbours.get(counter),block);
					}
					
			    }  
				break;
			case "ASKNEIGHBOURS":
				sendNeighbours(desIP,myIP,book.neighbours);
				break;
			case "SENDNEIGHBOURS":
				String neigbourString = obj.getString("neighbours");
				String[] neigbourArray =neigbourString.split("&");
				ArrayList<IpAddress> newNeighbours = new ArrayList<IpAddress>();
				for(int i=0;i<neigbourArray.length;i++) {
					newNeighbours.add(new IpAddress(neigbourArray[i]));
				}
				book.addAddressBook(newNeighbours);
				break;
			}
		}catch(Exception e) {
			System.out.println("Json error occurred.");
		    e.printStackTrace();
		}
		
	}
	

}

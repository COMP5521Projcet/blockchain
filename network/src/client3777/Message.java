package client3777;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Message {
	
	public enum Type {SENDIP,ASKNEIGHBOURS,SENDBLOCK,SENDNEIGHBOURS,RECEIVIP,RECEIBLOCK,ASKBLOCK};
	
	
	public static String sendIP(IpAddress ipAddress, IpAddress myIpAddress, Block block) {
		String str = "";
		try {
			JSONObject sampleObject = new JSONObject();
		    sampleObject.put("type", Type.SENDIP);
		    sampleObject.put("myIP", myIpAddress.getIpAddress());
		   // sampleObject.put("height", 1);
		    sampleObject.put("height", block.getHeight());
		    socket(ipAddress, sampleObject.toString());
		}catch(Exception e) {
			System.out.println("Json error occurred.");
		    e.printStackTrace();
		}
		return str;
	}
	
	public static void receiveIP(IpAddress ipAddress, IpAddress myIpAddress, Block block) {
		try {
			JSONObject sampleObject = new JSONObject();
		    sampleObject.put("type", Type.RECEIVIP);
		    sampleObject.put("myIP", myIpAddress.getIpAddress());
		    sampleObject.put("height", 12);
		    socket(ipAddress, sampleObject.toString());
		}catch(Exception e) {
			System.out.println("Json error occurred.");
		    e.printStackTrace();
		}
		
	}
	
	public static void receiveBlock(IpAddress ipAddress, IpAddress myIpAddress, int height) {
		try {
			JSONObject sampleObject = new JSONObject();
		    sampleObject.put("type", Type.RECEIBLOCK);
		    sampleObject.put("myIP", myIpAddress.getIpAddress());
		    sampleObject.put("height", height);
		    socket(ipAddress, sampleObject.toString());
		}catch(Exception e) {
			System.out.println("Json error occurred.");
		    e.printStackTrace();
		}
		
	}
	
	public static void askBlock(IpAddress ipAddress, IpAddress myIpAddress, int height) {
		try {
			JSONObject sampleObject = new JSONObject();
		    sampleObject.put("type", Type.ASKBLOCK);
		    sampleObject.put("myIP", myIpAddress.getIpAddress());
		    sampleObject.put("height", height);
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
	
	public static void sendBlock(IpAddress ipAddress,IpAddress myIP, String blockString, int height) {
		try {
			JSONObject sampleObject = new JSONObject();
		    sampleObject.put("type", Type.SENDBLOCK);
		    sampleObject.put("myIP", myIP.getIpAddress());
		    sampleObject.put("block", blockString);
		    sampleObject.put("height", height);
		    socket(ipAddress, sampleObject.toString());
		}catch(Exception e) {
			System.out.println("Json error occurred.");
		    e.printStackTrace();
		}
	}
	
	
	
	private static void socket(IpAddress ipAddress, String value ) {
		try{     
			Socket s= new Socket(ipAddress.getIP(),ipAddress.getPort());  
			DataOutputStream dout=new DataOutputStream(s.getOutputStream());  
			System.out.println("Send to "+ipAddress.getIpAddress()+" : "+ value);
			dout.writeUTF(value);  
			dout.flush();  
			dout.close();  
			s.close();  
		}catch(Exception e){
			System.out.println(e);
		}  
			  
	}
	
	
	
	public static void readResponse(String line,IpAddress myIP,AddressBook book,Block block) {
		try {
			JSONObject obj = new JSONObject(line);
			String typeString = obj.getString("type");
			String ipString = obj.getString("myIP");
			IpAddress desIP = new IpAddress(ipString); 
			switch(typeString) {
			case "RECEIVIP": 
				int opHeightNum2 = obj.getInt("height");
				if(opHeightNum2>block.getHeight()) {
					//for(int i = block.getHeight()+1; i <=opHeightNum2; i++ ) {
					//	System.out.println("reive:"+i);
						//block.queue.offer(i);
					//}
					askBlock(desIP,myIP,block.getHeight()+1);
				}else {
				}		
				break;
			case "SENDIP": 
				ArrayList<IpAddress> newNeighbours2 = new ArrayList<IpAddress>();
				newNeighbours2.add(desIP);
				book.addAddressBook(newNeighbours2);
				int opHeightNum = obj.getInt("height");
				if(opHeightNum>block.getHeight()) {
					//for(int i = block.getHeight()+1; i <=opHeightNum; i++ ) {
					//	System.out.println("reive:"+i);
					//	block.queue.offer(i);
					//}
					askBlock(desIP,myIP,block.getHeight()+1);
				}else {
					receiveIP(desIP,myIP,block);
				}		
				break;
			case "ASKBLOCK":
				int heightNum2 = obj.getInt("height");
				sendBlock(desIP,myIP,block.getBlock(heightNum2),heightNum2);
				break;

			case "RECEIBLOCK":
				//System.out.print(block.queue.isEmpty());
				//if(!block.queue.isEmpty()) {
				sendIP(desIP,myIP,block);
				//}
				break;

			case "SENDBLOCK":
				int heightNum3 = obj.getInt("height");
				String blockString = obj.getString("block");
				if(heightNum3 == block.height+1) {
					
					//receiveBlock(desIP,myIP,heightNum3);
					//block.queue.peek();
					block.addBlock(heightNum3, blockString);
					sendIP(desIP,myIP,block);
				}else {
					
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

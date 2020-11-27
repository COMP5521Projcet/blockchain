package client3777;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class AddressBook {
	public IpAddress myIP;
	
	public ArrayList<IpAddress> neighbours = new ArrayList<IpAddress>();
	
	public String fname = "myAddressBook.txt";
	
	public AddressBook(String MyIPString,String initNeig) {
		this.myIP = new IpAddress(MyIPString);
		this.writeAddressInit(initNeig);
		this.loadAddressBook();
	}
	
	private void loadAddressBook() {		
		try {
			File myObj = new File(this.fname);
		    Scanner myReader = new Scanner(myObj);
		    while (myReader.hasNextLine()) {
		       String data = myReader.nextLine();
		       this.neighbours.add(new IpAddress(data));
		    }
		    myReader.close();
		} catch(IOException e) {
			System.out.println("An error occurred.");
		    e.printStackTrace();
		}
	}
	
	private void writeAddressInit(String initNeig) {
		String line = System.getProperty("line.separator"); 
		try {
			File myObj = new File(this.fname);
			if (myObj.createNewFile()) {
				System.out.println("File created: " + myObj.getName());
				FileWriter myWriter = new FileWriter("myAddressBook.txt");
			      
				myWriter.write(initNeig);
				myWriter.write(line); 	
			    
			    myWriter.close();
			    System.out.println("Successfully wrote to the file.");
			} else {
				System.out.println("File already exists.");
			}
			
		} catch(IOException e) {
			System.out.println("An error occurred.");
		    e.printStackTrace();
		}
	}
	
	private void writeAddressBook() {
		String line = System.getProperty("line.separator"); 
		try {
			File myObj = new File(this.fname);
			if (myObj.createNewFile()) {
				System.out.println("File created: " + myObj.getName());
			} else {
				System.out.println("File already exists.");
			}
			FileWriter myWriter = new FileWriter("myAddressBook.txt");
			for (int counter = 0; counter < this.neighbours.size(); counter++) { 		      
				myWriter.write(this.neighbours.get(counter).getIpAddress());
				myWriter.write(line); 	
		    }  
		    myWriter.close();
		    System.out.println("Successfully wrote to the file.");
		} catch(IOException e) {
			System.out.println("An error occurred.");
		    e.printStackTrace();
		}
	}
	
	public void  addAddressBook(ArrayList<IpAddress> newNeighbours) {
		for (int counter = 0; counter < newNeighbours.size(); counter++) { 
			boolean same = false;
			for (int counteri = 0; counteri < this.neighbours.size(); counteri++) {
				if(this.neighbours.get(counteri).equal(newNeighbours.get(counter).getIpAddress())) {
					same = true;
					break;
				}
			}
			if(!same) {
				this.neighbours.add(newNeighbours.get(counter));
			}

	    }
		
		this.writeAddressBook();
		this.loadAddressBook();
	}
	
	public void deleteAddressBook(String deleteIP) {
		this.neighbours.remove(new IpAddress(deleteIP));
		this.writeAddressBook();
		this.loadAddressBook();
	}
}

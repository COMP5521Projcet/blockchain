package client3777;

public class IpAddress {
	private String ip;
	private String port;
	
	public IpAddress(String Address) {
		this.set(Address);
	}
	
	public IpAddress(String ip,String port) {
		this.ip = ip;
		this.port = port;
	}
	
	public void set(String Address) {
	    String[] arrOfStr = Address.split(":", -2); 
	    this.ip = arrOfStr[arrOfStr.length-2] ;
	    this.port= arrOfStr[arrOfStr.length-1];
	}
	
	public String getIP() {
		return this.ip;
	}
	
	public int getPort() {
		return Integer.parseInt(this.port);
	}
	
	public String getIpAddress() {
		return this.ip +":"+this.port;
	}
	
	public boolean equal(String Address) {
		String[] arrOfStr = Address.split(":", -2); 
	    if(this.ip.equals(arrOfStr[arrOfStr.length-2])&&this.port.equals(arrOfStr[arrOfStr.length-1])) {
	    	return true;
	    }
		return false;
	}
}

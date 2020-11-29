package client3777;

//Store one IpAddress into format of Ip + port 
public class IpAddress {
	private String ip;
	private String port;
	//check it valid or not, it can response or not
	private Boolean valid = false;
	
	public IpAddress(String Address) {
		this.set(Address);
	}
	
	public IpAddress(String ip,String port) {
		this.ip = ip;
		this.port = port;
	}
	
	public IpAddress(String ip, String port, Boolean valid) {
		this.ip = ip;
		this.port = port;
		this.valid = valid;
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
	
	public Boolean getValid() {
		return this.valid;
	}
	
	public void setValid(Boolean valid) {
		this.valid = valid;
	}
	
	//check it equal or not
	public boolean equal(String Address) {
		String[] arrOfStr = Address.split(":", -2); 
	    if(this.ip.equals(arrOfStr[arrOfStr.length-2])&&this.port.equals(arrOfStr[arrOfStr.length-1])) {
	    	return true;
	    }
		return false;
	}
}

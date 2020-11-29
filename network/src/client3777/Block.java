package client3777;

import java.util.LinkedList;
import java.util.Queue;

public class Block {
	public int height = 5;
	public Queue<Integer> queue = new LinkedList<Integer>();
	
	public Block(String height) {
		this.height = Integer.parseInt(height);
	}
	public int getHeight() {
		return this.height;
	}
	
	public String getBlock(int height) {
		String block = "The block's height is "+height;
		return block;
	}
	
	public void addBlock(int height, String blockString) {
		this.height = this.height +1;
		System.out.println("height = height+1:"+this.height);
	}
}

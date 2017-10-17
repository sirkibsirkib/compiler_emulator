package assembly;

import compiler.Compiler;


public class BinData implements BinUnit{
	private byte[] data;
	private String comment;
	
	public BinData(byte[] data, String comment) {
		this.data = data;
		this.comment = comment;
	}
	
	@Override
	public int numBytesTaken() {
		return data.length;
	}
	
	@Override
	public String toString(){
		String s = Compiler.hex(data);
		while(s.length() < 30){
			s += ' ';
		}
		s += "//" + comment;
		return s;
	}
	
	@Override
	public byte[] compile(){
		return data;
	}
}

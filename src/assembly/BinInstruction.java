package assembly;

import java.util.List;

import compiler.Compiler;

public class BinInstruction implements BinUnit{
	private OpCode code;
	protected byte[] argData;
	String comment;
	
	public BinInstruction(OpCode code, List<Byte> argData){
		this.code = code;
		this.argData = new byte[argData.size()];
		for(int i = 0; i < argData.size(); i++){
			this.argData[i] = argData.get(i);
		}
	}
	
	public BinInstruction(OpCode code, byte[] argData){
		this.code = code;
		this.argData = argData;
	}
	
	public BinInstruction(OpCode code, String comment){
		this.code = code;
		argData = new byte[0];
		this.comment = comment;
	}
	
	public BinInstruction(OpCode code, byte[] argData, String comment){
		this.code = code;
		this.argData = argData;
		this.comment = comment;
	}
	@Override
	public String toString(){
		String s = code.name() + " " + Compiler.hex(argData);
		while(s.length() < 30){
			s += ' ';
		}
		s += " //" + comment;
		return s;
	}

	public int numBytesTaken() {
		return argData.length+1;
	}
	
	public OpCode getOpCode(){
		return code;  
	}

	public void overWriteData(byte[] argData) {
		this.argData = argData;
	}
	
	public byte[] compile(){
		byte[] bytes = new byte[numBytesTaken()];
		bytes[0] = code.getByte();
		for(int i = 1; i < bytes.length; i++){
			bytes[i] = argData[i-1];
		}
		return bytes;
	}
	
	public static BinInstruction generateEq(int byteEnum, String comment){
		OpCode eq = OpCode.eqFor(byteEnum);
		if(byteEnum == 1 || byteEnum == 2 || byteEnum == 4){
			return new BinInstruction(eq, comment);
		}else{
			return new BinInstruction(eq, Compiler.cast(byteEnum, 2), comment);
		}
	}
	
	public static BinInstruction generateChuck(int byteEnum, String comment){
		OpCode chuck = OpCode.chuckFor(byteEnum);
		if(byteEnum == 1 || byteEnum == 2 || byteEnum == 4){
			return new BinInstruction(chuck, comment);
		}else{
			return new BinInstruction(chuck, Compiler.cast(byteEnum, 2), comment);
		}
	}
	
	public static BinInstruction generatePush(byte[] value, int byteEnum, String comment){
		OpCode push = OpCode.pushFor(byteEnum);
		if(byteEnum == 1 || byteEnum == 2 || byteEnum == 4){
			return new BinInstruction(push, value, comment);
		}else{
			return new BinInstruction(push, Compiler.concat(Compiler.cast(byteEnum, 2), value), comment);
		}
	}
}

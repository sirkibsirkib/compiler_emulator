package compiler;

import assembly.Assembles;
import assembly.BinProgram;
import assembly.BinData;

public class MethodHeader implements Assembles{
	private byte[] methodData;
	private String methodName;
	
	public MethodHeader(int retSize, int argSize, int varSize, String methodName){
		methodData = Compiler.concat(
				Compiler.cast(retSize, 2),
				Compiler.cast(varSize, 2),
				Compiler.cast(argSize, 2)
				);
		this.methodName = methodName;
	}

	public int numBytesTaken() {
		return 6;
	}

	@Override
	public String stringify(int depth) {
		return Compiler.hex(methodData) + "\n";
	}

	@Override
	public void assembleTo(BinProgram ac) {
		ac.add(new BinData(methodData, "Method Head (" + methodName + ")"));
	}
}

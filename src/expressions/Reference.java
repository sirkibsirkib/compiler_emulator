package expressions;

import java.math.BigInteger;

import compiler.Compiler;
import compiler.Variable;

import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class Reference implements Expression {
	private Variable refersTo;
	
	public Reference(Variable refersTo){
		this.refersTo = refersTo;
	}
	
	@Override
	public int getByteEnumeration() {
		return 4;
	}
	
	public int getVariableLen(){
		return refersTo.getByteSize();
	}
	
	@Override
	public String stringify(int depth){
		String s = "Reference ["+4+"]\n";
		s += Compiler.tabs(depth) + refersTo.toString();
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		OpCode referenceFrame = null;
		switch(refersTo.retArgGlobVar()){
		case 'r': referenceFrame = OpCode.PUSH_RET; break;
		case 'a': referenceFrame = OpCode.PUSH_ARG; break;
		case 'g': referenceFrame = OpCode.PUSH_GLOB; break;
		case 'v': referenceFrame = OpCode.PUSH_VAR; break;
		}
		ac.add(Compiler.generatePush(Compiler.cast(refersTo.getRelativeAddr(), 4)));
		ac.add(new BinInstruction(referenceFrame, "ref frame"));
		ac.add(new BinInstruction(OpCode.ADD_4, "ref addr"));
		
		//TODO consdier the byte sizes
	}

	@Override
	public boolean hasCompileTimeValue() {
		return false;
	}

	@Override
	public BigInteger getCompileTimeValue() {
		throw new Error();
	}
}

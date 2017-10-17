package expressions;

import java.math.BigInteger;

import compiler.Compiler;

import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class Cast implements Expression{
	private Expression exp;
	private int toNumBytes;
	
	public Cast(Expression exp, int toNumBytes){
		this.exp = exp;
		this.toNumBytes = toNumBytes;
		if(exp instanceof Cast){
			Cast cExp = (Cast) exp;
			this.exp = cExp.exp;
		}
	}

	@Override
	public void assembleTo(BinProgram ac) {
		exp.assembleTo(ac);
		OpCode cast = null;
		byte[] data = null;
		if(toNumBytes > exp.getByteEnumeration()){
			cast = OpCode.CAST_UP;
			data = Compiler.cast(toNumBytes-exp.getByteEnumeration(), 2);
		}else{
			cast = OpCode.CAST_DOWN;
			data = Compiler.cast(exp.getByteEnumeration()-toNumBytes, 2);
		}
		if(exp.getByteEnumeration() == toNumBytes){
			return;
		}
		ac.add(new BinInstruction(cast, data, "cast"));
	}

	@Override
	public int getByteEnumeration() {
		return toNumBytes;
	}
	@Override
	public String stringify(int depth){
		String s = "cast [" + exp.getByteEnumeration() + "->" + toNumBytes + "]\n";
		s += Compiler.tabs(depth) + exp.stringify(depth+1);
		return s;
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

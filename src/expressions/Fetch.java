package expressions;

import java.math.BigInteger;

import compiler.Compiler;

import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class Fetch implements Expression{
	int byteEnumeration;
	Expression exp;
	
	public Fetch(int byteEnumeration, Expression exp){
		this.byteEnumeration = byteEnumeration;
		this.exp = exp;
	}

	@Override
	public int getByteEnumeration() {
		return byteEnumeration;
	}
	
	@Override
	public String stringify(int depth){
		String s = "Fetch [" + byteEnumeration + "]\n";
		s += Compiler.tabs(depth) + exp.stringify(depth+1);
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		exp.assembleTo(ac);
		OpCode fetch = OpCode.fetchFor(getByteEnumeration());
		if(fetch == OpCode.FETCH_OTH){
			ac.add(new BinInstruction(fetch, Compiler.cast(getByteEnumeration(), 2), "fetch"));
		}else{
			ac.add(new BinInstruction(fetch, "fetch"));
		}
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

package expressions;

import java.math.BigInteger;

import compiler.Compiler;

import assembly.BinInstruction;
import assembly.BinProgram;
import assembly.OpCode;

public class Constant implements Expression {
	byte[] value;
	
	public Constant(byte... value){
		this.value = value;
	}

	@Override
	public int getByteEnumeration() {
		return value.length;
	}
	
	@Override
	public String stringify(int depth){
		return "Cons: "+ Compiler.hex(value) + "(" + new BigInteger(value).toString() + ") ["+value.length+"]\n";
	}

	@Override
	public void assembleTo(BinProgram ac) {
		ac.add(Compiler.generatePush(value));
	}

	@Override
	public boolean hasCompileTimeValue() {
		return true;
	}

	@Override
	public BigInteger getCompileTimeValue() {
		return new BigInteger(value);
	}
}

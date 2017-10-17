package instructions;

import java.math.BigInteger;

import compiler.Compiler;

import expressions.Expression;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class OUT implements Instruction, Expression{
	Expression exp;
	
	public OUT(Expression exp){
		this.exp = exp;
	}

	@Override
	public String stringify(int depth) {
		String s = "OUT\n";
		s += Compiler.tabs(depth) + exp.stringify(depth+1);
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		exp.assembleTo(ac);
		ac.add(new BinInstruction(OpCode.OUT, "out"));
	}

	@Override
	public int getByteEnumeration() {
		return 0;
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

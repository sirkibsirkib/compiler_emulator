package instructions;

import java.math.BigInteger;

import compiler.Compiler;

import expressions.Cast;
import expressions.Expression;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class FREE implements Instruction, Expression{
	Expression exp;
	
	public FREE(Expression exp){
		if(exp.getByteEnumeration() != 4){
			exp = new Cast(exp, 4);
		}
		this.exp = exp;
	}

	@Override
	public String stringify(int depth) {
		String s = "FREE\n";
		s += Compiler.tabs(depth) + exp.stringify(depth+1);
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		exp.assembleTo(ac);
		ac.add(new BinInstruction(OpCode.FREE, "free"));
	}

	@Override
	public int getByteEnumeration() {
		return 1;
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

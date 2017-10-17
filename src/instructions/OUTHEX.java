package instructions;

import java.math.BigInteger;

import compiler.Compiler;
import expressions.Cast;
import expressions.Expression;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class OUTHEX implements Instruction, Expression{
	Expression exp;
	
	public OUTHEX(Expression exp){
		if(exp.getByteEnumeration() != 6){
			exp = new Cast(exp, 6);
		}
		this.exp = exp;
	}

	@Override
	public String stringify(int depth) {
		String s = "OUTHEX\n";
		s += Compiler.tabs(depth) + exp.stringify(depth+1);
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		exp.assembleTo(ac);
		ac.add(new BinInstruction(OpCode.OUTHEX, "outhex"));
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

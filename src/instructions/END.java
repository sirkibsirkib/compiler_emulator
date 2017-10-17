package instructions;

import java.math.BigInteger;

import compiler.Compiler;

import expressions.Cast;
import expressions.Expression;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class END implements Instruction, Expression{
	Expression exp;
	
	public END(Expression exp){
		if(exp.getByteEnumeration() != 1){
			exp = new Cast(exp, 1);
		}
		this.exp = exp;
	}

	@Override
	public String stringify(int depth) {
		String s = "END\n";
		s += Compiler.tabs(depth) + exp.stringify(depth+1);
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		ac.add(new BinInstruction(OpCode.END, "end"));
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

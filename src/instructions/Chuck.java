package instructions;

import java.math.BigInteger;

import assembly.BinInstruction;
import assembly.BinProgram;

import compiler.Compiler;

import expressions.Cast;
import expressions.Expression;

public class Chuck implements Instruction, Expression{
	Expression exp;
	
	public Chuck(Expression exp){
		this.exp = exp;
		if(exp.getByteEnumeration() != 4){
			this.exp = new Cast(exp, 4);
		}
	}

	@Override
	public String stringify(int depth) {
		String s = "Chuck ["+exp.getByteEnumeration()+"]\n";
		s += Compiler.tabs(depth) + exp.stringify(depth+1);
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		exp.assembleTo(ac);
		ac.add(BinInstruction.generateChuck(exp.getByteEnumeration(), "chuck"));
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

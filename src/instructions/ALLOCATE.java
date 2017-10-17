package instructions;

import java.math.BigInteger;

import compiler.Compiler;
import expressions.Cast;
import expressions.Expression;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class ALLOCATE implements Instruction, Expression{
	Expression exp;
	
	public ALLOCATE(Expression exp){
		this.exp = exp;
		if(exp.getByteEnumeration() != 4){
			this.exp = new Cast(exp, 4);
		}
	}

	@Override
	public String stringify(int depth) {
		String s = "ALLOCATE\n";
		s += Compiler.tabs(depth) + exp.stringify(depth+1);
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		exp.assembleTo(ac);
		ac.add(new BinInstruction(OpCode.ALLOCATE, "allocate"));
	}

	@Override
	public int getByteEnumeration() {
		return 4;
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

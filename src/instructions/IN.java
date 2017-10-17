package instructions;

import java.math.BigInteger;

import expressions.Expression;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class IN implements Instruction, Expression{

	@Override
	public String stringify(int depth) {
		return "IN\n";
	}

	@Override
	public void assembleTo(BinProgram ac) {	
		ac.add(new BinInstruction(OpCode.IN, "in"));
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

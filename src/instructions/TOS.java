package instructions;

import java.math.BigInteger;

import assembly.BinInstruction;
import assembly.BinProgram;
import assembly.OpCode;
import expressions.Expression;

public class TOS implements Instruction, Expression{
	
	public TOS(){
		
	}

	@Override
	public String stringify(int depth) {
		return "TOS\n";
	}

	@Override
	public void assembleTo(BinProgram ac) {
		ac.add(new BinInstruction(OpCode.TOS, "tos"));
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

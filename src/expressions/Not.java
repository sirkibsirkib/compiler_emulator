package expressions;

import java.math.BigInteger;

import compiler.Compiler;

import assembly.Assembles;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class Not implements Expression, Assembles {
	Expression exp;
	
	public Not(Expression exp){
		this.exp = exp;
	}
	
	@Override
	public String stringify(int depth) {
		String s = "Not\n";
		s += Compiler.tabs(depth) + exp.stringify(depth+1);
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		int expBytes = exp.getByteEnumeration();
		OpCode eq = OpCode.eqFor(expBytes);
		BinInstruction aoE = null;
		if(eq == OpCode.EQ_OTH){
			aoE = new BinInstruction(eq, Compiler.cast(expBytes, 2), "not");
		}else{
			aoE = new BinInstruction(eq, "not");
		}
		exp.assembleTo(ac);
		ac.add(Compiler.generatePush(Compiler.cast(0, expBytes)));
		ac.add(aoE);
	}

	@Override
	public int getByteEnumeration() {
		return 1;
	}

	@Override
	public boolean hasCompileTimeValue() {
		return exp.hasCompileTimeValue();
	}

	@Override
	public BigInteger getCompileTimeValue() {
		if(!hasCompileTimeValue()){
			throw new Error();
		}
		return exp.getCompileTimeValue().compareTo(BigInteger.ZERO) == 0 ?
				BigInteger.ONE : BigInteger.ZERO;
	}

}

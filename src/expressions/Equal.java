package expressions;

import java.math.BigInteger;

import compiler.Compiler;

import assembly.Assembles;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class Equal implements Expression, Assembles {
	Expression left,
	right;
	
	public Equal(Expression left, Expression right){
		this.left = left;
		this.right = right;
	}

	@Override
	public String stringify(int depth) {
		String s = "==\n";
		s += Compiler.tabs(depth) + left.stringify(depth+1);
		s += Compiler.tabs(depth) + right.stringify(depth+1);
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		int byteEnum = left.getByteEnumeration();
		OpCode eq = OpCode.eqFor(byteEnum);
		BinInstruction aoe = null;
		if(eq == OpCode.LESS_OTH){
			aoe = new BinInstruction(eq, Compiler.cast(byteEnum, 2), "equal");
		}else{
			aoe = new BinInstruction(eq, "equal");
		}
		left.assembleTo(ac);
		right.assembleTo(ac);
		ac.add(aoe);
	}

	@Override
	public int getByteEnumeration() {
		return 1;
	}

	@Override
	public boolean hasCompileTimeValue() {
		return left.hasCompileTimeValue() && left.hasCompileTimeValue();
	}

	@Override
	public BigInteger getCompileTimeValue() {
		return left.getCompileTimeValue().compareTo(right.getCompileTimeValue()) == 0 ?
				BigInteger.ONE : BigInteger.ZERO;
	}

}

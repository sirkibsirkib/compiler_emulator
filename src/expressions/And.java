package expressions;

import java.math.BigInteger;

import compiler.Compiler;

import assembly.Assembles;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class And implements Expression, Assembles {
	Expression left, right;
	
	public And(Expression left, Expression right){
		if(left.getByteEnumeration() < right.getByteEnumeration()){
			left = new Cast(left, right.getByteEnumeration());
		}
		if(right.getByteEnumeration() < left.getByteEnumeration()){
			right = new Cast(right, left.getByteEnumeration());
		}
		this.left = left;
		this.right = right;
	}
	
	@Override
	public String stringify(int depth) {
		String s = "And\n";
		s += Compiler.tabs(depth) + left.stringify(depth+1);
		s += Compiler.tabs(depth) + right.stringify(depth+1);
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		int expBytes = left.getByteEnumeration();
		BinInstruction andIns = null;
		OpCode and = OpCode.andFor(expBytes);
		if(and == OpCode.AND_OTH){
			andIns = new BinInstruction(and, Compiler.cast(expBytes, expBytes), "and");
		}
		andIns = new BinInstruction(and, "and");
		left.assembleTo(ac);
		right.assembleTo(ac);
		ac.add(andIns);
	}

	@Override
	public int getByteEnumeration() {
		return 1;
	}

	@Override
	public boolean hasCompileTimeValue() {
		return left.hasCompileTimeValue() && right.hasCompileTimeValue();
	}

	@Override
	public BigInteger getCompileTimeValue() {
		return left.getCompileTimeValue().compareTo(BigInteger.ZERO) == 1 &&
				right.getCompileTimeValue().compareTo(BigInteger.ZERO) == 1 ?
				BigInteger.ONE : BigInteger.ZERO;
	}

}

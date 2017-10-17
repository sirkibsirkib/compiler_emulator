package expressions;

import java.math.BigInteger;

import compiler.Compiler;

import assembly.Assembles;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class LessThan implements Expression, Assembles{
	boolean strictly;
	Expression left, right;
	
	public LessThan(Expression left, Expression right, boolean strictly){
		this.left = left;
		this.right = right;
		this.strictly = strictly;
		if(left.getByteEnumeration() > right.getByteEnumeration()){
			right = new Cast(right, left.getByteEnumeration());
		}else if(left.getByteEnumeration() < right.getByteEnumeration()){
			left = new Cast(left, right.getByteEnumeration());
		}
	}
	
	@Override
	public String stringify(int depth) {
		String s = "<"+ (strictly? ' ': '=') +"\n";
		s += Compiler.tabs(depth) + left.stringify(depth+1);
		s += Compiler.tabs(depth) + right.stringify(depth+1);
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		int byteEnum = left.getByteEnumeration();
		OpCode lt = strictly? OpCode.lessFor(byteEnum) : OpCode.lEqFor(byteEnum);
		BinInstruction aol = null;
		if(lt == OpCode.LESS_OTH){
			aol = new BinInstruction(lt, Compiler.cast(byteEnum, 2), "less");
		}else{
			aol = new BinInstruction(lt, "less");
		}
		right.assembleTo(ac);
		left.assembleTo(ac);
		ac.add(aol);
	}

	@Override
	public int getByteEnumeration() {
		return 1;
	}

	@Override
	public boolean hasCompileTimeValue() {
		return left.hasCompileTimeValue() &&  right.hasCompileTimeValue();
	}

	@Override
	public BigInteger getCompileTimeValue() {
		if(!hasCompileTimeValue()){
			throw new Error();
		}
		if(strictly){
			return left.getCompileTimeValue().compareTo(right.getCompileTimeValue()) == -1 ?
					BigInteger.ONE : BigInteger.ZERO;
				
		}else{
			return left.getCompileTimeValue().compareTo(right.getCompileTimeValue()) != 1 ?
					BigInteger.ONE : BigInteger.ZERO;
		}
	}
	
}

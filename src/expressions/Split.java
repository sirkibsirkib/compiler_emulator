package expressions;

import java.math.BigInteger;

import compiler.Compiler;

import assembly.Assembles;
import assembly.BinProgram;

public class Split implements Expression, Assembles{
	Expression[] exp;
	
	public Split(Expression... exp){
		this.exp = exp;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		for(int i = exp.length-1; i >= 0; i--){
			exp[i].assembleTo(ac);
		}
	}

	@Override
	public int getByteEnumeration() {
		int total = 0;
		for(int i = exp.length-1; i >= 0; i--){
			total += exp[i].getByteEnumeration();
		}
		return total;
	}
	
	@Override
	public String stringify(int depth){
		String s = "Split " + getByteEnumeration() + "\n";
		for(int i = 0; i < exp.length; i++){
			s += Compiler.tabs(depth) + exp[i].stringify(depth+1);
		}
		return s;
	}

	@Override
	public boolean hasCompileTimeValue() {
		for(Expression e : exp){
			if(!e.hasCompileTimeValue()){
				return false;
			}
		}
		return true;
	}

	@Override
	public BigInteger getCompileTimeValue() {
		BigInteger total = BigInteger.ZERO;
		int shift = 0;
		for(int i = exp.length; i >= 0; i--){
			total = total.add(exp[i].getCompileTimeValue().shiftLeft(8*shift));
			shift += exp[i].getByteEnumeration();
		}
		return total;
	}

}

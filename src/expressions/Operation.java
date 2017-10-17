package expressions;

import java.math.BigInteger;

import compiler.Compiler;

import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class Operation implements Expression {
	Expression left;
	Expression right;
	char operChar;

	public Operation(Expression left, Expression right, char operChar) {
		if(left.getByteEnumeration() > right.getByteEnumeration()){
			right = new Cast(right, left.getByteEnumeration());
		}else if(left.getByteEnumeration() < right.getByteEnumeration()){
			left = new Cast(left, right.getByteEnumeration());
		}
		this.left = left;
		this.right = right;
		this.operChar = operChar;
	}

	@Override
	public int getByteEnumeration() {
		return Math.max(left.getByteEnumeration(), right.getByteEnumeration());
	}
	
	@Override
	public String stringify(int depth){
		String s = "operation " + operChar + " [" + getByteEnumeration() + "]\n";
		s += Compiler.tabs(depth) + left.stringify(depth+1);
		s += Compiler.tabs(depth) + right.stringify(depth+1);
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		OpCode oc = null;
		byte[] othBytes = Compiler.cast(getByteEnumeration(), 2);
		switch(operChar){
		case '+': oc = OpCode.addFor(getByteEnumeration()); break;
		case '-': oc = OpCode.subFor(getByteEnumeration()); break;
		case '*': oc = OpCode.mulFor(getByteEnumeration()); break;
		case '/': oc = OpCode.divFor(getByteEnumeration()); break;
		case '%': oc = OpCode.modFor(getByteEnumeration()); break;
		default: throw new Error();
		}

		right.assembleTo(ac);
		left.assembleTo(ac);
		if(oc == OpCode.ADD_OTH || oc == OpCode.SUB_OTH){
			ac.add(new BinInstruction(oc, othBytes, "operation"));
		}else{
			ac.add(new BinInstruction(oc, "operation"));
		}
		
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
		switch(operChar){
		case '+': return left.getCompileTimeValue().add(right.getCompileTimeValue());
		case '-': return left.getCompileTimeValue().subtract(right.getCompileTimeValue());
		case '*': return left.getCompileTimeValue().multiply(right.getCompileTimeValue());
		case '/': return left.getCompileTimeValue().divide(right.getCompileTimeValue());
		case '%': return left.getCompileTimeValue().mod(right.getCompileTimeValue());
		default: throw new Error();
		}
	}
}

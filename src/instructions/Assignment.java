package instructions;

import compiler.Compiler;

import expressions.Expression;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class Assignment implements Instruction{
	Expression left, right;
	
	public Assignment(Expression left, Expression right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public void assembleTo(BinProgram ac) {

		right.assembleTo(ac);
		left.assembleTo(ac);
		OpCode save =  OpCode.saveFor(right.getByteEnumeration());
		if(save == OpCode.SAVE_OTH){
			ac.add(new BinInstruction(save, Compiler.cast(right.getByteEnumeration(),2), "assignment"));
		}else{
			ac.add(new BinInstruction(save, "assignment"));
		}
		
	}

	@Override
	public String stringify(int depth) {
		String s = "assign [" + right.getByteEnumeration() + "]\n";
		s += Compiler.tabs(depth) + left.stringify(depth+1);
		s += Compiler.tabs(depth) + right.stringify(depth+1);
		return s;
	}
}

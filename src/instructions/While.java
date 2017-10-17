package instructions;

import java.util.List;

import compiler.Compiler;

import expressions.Expression;
import assembly.AddressMarker;
import assembly.Assembles;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class While implements Instruction, Assembles{
	Expression condition;
	List<Instruction> nested;
	
	public While(Expression condition, List<Instruction> nested){
		this.condition = condition;
		this.nested = nested;
	}
	@Override
	public void assembleTo(BinProgram ac) {
		ac.myAddressStart(this);
		AddressMarker exit = new AddressMarker();
		int byteEnum = condition.getByteEnumeration();
		condition.assembleTo(ac); //push condition
		ac.add(BinInstruction.generatePush(Compiler.cast(0, byteEnum), byteEnum, "while"));
		ac.add(BinInstruction.generateEq(byteEnum, "while"));
		ac.add(ac.getLinkedBinInstruction(OpCode.IF_JUMP, exit, "while"));
		for(Instruction i : nested){
			i.assembleTo(ac);
		}
		ac.add(ac.getLinkedBinInstruction(OpCode.JUMP, this, "while"));
		ac.myAddressStart(exit);		
	}
	
	@Override
	public String stringify(int depth) {
		String s = "While\t" + condition.stringify(depth+1);
		for(Instruction i : nested){
			s += Compiler.tabs(depth) + i.stringify(depth+1);
		}
		return s;
	}
}

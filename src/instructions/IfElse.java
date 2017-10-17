package instructions;

import java.util.List;

import compiler.Compiler;
import compiler.HasAddress;
import expressions.Expression;
import assembly.AddressMarker;
import assembly.Assembles;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class IfElse implements Instruction, Assembles{
	Expression condition;
	IfElse previous, next;
	List<Instruction> nested;
	AddressMarker endOfChain;
	
	public IfElse(Expression condition, List<Instruction> nested, IfElse previous){
		this.condition = condition;
		if(previous != null){
			this.previous = previous;
			previous.next = this;
		}
		endOfChain = previous == null? new AddressMarker() : previous.endOfChain;
		//all linked elses share a marker
		this.nested = nested;
	}
	@Override
	public void assembleTo(BinProgram ac) {
		HasAddress following = next == null ? new AddressMarker() : next;
		ac.myAddressStart(this);
		if(nested == null || nested.size() == 0){
			return;
		}
		if(condition != null){
			int byteEnum = condition.getByteEnumeration();
			condition.assembleTo(ac);
			ac.add(BinInstruction.generatePush(Compiler.cast(0, byteEnum), byteEnum, "if"));
			ac.add(BinInstruction.generateEq(byteEnum, "if"));
			//stack has 1 if condition is FALSE
			ac.add(ac.getLinkedBinInstruction(OpCode.IF_JUMP, following, "if"));
		}
		
		//enter THEN
		for(Instruction i : nested){
			i.assembleTo(ac);
		}
		ac.add(ac.getLinkedBinInstruction(OpCode.JUMP, endOfChain, "if"));
		//exit THEN jump to end
		
		
		if(next == null){
			//I AM THE END OF THE CHAIN
			ac.myAddressStart(following);
			ac.myAddressStart(endOfChain);
		}
	}
	
	@Override
	public String stringify(int depth) {
		String head = "";
		if(previous != null) head += "else ";
		if(condition != null)head += "if";
		String s = "";
		if(condition != null){
			s = head  + "\t" + condition.stringify(depth+1);
		}else{
			s = head + '\n';
		}
		
		if(nested != null){
			for(Instruction i : nested){
				s += Compiler.tabs(depth) + i.stringify(depth+1);
			}
		}
		return s;
	}
}

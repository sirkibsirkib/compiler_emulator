package instructions;

import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class Return implements Instruction {
	@Override
	public String stringify(int depth){
		return "Return\n";
	}

	@Override
	public void assembleTo(BinProgram ac) {
		ac.add(new BinInstruction(OpCode.RET, "return"));
	}
}

package expressions;

import java.math.BigInteger;

import assembly.Assembles;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class FrameRefPush implements Expression, Assembles {
	private char mode;

	public FrameRefPush(char mode) {
		this.mode = mode;
	}

	@Override
	public String stringify(int depth) {
		return "RefFramePush " + mode + "|\n";
	}

	@Override
	public void assembleTo(BinProgram ac) {
		OpCode oc = null;
		switch(mode){
		case 'a' : oc = OpCode.PUSH_ARG; break;
		case 'v' : oc = OpCode.PUSH_VAR; break;
		case 'r' : oc = OpCode.PUSH_RET; break;
		case 'g' : oc = OpCode.PUSH_GLOB; break;
		default: throw new Error();
		}
		ac.add(new BinInstruction(oc, "refFrame " + mode));
	}

	@Override
	public int getByteEnumeration() {
		return 4;
	}

	@Override
	public boolean hasCompileTimeValue() {
		return false;
	}

	@Override
	public BigInteger getCompileTimeValue() {
		throw new Error();
	}
}

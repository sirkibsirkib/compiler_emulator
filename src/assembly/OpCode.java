package assembly;

import emulator.RunError;
import compiler.Compiler;

public enum OpCode {
	END(0x00),	//ends program. no data.
	OUT(0x01),	
	PUSH_ARG(0x02),	//pushes 4 byte argument address to stack
	PUSH_VAR(0x03), //pushes 4 byte variable address to stack
	PUSH_RET(0x04), //pushes 4 byte return address to stack
	PUSH_GLOB(0x04), //pushes 4 byte global address to stack
	RET(0x05), //returns
	CAST_UP(0x06, 2), 
	CAST_DOWN(0x07, 2), 
	INVOKE(0x08, 4), //data is 4 bytes = addr of "method area"
	ALLOCATE(0x09),
	FREE(0x0a),
	ADD_1(0x0b),
	ADD_2(0x0c),
	ADD_4(0x0d),
	ADD_OTH(0x0e, 2),
	SUB_1(0x0f),
	SUB_2(0x10),
	SUB_4(0x11),
	SUB_OTH(0x12, 2),
	PUSH_1(0x13, 1),
	PUSH_2(0x14, 2),
	PUSH_4(0x15, 4),
	PUSH_OTH(0x16, 2),
	FETCH_1(0x17),
	FETCH_2(0x18),
	FETCH_4(0x19),
	FETCH_OTH(0x1a, 2),
	SAVE_1(0x1b),
	SAVE_2(0x1c),
	SAVE_4(0x1d),
	SAVE_OTH(0x1e, 2),
	IN(0x1f),			//TODO how many bytes?
	JUMP(0x20, 4),
	LESS_1(0x21),
	LESS_2(0x22),
	LESS_4(0x23),
	LESS_OTH(0x24, 2),
	EQ_1(0x25),
	EQ_2(0x26),
	EQ_4(0x27),
	EQ_OTH(0x28, 2),
	OR_1(0x29),
	OR_2(0x2a),
	OR_4(0x2b),
	OR_OTH(0x2c, 2),
	AND_1(0x2d),
	AND_2(0x2e),
	AND_4(0x2f),
	AND_OTH(0x30, 2),
	DUP_1(0x31),
	DUP_2(0x32),
	DUP_4(0x33),
	DUP_OTH(0x34, 2),
	L_EQ_1(0x35),
	L_EQ_2(0x36),
	L_EQ_4(0x37),
	L_EQ_OTH(0x38, 2),
	IF_JUMP(0x39, 4), //jumps if pop TOS == '01'. has 4 bytes of addr
	MUL_1(0x3a),
	MUL_2(0x3b),
	MUL_4(0x3c),
	MUL_OTH(0x3d, 2),
	DIV_1(0x3e),
	DIV_2(0x3f),
	DIV_4(0x40),
	DIV_OTH(0x41, 2),
	MOD_1(0x42),
	MOD_2(0x43),
	MOD_4(0x44),
	MOD_OTH(0x45, 2),
	IF_N_JUMP(0x46, 4),
	HOLD(0x47),
	TOS(0x48),
	CHUCK_1(0x49),
	CHUCK_2(0x4a),
	CHUCK_4(0x4b),
	CHUCK_OTH(0x4c, 2),
	OUTHEX(0x4d);
	
	
	private byte code;
	private int argLength;
	OpCode(int code, int argLength){
		this.code = (byte) code;
		this.argLength = argLength;
	}
	
	OpCode(int code){
		this.code = (byte) code;
		this.argLength = 0;
	}
	
	public int getArgLength(){
		return argLength;
	}

	public byte getByte() {
		return code;
	}

	public static OpCode getOpWithByteValue(byte b) {
		for(OpCode oc : values()){
			if(oc.code == b){
				return oc;
			}
		}
		throw new RunError("Can't find op code <" + b + ">" + Compiler.hex(Compiler.cast(b, 1)));
	}
	
	public static OpCode chuckFor(int bytes){
		switch(bytes){
		case 1: return CHUCK_1;
		case 2: return CHUCK_2;
		case 4: return CHUCK_4;
		default: return CHUCK_OTH;
		}
	}
	
	public static OpCode mulFor(int bytes){
		switch(bytes){
		case 1: return MUL_1;
		case 2: return MUL_2;
		case 4: return MUL_4;
		default: return MUL_OTH;
		}
	}
	
	public static OpCode divFor(int bytes){
		switch(bytes){
		case 1: return DIV_1;
		case 2: return DIV_2;
		case 4: return DIV_4;
		default: return DIV_OTH;
		}
	}
	
	public static OpCode modFor(int bytes){
		switch(bytes){
		case 1: return MOD_1;
		case 2: return MOD_2;
		case 4: return MOD_4;
		default: return MOD_OTH;
		}
	}
	
	public static OpCode dupFor(int bytes){
		switch(bytes){
		case 1: return DUP_1;
		case 2: return DUP_2;
		case 4: return DUP_4;
		default: return DUP_OTH;
		}
	}
	
	public static OpCode orFor(int bytes){
		switch(bytes){
		case 1: return OR_1;
		case 2: return OR_2;
		case 4: return OR_4;
		default: return OR_OTH;
		}
	}
	
	public static OpCode andFor(int bytes){
		switch(bytes){
		case 1: return AND_1;
		case 2: return AND_2;
		case 4: return AND_4;
		default: return AND_OTH;
		}
	}
	
	public static OpCode pushFor(int bytes){
		switch(bytes){
		case 1: return PUSH_1;
		case 2: return PUSH_2;
		case 4: return PUSH_4;
		default: return PUSH_OTH;
		}
	}
	
	public static OpCode eqFor(int bytes){
		switch(bytes){
		case 1: return EQ_1;
		case 2: return EQ_2;
		case 4: return EQ_4;
		default: return EQ_OTH;
		}
	}
	
	public static OpCode lEqFor(int bytes){
		switch(bytes){
		case 1: return L_EQ_1;
		case 2: return L_EQ_2;
		case 4: return L_EQ_4;
		default: return L_EQ_OTH;
		}
	}
	
	public static OpCode lessFor(int bytes){
		switch(bytes){
		case 1: return LESS_1;
		case 2: return LESS_2;
		case 4: return LESS_4;
		default: return LESS_OTH;
		}
	}
	
	public static OpCode addFor(int bytes){
		switch(bytes){
		case 1: return ADD_1;
		case 2: return ADD_2;
		case 4: return ADD_4;
		default: return ADD_OTH;
		}
	}
	
	public static OpCode subFor(int bytes){
		switch(bytes){
		case 1: return SUB_1;
		case 2: return SUB_2;
		case 4: return SUB_4;
		default: return SUB_OTH;
		}
	}
	
	public static OpCode fetchFor(int bytes){
		switch(bytes){
		case 1: return FETCH_1;
		case 2: return FETCH_2;
		case 4: return FETCH_4;
		default: return FETCH_OTH;
		}
	}
	
	public static OpCode saveFor(int bytes){
		switch(bytes){
		case 1: return SAVE_1;
		case 2: return SAVE_2;
		case 4: return SAVE_4;
		default: return SAVE_OTH;
		}
	}
}

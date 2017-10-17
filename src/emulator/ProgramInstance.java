package emulator;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import visualizer.Visualizer;
import assembly.OpCode;
import compiler.Compiler;

public class ProgramInstance implements HasStack{
	private Memory mem;
	private HeapManager heapManager;
	private int nextOpAddr, tosAddr, bpAddr, memSize, codeLen;
	private boolean verbose;
	private Visualizer viz;

	public ProgramInstance(byte[] code, int memorySize, boolean verbose, int tripAfterOps) {
		bpAddr = memorySize-6;
		mem = new Memory(memorySize);
		memwrite(0, code);
		if(verbose)System.out.println("ProgramInstance started");
		heapManager = new HeapManager(code.length, this, verbose);
		codeLen = code.length;
		this.verbose = verbose;
		this.tosAddr = memorySize;
		memSize = mem.size();
		viz = new Visualizer(0, memorySize);
		
		nextOpAddr = 0;
		try{
			run(tripAfterOps);
		}catch(RunError e){
			System.out.println(e.readMsg());
			e.printStackTrace();
		}
		viz.drawAllSnaps();
	}

	private void run(int tripAfterOps) {
		int ops = 0;
		if(verbose)System.out.println("Program running");
		OpCode oc = null;
		do{

			viz.addSnapof(mem, tosAddr, bpAddr, nextOpAddr);
			oc = fetchOpCode();
			//System.out.println(nextOpAddr);
			byte[] args = fetchArgBytes(oc);

			viz.addSnapof(oc, args);
			if(verbose) System.out.println("ArgData: " + Compiler.hex(args) + "(" + args.length + ")");
			int opWillBe = nextOpAddr + args.length + 1; //overwrite value of this for jumps etc.
			if(verbose) System.out.println("Op will be: " + opWillBe );
			int len;

			switch(oc){
			case ALLOCATE: push(allocate(valueOf(pop(4))));
				break;
			case HOLD: 
				int holdBytes = valueOf(pop(4));
				tosAddr -= holdBytes;
//				System.out.println("Giving space for " + holdBytes);
//				System.out.println("tos = " + tosAddr);
				push(bytesFor(tosAddr, 4));
				break;
			case FREE: push(free(valueOf(pop(4))));
				break;
				
			case TOS: push(bytesFor(tosAddr, 4));
				break;
				
			case AND_1: push(binaryLogic(pop(1), pop(1), '&'));
				break;
			case AND_2: push(binaryLogic(pop(2), pop(2), '&'));
				break;
			case AND_4: push(binaryLogic(pop(4), pop(4), '&'));
				break;
			case AND_OTH:
				len = valueOf(args);
				push(binaryLogic(pop(len), pop(len), '&'));
				break;
				
			case OR_1: push(binaryLogic(pop(1), pop(1), '|'));
				break;
			case OR_2: push(binaryLogic(pop(2), pop(2), '|'));
				break;
			case OR_4: push(binaryLogic(pop(4), pop(4), '|'));
				break;
			case OR_OTH:
				len = valueOf(args);
				push(binaryLogic(pop(len), pop(len), '|'));
				break;
				
			case CAST_DOWN: cast(valueOf(args), false);
				break;
			case CAST_UP: cast(valueOf(args), true);
				break;
				
		
				
			case DUP_1: push(memread(tosAddr, 1));
				break;
			case DUP_2: push(memread(tosAddr, 2));
				break;
			case DUP_4: push(memread(tosAddr, 4));
				break;
			case DUP_OTH:
				len = valueOf(args);
				push(memread(tosAddr, len));
				break;
				
			case END: //END LOOP
				break;
				
			case FETCH_1: fetch(1);
				break;
			case FETCH_2: fetch(2);
				break;
			case FETCH_4: fetch(4);
				break;
			case FETCH_OTH:
				len = valueOf(args);
				fetch(len);
				break;
			
			case IF_JUMP:
				if(valueOf(pop(1)) != 0){
					if(verbose)System.out.println("JUMPING!");
					opWillBe = valueOf(args);
				}else{
					if(verbose)System.out.println("NOT JUMPING!");
				}
				break;
			case IF_N_JUMP:
				if(valueOf(pop(1)) == 0){
					if(verbose)System.out.println("JUMPING!");
					opWillBe = valueOf(args);
				}else{
					if(verbose)System.out.println("NOT JUMPING!");
				}
				break;
			case JUMP: opWillBe = valueOf(args);
			break;
				
			case LESS_1: push(cmp(pop(1), pop(1), '<'));
				break;
			case LESS_2: push(cmp(pop(2), pop(2), '<'));
				break;
			case LESS_4: push(cmp(pop(4), pop(4), '<'));
				break;
			case LESS_OTH:
				len = valueOf(args);
				push(cmp(pop(len), pop(len), '<'));
				break;
				
			case L_EQ_1: push(cmp(pop(1), pop(1), '~'));
				break;
			case L_EQ_2: push(cmp(pop(1), pop(1), '~'));
				break;
			case L_EQ_4: push(cmp(pop(1), pop(1), '~'));
				break;
			case L_EQ_OTH:
				len = valueOf(args);
				push(cmp(pop(len), pop(len), '~'));
				break;
				
			case EQ_1: push(cmp(pop(1), pop(1), '='));
				break;
			case EQ_2: push(cmp(pop(2), pop(2), '='));
				break;
			case EQ_4: push(cmp(pop(4), pop(4), '='));
				break;
			case EQ_OTH:
				len = valueOf(args);
				push(cmp(pop(len), pop(len), '='));
				break;
				
			case ADD_1: push(mathOperation(pop(1), pop(1), '+'));
				break;
			case ADD_2: push(mathOperation(pop(2), pop(2), '+'));
				break;
			case ADD_4: push(mathOperation(pop(4), pop(4), '+'));
				break;
			case ADD_OTH:
				len = valueOf(args);
				push(mathOperation(pop(len), pop(len), '+'));
				break;
				
			case MOD_1: push(mathOperation(pop(1), pop(1), '%'));
				break;
			case MOD_2: push(mathOperation(pop(2), pop(2), '%'));
				break;
			case MOD_4: push(mathOperation(pop(4), pop(4), '%'));
				break;
			case MOD_OTH:
				len = valueOf(args);
				push(mathOperation(pop(len), pop(len), '%'));
				break;
				
			case MUL_1: push(mathOperation(pop(1), pop(1), '*'));
				break;
			case MUL_2: push(mathOperation(pop(2), pop(2), '*'));
				break;
			case MUL_4: push(mathOperation(pop(4), pop(4), '*'));
				break;
			case MUL_OTH:
				len = valueOf(args);
				push(mathOperation(pop(len), pop(len), '*'));
				break;
				
			case DIV_1: push(mathOperation(pop(1), pop(1), '/'));
				break;
			case DIV_2: push(mathOperation(pop(2), pop(2), '/'));
				break;
			case DIV_4: push(mathOperation(pop(4), pop(4), '/'));
				break;
			case DIV_OTH:
				len = valueOf(args);
				push(mathOperation(pop(len), pop(len), '/'));
				break;
				
				case SUB_1: push(mathOperation(pop(1), pop(1), '-'));
				break;
			case SUB_2: push(mathOperation(pop(2), pop(2), '-'));
				break;
			case SUB_4: push(mathOperation(pop(4), pop(4), '-'));
				break;
			case SUB_OTH:
				len = valueOf(args);
				push(mathOperation(pop(len), pop(len), '-'));
				break;
				
			
			case OUTHEX:
				byte[] x = pop(6);
				int addr = valueOf(subBytes(x, 0, 4));
				len = valueOf(subBytes(x, 4, 2) );
//				System.out.println("outhex addr " +addr);
//				System.out.println("outhex len " +len);
				System.out.println(Compiler.hex(memread(addr, len)));
				break;
			case OUT:
				int outChar = valueOf(pop(1));
				if(verbose){
					System.out.print("OUT:<" + (char) outChar + " 0x" + outChar + ">\n");
				}else{
					System.out.print((char) outChar);
				}
				break;
			case IN: try {
					push(bytesFor(System.in.read(), 1));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case PUSH_1: push(args);
				break;
			case PUSH_2: push(args);
				break;
			case PUSH_4: push(args);
				break;
			case PUSH_OTH:
				len = valueOf(args);
				byte[] trailer = memread(nextOpAddr+3, len);
				if(verbose) System.out.println("TRAILER:" + Compiler.hex(trailer));
				push(trailer);
				opWillBe = nextOpAddr + len + 3;
				break;
			case PUSH_ARG: push(bytesFor(myArgSpaceAt(), 4));
				break;
			case PUSH_GLOB: push(bytesFor(memSize-4, 4));
				break;
			case PUSH_RET: push(bytesFor(myRetSpaceAt(), 4));
				break;
			case PUSH_VAR: push(bytesFor(myVarSpaceAt(), 4));
				break;
				
			case INVOKE:
				//READING mh
				int mhStart = valueOf(args);
				byte[] mHeader = memread(mhStart, 6);
				byte[] nextRetSize = subBytes(mHeader, 0, 2);
				byte[] nextVarSize = subBytes(mHeader, 2, 2);
				byte[] nextArgSize = subBytes(mHeader, 4, 2);
//				System.out.println("HEADER: " + Compiler.hex(mHeader));
//
//				System.out.println("ret: " + valueOf(nextRetSize));
//				System.out.println("var: " + valueOf(nextVarSize));
//				System.out.println("args: " + valueOf(nextArgSize));
				
				//read #args from TOS. write them to TOS-retsize
				byte[] movArg = memread(tosAddr, valueOf(nextArgSize));
				mem.remove(tosAddr, valueOf(nextArgSize));
//				System.out.println("MOV ARG: " + Compiler.hex(movArg));
				memwrite(tosAddr-valueOf(nextRetSize), movArg);
//				System.out.println("next arg size " + valueOf(nextArgSize));
				viz.addSnapof(mem, tosAddr, bpAddr,valueOf(nextArgSize));
				
				//new bp is current TOS - sum of all argSpaces
				int newBp = tosAddr //calc new bp addr.
						- valueOf(nextRetSize)
						- valueOf(nextVarSize);
//						- valueOf(nextArgSize);
				
				//retsize, varsize, argsize to stack
				memwrite(newBp-14, mHeader);

				viz.addSnapof(mem, tosAddr, bpAddr, nextOpAddr);
				
				//write return op and old bp to stack
				memwrite(newBp-8, bytesFor(opWillBe, 4));
				memwrite(newBp-4, bytesFor(bpAddr, 4));

				viz.addSnapof(mem, tosAddr, bpAddr, nextOpAddr);
				
				//update nextop, bp and tos
				opWillBe = mhStart+6; //change op code				
				bpAddr = newBp; //BP switch
				tosAddr = newBp - 14; //move TOS
				break;
			case RET:
				int retOp = valueOf(memread(bpAddr-8, 4));
				int retTos = bpAddr + myArgSize() + myVarSize();
				int retBp = valueOf(memread(bpAddr-4, 4));
				opWillBe = retOp;
				tosAddr = retTos;
				bpAddr = retBp;
				break;
				
			case SAVE_1: save(pop(4), pop(1));
				break;
			case SAVE_2: save(pop(4), pop(2));
				break;
			case SAVE_4: save(pop(4), pop(4));
				break;
			case SAVE_OTH:
				len = valueOf(args);
				save(pop(4), pop(len));
				break;
				
			case CHUCK_1:
				mem.remove(tosAddr, 1);
				tosAddr += 1;
				break;
			case CHUCK_2:
				mem.remove(tosAddr, 2);
				tosAddr += 2;
				break;
			case CHUCK_4:
				mem.remove(tosAddr, 4);
				tosAddr += 4;
				break;
			case CHUCK_OTH:
				len = valueOf(args);
				mem.remove(tosAddr, len);
				tosAddr += len;
				break;
				
			
				
			default: throw new Error();
			}
			nextOpAddr = opWillBe;
			ops++;
			if(ops == tripAfterOps){
				throw new RunError(tripAfterOps + "ops run. Program tripped as per user request.");
			}
		}while(oc != OpCode.END);
	}
	
	private void fetch(int fetchBytes) {
		int addr = valueOf(pop(4));
		if(verbose) System.out.println("Fetching " + fetchBytes + " from " + addr);
		push(memread(addr, fetchBytes));
	}

	private byte[] cmp(byte[] left, byte[] right, char mode) {
		int l = valueOf(left);
		int r = valueOf(right);
		switch(mode){
		case '<': return bytesFor(l < r ? 1:0, 1);
		case '~': return bytesFor(l <= r ? 1:0, 1);
		case '=': return bytesFor(l == r ? 1:0, 1);
		default: throw new Error();
		}
	}

	private byte[] free(int numBytes) {
		int code = heapManager.free(numBytes);
		return bytesFor(code, 1);
	}

	private byte[] subBytes(byte[] array, int startIndex, int length){
		return Arrays.copyOfRange(array, startIndex, startIndex+length);
	}
	
	private void save(byte[] addrBytes, byte[] data) {
		int addr = valueOf(addrBytes);
//		System.out.println("saving " + data.length + " bytes at pos " + addr);
		memwrite(addr, data);
	}

	private int opArgsStartAt() {
		return nextOpAddr + 1;
	}
	
	private byte[] makeByteArrayLen(byte[] bytes, int len) {
		if(bytes.length == len){
			return bytes;
		}
		boolean up = len > bytes.length;
		boolean pos;
		if(bytes.length == 0){
			pos = true;
		}else if(bytes[0] < 0){
			pos = false;
		}else{
			pos = true;
		}
		if(!up){
			return subBytes(bytes, bytes.length-len, len);
		}
		byte[] upBytes = new byte[len];
		for(int i = 0; i < upBytes.length; i++){
			if(i < len-bytes.length){
				upBytes[i] = (byte) (pos? 0x00 : 0xFF);
			}else{
				upBytes[i] = bytes[i + (bytes.length - len)];
			}
		}
		return upBytes;
	}

	private void cast(int len, boolean up) {
		if(!up){
			tosAddr += len;
			return;
		}
		byte toWrite = (byte) (memReadOne(tosAddr) >= 0 ? 0x00 : 0xFF);
		byte[] filling  = new byte[len];
		for(int i = 0; i < len; i++){
			filling[i] = toWrite;
		}
		tosAddr -= len;
		memwrite(tosAddr, filling);
//		for(int i = 0; i < len; i++){
//			tosAddr--;
//			memWriteOne(tosAddr, toWrite);
//		}
	}

	

	private byte[] binaryLogic(byte[] a, byte[] b, char mode) {
		switch(mode){
		case '&': return bytesFor(valueOf(a) > 0 && valueOf(b) > 0? 1 : 0, 1);
		case '|': return bytesFor(valueOf(a) > 0 || valueOf(b) > 0? 1 : 0, 1);
		default: throw new Error();
		}
	}

	private byte[] allocate(int numBytes) {
		int addr = heapManager.allocate(numBytes);
		return bytesFor(addr, 4);
	}

	private byte[] bytesFor(int value, int numBytes){
		if(verbose) System.out.println("bytesFor " + value + " " + numBytes);
		byte[] result = new byte[numBytes];
		for(int i = 0; i < numBytes; i++){
			result[i] = (byte) ((value>>((numBytes-1-i)*8))&0xFF);
		}
		if(verbose) System.out.println(">> " + Compiler.hex(result));
		return result;
	}
	
	private int valueOf(byte[] bytes){
		int val = 0;
		if(bytes.length > 4){
			throw new Error();
		}
		for(int i = bytes.length-1; i >= 0; i--){
			val += (bytes[i]&0xFF)<<(bytes.length-1-i)*8;
		}
		return val;
	}
	
	private byte[] mathOperation(byte[] left, byte[] right, char opChar){
		if(left.length != right.length){
			throw new Error();
		}

//		System.out.println("IN: " + Compiler.hex(left) + " " + opChar + " " + Compiler.hex(right));
		BigInteger l = new BigInteger(left);
		BigInteger r = new BigInteger(right);
//		System.out.println("left " + l + " right " + r + " op " + opChar + " leftlen" + left.length + " rightlen" + right.length);
		BigInteger result;
		switch(opChar){
		case '+': result = l.add(r); break;
		case '-': result = l.subtract(r); break;
		case '*': result = l.multiply(r); break;
		case '/': result = l.divide(r); break;
		case '%': result = l.mod(r); break;
		default: throw new Error();
		}
		byte[] res = makeByteArrayLen(result.toByteArray(), left.length);
//		System.out.println("OUT: " + Compiler.hex(res));
		return res;
	}
	
	private byte[] pop(int bytes){
		byte[] x = memread(tosAddr, bytes);
		mem.remove(tosAddr, bytes);
		tosAddr += bytes;
		if(verbose) System.out.println("popping " +
				Compiler.hex(x) + "(" + x.length + ")");
		
		return x;
	}
	
	private void push(byte[] bytes){
		if(verbose) System.out.println("pushing " +
				Compiler.hex(bytes) + "(" + bytes.length + ")");
		tosAddr -= bytes.length;
		memwrite(tosAddr, bytes);
	}

	private byte[] fetchArgBytes(OpCode oc) {
		return memread(nextOpAddr+1, oc.getArgLength());
	}

	private OpCode fetchOpCode() {
		return OpCode.getOpWithByteValue(memReadOne(nextOpAddr));
	}
	
	private byte memReadOne(int addr){
		byte[] memRead = memread(addr, 1);
		return memRead[0];
	}
	
	private void memWriteOne(int addr, byte toWrite) {
		byte[] mw = new byte[1];
		mw[0] = toWrite;
		memwrite(addr, mw);
	}
	
	private void memwrite(int addr, byte[] toWrite) {
		if(viz != null){
			viz.addWriteSnap(addr, toWrite);
		}
		mem.write(addr, toWrite);
	}

	private int myRetSize(){
		int readFrom = bpAddr - 14;
		return valueOf(memread(readFrom, 2));
	}
	
	private int myArgSize(){
		int readFrom = bpAddr - 12;
		return valueOf(memread(readFrom, 2));
	}
	
	private int myVarSize(){
		int readFrom = bpAddr - 10;
		return valueOf(memread(readFrom, 2));
	}
	
	private byte[] memread(int addr, int len) {
		viz.addReadSnap(addr, len);
		try{
			return mem.read(addr, len);
		}catch(RunError re){
			viz.addCrash(addr, len);
			//TODO where the fuck is the crash line
			System.out.println("CRASH");
			throw re;
		}
		
	}

	private int myOldOp(){
		int readFrom = bpAddr - 8;
		return valueOf(memread(readFrom, 2));
	}
	
	private int myOldBp(){
		int readFrom = bpAddr - 4;
		return valueOf(memread(readFrom, 2));
	}
	
	private int myRetSpaceAt(){
		return bpAddr + myVarSize() + myArgSize();
	}
	
	private int myArgSpaceAt(){
		return bpAddr + myVarSize();
	}
	
	private int myVarSpaceAt(){
		return bpAddr;
	}

	@Override
	public int getTosAddr() {
		return tosAddr;
	}

}

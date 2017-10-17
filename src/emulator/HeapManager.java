package emulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HeapManager {
	private List<MemoryBoundsBlock> blocks;
	private HasStack hs;
	private int start;
	boolean verbose;
	
	public HeapManager(int start, HasStack hs, boolean verbose){
		blocks = new ArrayList<>();
		this.start = start;
		this.hs = hs;
		this.verbose = verbose;
	}
	
	public int allocate(int length){
		if(verbose)System.out.println("Allocating " + length);
		int lastSuccess = -1;
		for(int i = start; i < hs.getTosAddr(); i++){
			MemoryBoundsBlock m = usingAddr(i);
			if(verbose)System.out.println("||" + lastSuccess + "  " + i);
			if(m == null){
				if(lastSuccess == -1){
					lastSuccess = i;
				}
				if(i - lastSuccess >= length){
					blocks.add(new MemoryBoundsBlock(lastSuccess, length));
					Collections.sort(blocks);
					return lastSuccess;
				}
			}else{
				lastSuccess = -1;
				i = m.getEndNotInclusive()-1;
			}
		}
		throw new RunError("Out of memory!");
	}

	private MemoryBoundsBlock usingAddr(int addr){
		for(int i = 0; i < blocks.size(); i++){
			MemoryBoundsBlock next = blocks.get(i);
			if(next.getStartIndex() <= i && i < next.getEndNotInclusive()){
				return next;
			}
		}
		return null;
	}

	public int free(int addr) {
		for(int i = 0; i < blocks.size(); i++){
			if(blocks.get(i).getStartIndex() == addr){
				blocks.remove(i);
				if(verbose)System.out.println("FREE SUCCESSFUL");
				return 1;
			}
		}
		return 0;
	}

	public int getEndOfHeap() {
		if(blocks.size() == 0){
			return start;
		}
		return blocks.get(blocks.size()-1).getEndNotInclusive();
	}
}

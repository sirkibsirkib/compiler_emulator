package emulator;

public class MemoryBoundsBlock implements Comparable<MemoryBoundsBlock> {
	private int startIndex, endNotInclusive;
	
	public MemoryBoundsBlock(int startIndex, int length){
		this.startIndex = startIndex;
		this.endNotInclusive = startIndex + length;
	}
	
	public int getStartIndex(){
		return startIndex;
	}
	
	public int getEndNotInclusive(){
		return endNotInclusive;
	}

	@Override
	public int compareTo(MemoryBoundsBlock o) {
		return startIndex - o.startIndex;
	}
}

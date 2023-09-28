package application;

public class Block {
	private int startAddress;
	private int sizeInMB;
	private boolean allocated;

	public Block(int sizeInMB) {
		this.sizeInMB = sizeInMB;
		this.allocated = false;
	}

	public int getStartAddress() {
		return startAddress;
	}

	public void setStartAddress(int startAddress) {
		this.startAddress = startAddress;
	}

	public int getSizeInMB() {
		return sizeInMB;
	}

	public void setSizeInMB(int sizeInMB) {
		this.sizeInMB = sizeInMB;
	}

	public boolean isAllocated() {
		return allocated;
	}

	public void allocate() {
		allocated = true;
	}

	public void deallocate() {
		allocated = false;
	}
}
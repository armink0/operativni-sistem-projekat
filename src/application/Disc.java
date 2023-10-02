package application;

import java.util.ArrayList;
import java.util.List;

public class Disc {
	static List<Block> discMemoryBlocks = new ArrayList<>();
	private static final int TOTAL_DISC_MEMORY_BLOCKS = 256;
	static final int blockSizeInMB = 4;

	public List<Block> allocateMemoryBlocks(int fileSizeInMB) {
		List<Block> allocatedBlocks = new ArrayList<>();
		int blocksRequired = (int) Math.ceil((double) fileSizeInMB / blockSizeInMB);

		int contiguousCount = 0;

		for (Block block : discMemoryBlocks) {
			if (!block.isAllocated()) {
				contiguousCount++;
				allocatedBlocks.add(block);

				if (contiguousCount == blocksRequired) {
					for (Block allocatedBlock : allocatedBlocks) {
						allocatedBlock.allocate();
					}

					return allocatedBlocks;
				}
			} else {
				contiguousCount = 0;
				allocatedBlocks.clear();
			}
		}

		for (Block block : allocatedBlocks) {
			block.deallocate();
		}

		return null;
	}

	public void deallocateMemoryBlocks(List<Block> blocksToDeallocate) {
		for (Block block : blocksToDeallocate) {
			block.deallocate();
		}
	}

	public static void createMemoryBlocks() {
		for (int i = 0; i < TOTAL_DISC_MEMORY_BLOCKS; i++) {
			discMemoryBlocks.add(new Block(blockSizeInMB));
		}
	}
}

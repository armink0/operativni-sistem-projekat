package application;

import java.util.ArrayList;
import java.util.List;

public class Memory {
	private List<MemorySegment> memorySegments;

	public Memory(List<MemorySegment> initialSegments) {
		this.memorySegments = new ArrayList<>(initialSegments);
	}

	public List<MemorySegment> allocateMemory(Process process, int memorySize) {
		if (memorySize <= 0) {
			throw new IllegalArgumentException("Invalid memory size for allocation");
		}

		int totalAvailableMemory = memorySegments.size() * memorySegments.get(0).getSize();
		if (memorySize > totalAvailableMemory) {
			System.out.println("Requested memory size exceeds total available memory. Allocation failed.");
			return new ArrayList<>(); // Return an empty list to indicate allocation failure
		}

		// Check if there are enough free memory segments
		List<MemorySegment> allocatedSegments = new ArrayList<>();
		int remainingMemorySize = memorySize;
		boolean allSegmentsOccupied = true;

		for (MemorySegment segment : memorySegments) {
			if (segment.getProcess() == null) {
				allSegmentsOccupied = false; // At least one segment is available
				int segmentSize = segment.getSize();
				if (segmentSize >= remainingMemorySize) {
					// Allocate the entire segment
					segment.setProcess(process);
					allocatedSegments.add(segment);
					remainingMemorySize -= segmentSize;
					if (remainingMemorySize <= 0) {
						break;
					}
				} else {
					// Allocate a part of the segment
					segment.setProcess(process);
					allocatedSegments.add(segment);
					remainingMemorySize -= segmentSize;
				}
			}

			// If all memory is allocated, break the loop
			if (allocatedSegments.size() == memorySegments.size()) {
				break;
			}
		}

		// If all segments are occupied, print a message
		if (allSegmentsOccupied) {
			System.out.println("Not enough available memory segments to allocate for the process.");
		}

		// If there's not enough memory, deallocate the allocated segments
		if (remainingMemorySize > 0 && allocatedSegments.size() < memorySegments.size()) {
			for (MemorySegment segment : allocatedSegments) {
				segment.setProcess(null);
			}
			allocatedSegments.clear();
		}

		return allocatedSegments;
	}

	// Deallocate memory segments associated with processes in "DONE" state
	public void deallocateDoneProcesses() {
		for (MemorySegment segment : memorySegments) {
			Process process = segment.getProcess();
			if (process != null && process.isCompleted()) {
				segment.setProcess(null);
			}
		}
	}

	// Get the number of available memory segments
	public int getNumAvailableSegments() {
		int numAvailableSegments = 0;
		for (MemorySegment segment : memorySegments) {
			if (segment.getProcess() == null) {
				numAvailableSegments++;
			}
		}
		return numAvailableSegments;
	}

	// Get the list of memory segments
	public List<MemorySegment> getMemorySegments() {
		return memorySegments;
	}

	// Represents a memory segment allocated to a process
	public static class MemorySegment {
		private Process process;
		private int size;

		public MemorySegment(Process process, int size) {
			this.process = process;
			this.size = size;
		}

		public Process getProcess() {
			return process;
		}

		public void setProcess(Process process) {
			this.process = process;
		}

		public int getSize() {
			return size;
		}
	}
}

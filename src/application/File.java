package application;

import java.util.List;

public class File {
    private String name;
    private int sizeInMB;
    private List<Block> allocatedBlocks;

    public File(String name, int sizeInMB) {
        this.name = name;
        this.sizeInMB = sizeInMB;
        this.allocatedBlocks = null; // Initialize allocated blocks as null
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSizeInMB() {
        return sizeInMB;
    }

    public void setSizeInMB(int sizeInMB) {
        this.sizeInMB = sizeInMB;
    }

    public List<Block> getAllocatedBlocks() {
        return allocatedBlocks;
    }

    public void setAllocatedBlocks(List<Block> allocatedBlocks) {
        this.allocatedBlocks = allocatedBlocks;
    }

    @Override
    public String toString() {
        return name;
    }
}

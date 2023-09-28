package application;

import java.util.ArrayList;
import java.util.List;

public class Directory {
    private String name;
    private List<Directory> subdirectories;
    private List<File> files;
    private Directory parent;

    public Directory(String name) {
        this.name = name;
        subdirectories = new ArrayList<>();
        files = new ArrayList<>();
        parent = null;
    }

    public void addDirectory(Directory directory) {
        subdirectories.add(directory);
    }

    public void addFile(File file) {
        files.add(file);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Directory> getSubdirectories() {
        return subdirectories;
    }

    public void setSubdirectories(List<Directory> subdirectories) {
        this.subdirectories = subdirectories;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return name;
    }

    public Directory getParent() {
        return parent;
    }

    public void setParent(Directory parent) {
        this.parent = parent;
    }

    public Directory createDirectory(String name) {
        Directory newDirectory = new Directory(name);
        subdirectories.add(newDirectory);
        newDirectory.setParent(this);
        return newDirectory;
    }

    public void createFile(String name, int sizeInMB, List<Block> allocatedBlocks) {
        File newFile = new File(name, sizeInMB);
        newFile.setAllocatedBlocks(allocatedBlocks); // Set the allocated blocks for the file
        files.add(newFile);
    }

    public Directory getSubdirectoryByName(String name) {
        for (Directory subdir : subdirectories) {
            if (subdir.getName().equals(name)) {
                return subdir;
            }
        }
        return null;
    }

    public Directory changeToSubdirectory(String name) {
        Directory subdir = getSubdirectoryByName(name);
        if (subdir != null) {
            return subdir;
        } else {
            System.out.println("Directory not found: " + name);
            return this;
        }
    }

    public void deleteDirectory(String name) {
        Directory directoryToDelete = getSubdirectoryByName(name);
        if (directoryToDelete != null) {
            subdirectories.remove(directoryToDelete);
        }
    }

    public void deleteFile(String name) {
        File fileToDelete = getFileByName(name);
        if (fileToDelete != null) {
            files.remove(fileToDelete);
        }
    }

    private File getFileByName(String name) {
        for (File file : files) {
            if (file.getName().equals(name)) {
                return file;
            }
        }
        return null;
    }
}

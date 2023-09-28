package application;


public class FileSystem {
	private Directory root;

    public FileSystem() {
        root = new Directory("Root");
    }

    public Directory getRoot() {
        return root;
    }

}
package application;

import application.Directory;

public class FileSystem {
	private Directory root;

    public FileSystem() {
        root = new Directory("Root");
    }

    public Directory getRoot() {
        return root;
    }

}


package application;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Main extends Application {
    private static List<Block> memoryBlocks = new ArrayList<>();
    private static final int TOTAL_MEMORY_BLOCKS = 256; //Adjust as needed
    private static final int blockSizeInMB = 4; //Adjust as needed

    FileSystem fileSystem = new FileSystem();
    Directory root = fileSystem.getRoot();
    Directory currentDirectory = fileSystem.getRoot();

    public static void main(String[] args) {
    	createMemoryBlocks();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            TextArea textarea = new TextArea();
            textarea.setEditable(false);
            textarea.setMinHeight(300);

            textarea.setStyle(
                "-fx-border-color: white; -fx-border-width: 1px; -fx-control-inner-background: black; -fx-text-fill: green; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 3px;"
            );
            TextField textField = new TextField();
            textField.setStyle(
                "-fx-border-color: white; -fx-border-width: 1px; -fx-background-color: black; -fx-text-fill: green; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 3px;"
            );
            textField.setMinWidth(450);
            Button enter = new Button();
            enter.setMinWidth(80);
            enter.setText("enter");

            enter.setStyle(
                "-fx-background-color: black; -fx-border-color: white; -fx-border-width: 2px; -fx-text-fill: green; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 3px;"
            );
            HBox hbox = new HBox(textField, enter);
            hbox.setStyle("-fx-background-color: black;");
            HBox.setMargin(textField, new Insets(5, 10, 5, 5));
            HBox.setMargin(enter, new Insets(5, 5, 5, 0));
            VBox vbox = new VBox(textarea, hbox);
            vbox.setStyle("-fx-background-color: black;");

            VBox.setMargin(textarea, new Insets(5, 5, 5, 5));
            Scene scene = new Scene(vbox, 550, 365);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            textField.requestFocus();
            primaryStage.setTitle("OS");
            primaryStage.setScene(scene);
            primaryStage.show();
            

            textarea.appendText("Current Directory: Root\n");
            textarea.appendText("Enter command, '..' to go back or 'end':\n");
            
            textField.setOnKeyPressed(event -> {
				if (event.getCode() == KeyCode.ENTER) {
					enter.fire();
					event.consume();
				}
			});
            
            enter.setOnAction(e -> {
                String userInput = textField.getText();

                if (userInput.equals("..")) {
                    if (currentDirectory.getParent() != null) {
                        currentDirectory = currentDirectory.getParent();
                    } else {
                        textarea.appendText("Already in root directory.\n-------------\n");
                    }
                } else {
                    if (userInput.split(" ")[0].equals("mkdir")) {
                        // Checking for the same name
                        int flag = 0;
                        for (Directory d : currentDirectory.getSubdirectories()) {
                            if (userInput.split(" ")[1].equals(d.toString())) {
                                textarea.appendText("Existing folder.\n");
                                flag = 1;
                                break;
                            }
                        }

                        if (flag == 0) {
                            Directory d = currentDirectory.createDirectory(userInput.split(" ")[1]);
                            Directory d1 = currentDirectory.getSubdirectories().get(0);
                            textarea.appendText("New directory created.\n");
                        }
                    } else if (userInput.split(" ")[0].equals("cd")) {
                        currentDirectory = currentDirectory.changeToSubdirectory(userInput.split(" ")[1]);
                    } else if (userInput.equals("exit")) {
                        primaryStage.close();
                    } else if (userInput.split(" ")[0].equals("touch")) {
                        String fileName = userInput.split(" ")[1];
                        int fileSizeInMB = Integer.parseInt(userInput.split(" ")[2]);

                        int numBlocksNeeded = (int) Math.ceil((double) fileSizeInMB / blockSizeInMB);

                        if (numBlocksNeeded <= memoryBlocks.size()) {
                            List<Block> allocatedBlocks = allocateMemoryBlocks(fileSizeInMB);

                            if (allocatedBlocks != null) {
                                currentDirectory.createFile(fileName, fileSizeInMB, allocatedBlocks);
                                textarea.appendText("File created and memory allocated.\n");
                                System.out.println("File name: " + fileName + ", file size: " + fileSizeInMB +" MB, allocated blocks: " + allocatedBlocks.size());
                                
                            } else {
                                textarea.appendText("Not enough memory to allocate the file.\n");
                            }
                        } else {
                            textarea.appendText("Not enough memory to allocate the file.\n");
                        }
                    } else if (userInput.equals("cd ..")) {
                        currentDirectory = currentDirectory.getParent();
                        
                    } else if (userInput.equals("df")) {
                    	int availableBlocks = 0;
                        for(Block b : memoryBlocks) {
                        	if(!b.isAllocated())
                        		availableBlocks++;
                        }
                        textarea.appendText("Memory available: " + availableBlocks * blockSizeInMB + " MB\n");
                        
                    } else if (userInput.split(" ")[0].equals("rm")) {
                        for (Directory d : currentDirectory.getSubdirectories()) {
                            if (userInput.split(" ")[1].equals(d.toString())) {
                                currentDirectory.deleteDirectory(d.getName());
                                textarea.appendText("Directory deleted.\n");
                                break;
                            }
                        }

                        for (File f : currentDirectory.getFiles()) {
                            if (userInput.split(" ")[1].equals(f.toString())) {
                                currentDirectory.deleteFile(f.getName());
                                // Deallocate memory when a file is deleted
                                deallocateMemoryBlocks(f.getAllocatedBlocks());
                                textarea.appendText("File deleted and memory deallocated.\n");
                                break;
                            }
                        }
                    } else if (userInput.split(" ")[0].equals("dir") || userInput.split(" ")[0].equals("ls")) {
                        textarea.appendText("Subdirectories:\n");
                        for (Directory subdir : currentDirectory.getSubdirectories()) {
                            textarea.appendText(subdir.getName() + "\n");
                        }
                        textarea.appendText("-----------\n");

                        textarea.appendText("Files:\n");
                        for (File file : currentDirectory.getFiles()) {
                            textarea.appendText(file.getName() + "\n");
                        }
                        textarea.appendText("-----------\n");
                    } else {
                        textarea.appendText("Unknown command.\n");
                    }
                }

                textarea.appendText("Current Directory: " + currentDirectory + "\n");
                textarea.appendText("Enter command, '..' to go back:\n");
                textField.clear();
                textField.requestFocus();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Block> allocateMemoryBlocks(int fileSizeInMB) {
        List<Block> allocatedBlocks = new ArrayList<>();
        int blocksRequired = (int) Math.ceil((double) fileSizeInMB / blockSizeInMB);

        // Check for available contiguous memory blocks
        int contiguousCount = 0;
        
        for (Block block : memoryBlocks) {
            if (!block.isAllocated()) {
                contiguousCount++;
                allocatedBlocks.add(block);
                
                if (contiguousCount == blocksRequired) {
                    // Found enough contiguous blocks; mark them as allocated
                    for (Block allocatedBlock : allocatedBlocks) {
                        allocatedBlock.allocate();
                    }
                    return allocatedBlocks;
                }
            } else {
                contiguousCount = 0; // Reset if allocated block encountered
                allocatedBlocks.clear();
            }
        }

        // Deallocate blocks if allocation failed
        for (Block block : allocatedBlocks) {
            block.deallocate();
        }

        return null; // Not enough contiguous memory blocks
    }

    
    private int getAvailableMemorySize() {
        int availableMemory = 0;
        for (Block block : memoryBlocks) {
            if (!block.isAllocated()) {
                availableMemory += blockSizeInMB;
            }
        }
        return availableMemory;
    }



    private void deallocateMemoryBlocks(List<Block> blocksToDeallocate) {
        for (Block block : blocksToDeallocate) {
            block.deallocate(); // Mark the block as deallocated
        }
    }



    private static void createMemoryBlocks() {
        for (int i = 0; i < TOTAL_MEMORY_BLOCKS; i++) {
            memoryBlocks.add(new Block(blockSizeInMB));
        }
    }
}

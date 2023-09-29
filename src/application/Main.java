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
	private static List<Block> diskMemoryBlocks = new ArrayList<>();
	private static final int TOTAL_DISK_MEMORY_BLOCKS = 256;
	private static final int blockSizeInMB = 4;

	private static int totalMemory = 2048;
	private static int numSegments = 4;
	private static List<Memory.MemorySegment> initialSegments = new ArrayList<>();

	FileSystem fileSystem = new FileSystem();
	Directory root = fileSystem.getRoot();
	Directory currentDirectory = fileSystem.getRoot();
	FCFSScheduler scheduler = new FCFSScheduler();
	static Memory memory = null;

	public static void main(String[] args) {
		int initialSegmentSize = totalMemory / numSegments;

		for (int i = 0; i < numSegments; i++) {
			Memory.MemorySegment segment = new Memory.MemorySegment(null, initialSegmentSize);
			initialSegments.add(segment);
		}

		memory = new Memory(initialSegments);

		createMemoryBlocks();
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			Thread schedulerThread = new Thread(() -> scheduler.runScheduler());
			schedulerThread.start();

			TextArea textarea = new TextArea();
			textarea.setEditable(false);
			textarea.setMinHeight(300);

			textarea.setStyle(
					"-fx-border-color: white; -fx-border-width: 1px; -fx-control-inner-background: black; -fx-text-fill: green; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 3px;");
			TextField textField = new TextField();
			textField.setStyle(
					"-fx-border-color: white; -fx-border-width: 1px; -fx-background-color: black; -fx-text-fill: green; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 3px;");
			textField.setMinWidth(450);
			Button enter = new Button();
			enter.setMinWidth(80);
			enter.setText("enter");

			enter.setStyle(
					"-fx-background-color: black; -fx-border-color: white; -fx-border-width: 2px; -fx-text-fill: green; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 3px;");
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
				memory.deallocateDoneProcesses();

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

						if (numBlocksNeeded <= diskMemoryBlocks.size()) {
							List<Block> allocatedBlocks = allocateMemoryBlocks(fileSizeInMB);

							if (allocatedBlocks != null) {
								currentDirectory.createFile(fileName, fileSizeInMB, allocatedBlocks);
								textarea.appendText("File created and memory allocated.\n");
								System.out.println("File name: " + fileName + ", file size: " + fileSizeInMB
										+ " MB, allocated blocks: " + allocatedBlocks.size());

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
						for (Block b : diskMemoryBlocks) {
							if (!b.isAllocated())
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
					} else if (userInput.split(" ")[0].equals("run")) {
						String processName = userInput.split(" ")[1];
						int memorySize = Integer.parseInt(userInput.split(" ")[2]);

						boolean processExists = false;
						for (Memory.MemorySegment segment : memory.getMemorySegments()) {
							Process existingProcess = segment.getProcess();
							if (existingProcess != null && existingProcess.getName().equals(processName)) {
								processExists = true;
								System.out.println(
										"A process with name " + processName + " is already in memory. Ignored.");
								break;
							}
						}

						if (memory.getNumAvailableSegments() > 0 && !processExists) {
							Process p = new Process(processName, 10, memorySize);

							List<Memory.MemorySegment> allocatedSegments = memory.allocateMemory(p, memorySize);

							for (Memory.MemorySegment segment : allocatedSegments) {
								segment.setProcess(p);
							}

							if (allocatedSegments.size() > 0) {
								scheduler.addProcess(p);

								textarea.appendText("process " + p.getName() + ", " + p.getState() + ", time: "
										+ p.getExecutionTime() + "\n");
							} else {
								textarea.appendText("Not enough memory for process.\n");
							}
						} else {
							System.out.println("Not enough available memory segments to allocate for the process.");
						}

						System.out.println("Memory allocation status:");
						List<Memory.MemorySegment> memorySegments = memory.getMemorySegments();

						for (Memory.MemorySegment segment : memorySegments) {
							Process process = segment.getProcess();
							processName = (process != null) ? process.getName() : "Unallocated";
							System.out
									.println("Memory Segment Size: " + segment.getSize() + " Process: " + processName);
						}
					} else if (userInput.equals("ps")) {
						List<Process> processesInQueue = scheduler.getProcessesInQueue();

						for (Process process : processesInQueue) {
							textarea.appendText("Process " + process.getName() + " in state: " + process.getState()
									+ ", " + process.getExecutionTime() + "\n");
						}

						Process currentProcess = scheduler.getCurrentRunningProcess();

						if (currentProcess != null) {
							textarea.appendText("Process " + currentProcess.getName() + " in state: "
									+ currentProcess.getState() + ", " + currentProcess.getExecutionTime() + "\n");
						}

						List<Process> processesFinished = scheduler.getCompletedProcesses();

						for (Process process : processesFinished) {
							textarea.appendText("Process " + process.getName() + " in state: " + process.getState()
									+ ", " + process.getExecutionTime() + "\n");
						}
					} else if (!userInput.equals("")) {
						textarea.appendText("Unknown command.\n");
					}
				}

				if (!userInput.equals("")) {
					textarea.appendText("Current Directory: " + currentDirectory + "\n");
					textarea.appendText("Enter command, '..' to go back:\n");
					textField.clear();
					textField.requestFocus();
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<Block> allocateMemoryBlocks(int fileSizeInMB) {
		List<Block> allocatedBlocks = new ArrayList<>();
		int blocksRequired = (int) Math.ceil((double) fileSizeInMB / blockSizeInMB);

		int contiguousCount = 0;

		for (Block block : diskMemoryBlocks) {
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

	private int getAvailableMemorySize() {
		int availableMemory = 0;
		for (Block block : diskMemoryBlocks) {
			if (!block.isAllocated()) {
				availableMemory += blockSizeInMB;
			}
		}
		return availableMemory;
	}

	private void deallocateMemoryBlocks(List<Block> blocksToDeallocate) {
		for (Block block : blocksToDeallocate) {
			block.deallocate();
		}
	}

	private static void createMemoryBlocks() {
		for (int i = 0; i < TOTAL_DISK_MEMORY_BLOCKS; i++) {
			diskMemoryBlocks.add(new Block(blockSizeInMB));
		}
	}
}

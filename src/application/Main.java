package application;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
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
	private static int totalMemory = 2048;
	private static int numSegments = 4;
	private static List<Memory.MemorySegment> initialSegments = new ArrayList<>();

	List<String> commandHistory = new ArrayList<>();
	int currentCommandIndex = -1;

	FileSystem fileSystem = new FileSystem();
	Directory root = fileSystem.getRoot();
	Directory currentDirectory = fileSystem.getRoot();
	FCFSScheduler scheduler = new FCFSScheduler();
	static Disc disc = new Disc();
	static Memory memory = null;

	public static void main(String[] args) {
		int initialSegmentSize = totalMemory / numSegments;

		for (int i = 0; i < numSegments; i++) {
			Memory.MemorySegment segment = new Memory.MemorySegment(null, initialSegmentSize);
			initialSegments.add(segment);
		}

		memory = new Memory(initialSegments);
		Disc.createMemoryBlocks();

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
					String command = textField.getText();
					commandHistory.add(command);
					currentCommandIndex = -1;

					enter.fire();
					event.consume();
				} else if (event.getCode() == KeyCode.UP) {
					if (currentCommandIndex < commandHistory.size() - 1) {
						currentCommandIndex++;
						textField.setText(commandHistory.get(commandHistory.size() - 1 - currentCommandIndex));
						positionCaret(textField, textField.getText().length());
					}

					event.consume();
				} else if (event.getCode() == KeyCode.DOWN) {
					if (currentCommandIndex >= 0) {
						currentCommandIndex--;
						if (currentCommandIndex >= 0) {
							textField.setText(commandHistory.get(commandHistory.size() - 1 - currentCommandIndex));
						} else {
							textField.clear();
						}
						positionCaret(textField, textField.getText().length());
					}

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
						textarea.appendText("-----------\n");
						textarea.appendText("Already in root directory.\n-------------\n");
					}
				} else {
					if (userInput.split(" ")[0].equals("mkdir")) {
						int flag = 0;
						textarea.appendText("-----------\n");
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

						textarea.appendText("-----------\n");
					} else if (userInput.split(" ")[0].equals("cd")) {
						currentDirectory = currentDirectory.changeToSubdirectory(userInput.split(" ")[1]);
					} else if (userInput.equals("exit")) {
						primaryStage.close();
					} else if (userInput.split(" ")[0].equals("touch")) {
						if (userInput.split(" ").length == 3) {
							String fileName = userInput.split(" ")[1];

							if (!fileName.contains(".")) {
								textarea.appendText("-----------\n");
								textarea.appendText("File extension not defined properly.\n");
								textarea.appendText("-----------\n");
								textarea.appendText("Current Directory: " + currentDirectory + "\n");
								textarea.appendText("Enter command, '..' to go back:\n");

								return;
							}

							int fileSizeInMB = Integer.parseInt(userInput.split(" ")[2]);

							int numBlocksNeeded = (int) Math.ceil((double) fileSizeInMB / Disc.blockSizeInMB);

							if (numBlocksNeeded <= Disc.discMemoryBlocks.size()) {
								List<Block> allocatedBlocks = disc.allocateMemoryBlocks(fileSizeInMB);

								if (allocatedBlocks != null) {
									currentDirectory.createFile(fileName, fileSizeInMB, allocatedBlocks);

									textarea.appendText("-----------\n");
									textarea.appendText("File created and memory allocated.\n");
									textarea.appendText("-----------\n");

									System.out.println("File name: " + fileName + ", file size: " + fileSizeInMB
											+ " MB, allocated blocks: " + allocatedBlocks.size());

								} else {
									textarea.appendText("-----------\n");
									textarea.appendText("Not enough memory to allocate the file.\n");
									textarea.appendText("-----------\n");
								}
							} else {
								System.out.println("Memory space for file not defined");
							}
						} else {
							textarea.appendText("-----------\n");
							textarea.appendText("Not enough memory to allocate the file.\n");
							textarea.appendText("-----------\n");
						}
					} else if (userInput.equals("cd ..")) {
						currentDirectory = currentDirectory.getParent();

					} else if (userInput.equals("df")) {
						int availableBlocks = 0;

						for (Block b : Disc.discMemoryBlocks) {
							if (!b.isAllocated())
								availableBlocks++;
						}

						textarea.appendText("-----------\n");
						textarea.appendText("Memory available: " + availableBlocks * Disc.blockSizeInMB + " MB\n");
						textarea.appendText("-----------\n");

					} else if (userInput.split(" ")[0].equals("rm")) {
						textarea.appendText("-----------\n");
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

								disc.deallocateMemoryBlocks(f.getAllocatedBlocks());

								textarea.appendText("File deleted and memory deallocated.\n");
								break;
							}
						}

						textarea.appendText("-----------\n");
					} else if (userInput.split(" ")[0].equals("dir") || userInput.split(" ")[0].equals("ls")) {
						textarea.appendText("-----------\n");

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

							textarea.appendText("-----------\n");

							if (allocatedSegments.size() > 0) {
								scheduler.addProcess(p);

								textarea.appendText("Process " + p.getName() + ", " + p.getState() + ", time: "
										+ p.getExecutionTime() + "s\n");
							} else {
								textarea.appendText("Not enough memory for process.\n");
							}

							textarea.appendText("-----------\n");
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

						textarea.appendText("-----------\n");

						for (Process process : processesInQueue) {
							textarea.appendText("Process " + process.getName() + " in state: " + process.getState()
									+ ", " + process.getExecutionTime() + "s\n");
						}

						Process currentProcess = scheduler.getCurrentRunningProcess();

						if (currentProcess != null) {
							textarea.appendText("Process " + currentProcess.getName() + " in state: "
									+ currentProcess.getState() + ", " + currentProcess.getExecutionTime() + "s\n");
						}

						List<Process> processesFinished = scheduler.getCompletedProcesses();

						for (Process process : processesFinished) {
							textarea.appendText("Process " + process.getName() + " in state: " + process.getState()
									+ ", " + process.getExecutionTime() + "s\n");
						}

						textarea.appendText("-----------\n");
					} else if (!userInput.equals("")) {
						textarea.appendText("-----------\n");
						textarea.appendText("Unknown command.\n");
						textarea.appendText("-----------\n");
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

	private void positionCaret(TextField textField, int position) {
		Platform.runLater(() -> {
			textField.positionCaret(position);
		});
	}
}

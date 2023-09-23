package application;
	
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import application.Directory;
import application.File;
import application.FileSystem;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;


public class Main extends Application {
	FileSystem fileSystem = new FileSystem();
	Directory root = fileSystem.getRoot();
	Directory currentDirectory = fileSystem.getRoot();
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		
		
        
        
        
		
		
		try {
			
			TextArea textarea = new TextArea();
			textarea.setEditable(false);
			textarea.setMinHeight(300);
			
			textarea.setStyle("-fx-border-color: white; -fx-border-width: 1px; -fx-control-inner-background: black; -fx-text-fill: green; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 3px;");
			TextField textField = new TextField();
			textField.setStyle("-fx-border-color: white; -fx-border-width: 1px; -fx-background-color: black; -fx-text-fill: green; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 3px;");
			textField.setMinWidth(450);
			Button enter = new Button();
			enter.setMinWidth(80);
			enter.setText("enter");
			
			enter.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 2px; -fx-text-fill: green; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 3px;");
			HBox hbox = new HBox(textField, enter);
			hbox.setStyle("-fx-background-color: black;");
			HBox.setMargin(textField, new Insets(5,10,5,5));
			HBox.setMargin(enter, new Insets(5,5,5,0));
			VBox vbox = new VBox(textarea, hbox);
			vbox.setStyle("-fx-background-color: black;");
			
			VBox.setMargin(textarea, new Insets(5,5,5,5));
			Scene scene = new Scene(vbox,550,365);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			textField.requestFocus();
			primaryStage.setTitle("OS");
			primaryStage.setScene(scene);
			primaryStage.show();
			
			
			textarea.appendText("Current Directory: Root\n");
			textarea.appendText("Enter command, '..' to go back or 'end':\n");
			
			
			enter.setOnAction(e -> {
				
				String userInput = textField.getText();
				

		        
		        if (userInput.equals("..")) {
		            
		            if (currentDirectory.getParent() != null) {
		            	currentDirectory = currentDirectory.getParent();
		                
		                
		            } else {
		                textarea.appendText("Already in root directory.\n-------------\n");
		            }
		        } else {
		            
		        	if(userInput.split(" ")[0].equals("mkdir")) {
		        		//checking for same name
		        		int flag = 0;
		        		for(Directory d : currentDirectory.getSubdirectories()) {
		        			if(userInput.split(" ")[1].equals(d.toString())) {
		        				textarea.appendText("Existing folder.\n");
		        				flag = 1;
		        				break;
		        			}
		        		}
		        		
		        		
		        		if(flag == 0) {
		        		Directory d = currentDirectory.createDirectory(userInput.split(" ")[1]);
		        		Directory d1 = currentDirectory.getSubdirectories().get(0);
		        		textarea.appendText("New directory created.\n");
		        		}
		        	}
		        	
		        	else if(userInput.split(" ")[0].equals("cd")) {
		        		currentDirectory = currentDirectory.changeToSubdirectory(userInput.split(" ")[1]);
		        	}
		        	else if(userInput.equals("exit")) {
		        		primaryStage.close();
		        	}
		        	else if(userInput.split(" ")[0].equals("touch")) {
		        		//checking for same name
		        		int flag = 0;
		        		for(File f : currentDirectory.getFiles()) {
		        			if(userInput.split(" ")[1].equals(f.toString())) {
		        				textarea.appendText("Existing file.\n");
		        				flag = 1;
		        				break;
		        			}
		        		}
		        		
		        		if(flag == 0) {
		        		currentDirectory.createFile(userInput.split(" ")[1], 500);
		        		}
		        	}
		        	
		        	else if(userInput.equals("cd ..")) {
		        		currentDirectory = currentDirectory.getParent();
		        	}
		        	
		        	else if(userInput.split(" ")[0].equals("rm")) {
		        		for(Directory d : currentDirectory.getSubdirectories()) {
		        			if(userInput.split(" ")[1].equals(d.toString())) {
		        				currentDirectory.deleteDirectory(d.getName());
		        				textarea.appendText("Directory deleted.\n");
		        				break;
		        			}
		        		}
		        		
		        		for(File f : currentDirectory.getFiles()) {
		        			if(userInput.split(" ")[1].equals(f.toString())) {
		        				currentDirectory.deleteFile(f.getName());
		        				textarea.appendText("File deleted.\n");
		        				break;
		        			}
		        		}
		        	}
		        	
		        	else if(userInput.split(" ")[0].equals("dir") || userInput.split(" ")[0].equals("ls")) {
		        		
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
		        	}
		        	else {
		        		textarea.appendText("Unknown command.\n");
		        	}
		        	
		            
		       
		        }
		        
		        textarea.appendText("Current Directory: " + currentDirectory + "\n");
				textarea.appendText("Enter command, '..' to go back:\n");
	            textField.clear();
	            textField.requestFocus();
			});
			
			
			
	        
	        
	        	
	            
	            

	            
	            
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
}
package com.po.ping.obskoala.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.controls.JFXSnackbar;
import com.po.ping.obskoala.utils.Constants;
import com.po.ping.obskoala.utils.StemsAPIConnector;
import com.po.ping.obskoala.utils.UIUtil;

import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import io.datafx.controller.ViewController;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;

@ViewController(value = "/fxml/TaskHistoryPage.fxml", title = "Task History")
public class TaskHistoryPageController {
	
	@FXML private StackPane root;
    @FXML private JFXListView<HBox> historyList;
    @FXML private JFXScrollPane taskHistoryScroll;
    @FXML private JFXDatePicker historyDate;
    @FXML private JFXSnackbar snackbar;
    @FXML private JFXButton getEntryBtn;
    @FXML private JFXProgressBar progressBar;
    
    private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Asia/Kolkata"));
    private Service<Void> service = new ProcessService();

    /**
     * init fxml when loaded.
     */
    @PostConstruct
    public void init() {
    	progressBar.setVisible(false);
    	historyList.getItems().clear();
    	progressBar.setManaged(false);
    	
    	snackbar.registerSnackbarContainer(root);
    	historyDate.setConverter(UIUtil.dateConverter());
    	historyDate.setDayCellFactory(UIUtil.datePickerCallBack());
    	
    	getEntryBtn.setOnMouseClicked(event -> {
			if(null == historyDate.getValue())
				snackbar.show("Hmmm... You should select a date to start from.", 3000);
			else {
				historyList.getItems().clear();
				
				if(!service.isRunning()) {
					progressBar.setVisible(true);
					progressBar.setManaged(true);
					service.start();
				}

	    		service.setOnSucceeded(e -> {
	    			progressBar.setVisible(false);
	    			progressBar.setManaged(false);
	    			snackbar.show("Woohoo! Finished.", 3000);
		            service.reset();
		        });
	    		
	    		service.setOnFailed(e -> {
	    			progressBar.setVisible(false);
	    			progressBar.setManaged(false);
	    			snackbar.show("Oh No! Something went wrong...", 3000);
	    			service.reset();
	    		});				
			}
		});
    	
        historyList.setMaxHeight(500);
        JFXScrollPane.smoothScrolling((ScrollPane) taskHistoryScroll.getChildren().get(0));
    }
        
	class ProcessService extends Service<Void> {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                	LocalDate targetedDate = historyDate.getValue();
    				while(!targetedDate.isAfter(TODAY)) {					
    					try {
							String result =	StemsAPIConnector.getTimeSheetEntryForTheDay(targetedDate);
							buildListItem(targetedDate, result, true);							
						} catch (Exception e) {
							buildListItem(targetedDate, Constants.UNKNOWN, false);
						}    					
    					targetedDate = targetedDate.plusDays(1);
    				}
                    return null;
                }
            };
        }
        
        private void buildListItem(LocalDate date, String result, boolean success) {
        	
        	String styleClass = "";
        	String hoursString = result;
        	GlyphIcons icon = null;
        	
        	if(date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek()== DayOfWeek.SUNDAY) {
        		icon = FontAwesomeIcon.HOME;
        		hoursString = " -- ";
        		styleClass = "blue-lbl";
        	} else {    	
    	    	if(success) {
    	    		if(StringUtils.isBlank(result) || Constants.UNKNOWN.equals(result)) {
    	    			icon = FontAwesomeIcon.EXCLAMATION_TRIANGLE;
    	    			hoursString = Constants.UNKNOWN;
    		    		styleClass ="yellow-lbl";
    	    		}	    			
    	    		else {
    	    			int hours = Integer.parseInt(result.substring(0, result.indexOf(':')).trim());
    	    			if(hours < 8) {
    	    				styleClass = "red-lbl";
    	    				icon = FontAwesomeIcon.CALENDAR_TIMES_ALT;		
    	    			} else {
    	    				styleClass = "green-lbl";
    	    				icon = FontAwesomeIcon.CALENDAR_CHECK_ALT;
    	    			}
    	    		}
    	    	}else {
    	    		icon = FontAwesomeIcon.EXCLAMATION_TRIANGLE;
    	    		hoursString = Constants.UNKNOWN;
    	    		styleClass ="yellow-lbl";
    	    	}	    		
        	}    	
    		
    		Label timeLabel = new Label(hoursString);
    		timeLabel.getStyleClass().add("hours-label");
    		
    		Label iconLabel = GlyphsDude.createIconLabel(null == icon? FontAwesomeIcon.BOMB : icon, "   " + date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy - eeee")), "2em", "16", ContentDisplay.LEFT );
    		iconLabel.getStyleClass().add(styleClass);
    		iconLabel.setPrefWidth(250);
    		
    		HBox hbox = new HBox();
            hbox.getChildren().add(iconLabel);
            hbox.getChildren().add(timeLabel);
            hbox.setSpacing(20);
            
            Platform.runLater(() -> historyList.getItems().add(hbox));    	
        }
    }
}



package com.po.ping.obskoala.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.po.ping.obskoala.datum.BuddyCfgHandle;
import com.po.ping.obskoala.datum.IdNameHolder;
import com.po.ping.obskoala.datum.StemsMetaData;
import com.po.ping.obskoala.datum.TaskEntry;
import com.po.ping.obskoala.exceptions.StemsCustException;
import com.po.ping.obskoala.utils.Constants;
import com.po.ping.obskoala.utils.StemsAPIConnector;
import com.po.ping.obskoala.utils.StemsUtil;
import com.po.ping.obskoala.utils.UIUtil;

import io.datafx.controller.ViewController;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;

@ViewController(value = "/fxml/TaskEntryPage.fxml", title = "Task Entry")
public class TaskEntryPageController {
	
	private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Asia/Kolkata"));
	
    @FXMLViewFlowContext private ViewFlowContext context;
	@FXML private StackPane root;
    @FXML private JFXScrollPane taskEntryScroll;
            
    @FXML private JFXComboBox<IdNameHolder> projectCombo;
    @FXML private JFXDatePicker timesheetDate;
    
    @FXML private JFXTreeTableView<TaskEntry> tasksTableView;
    @FXML private JFXTreeTableColumn<TaskEntry, String> colGroup;
    @FXML private JFXTreeTableColumn<TaskEntry, String> colActivity;
    @FXML private JFXTreeTableColumn<TaskEntry, String> colDuration;
    @FXML private JFXTreeTableColumn<TaskEntry, String> colRemarks;
    
    @FXML private JFXCheckBox repeatCheckBox;
    @FXML private JFXRadioButton radio1day;
    @FXML private JFXRadioButton radio2day;
    @FXML private JFXRadioButton radio3day;
    @FXML private JFXRadioButton radio4day;
    @FXML private ToggleGroup repeatedDays;
    
    @FXML private JFXButton addEntryBtn;
    @FXML private JFXButton delEntryBtn;
    
    @FXML private JFXButton submitButton;
    @FXML private JFXButton clearButton;    

    @FXML private JFXDialog addTaskDlg;
    @FXML private JFXComboBox<IdNameHolder> tsActGrpCombo;			
	@FXML private JFXComboBox<IdNameHolder> tsActivityCombo;
	@FXML private JFXTextField tsHours;
	@FXML private JFXTextField tsMinutes;
	@FXML private JFXTextArea tsRemarks;
    @FXML private JFXButton addButton;
    @FXML private JFXButton closeButton;
    
    @FXML private JFXSnackbar snackbar;
    
    @FXML private JFXDialog messagePopup;
    @FXML private Label messageTitle;
    @FXML private Label messageBody;
    @FXML private JFXButton acceptButton;
    
    @FXML private JFXProgressBar progressBar;
    @FXML private HBox actyPrgrsContainer;
    
    private ObservableList<TaskEntry> taskList = FXCollections.observableArrayList();
    private StemsMetaData stemsCfg;
    private List<String> responseMsgs = new ArrayList<>();
    private Service<Void> service = new ProcessService();
    private Service<Void> activitySvc = new ActivityService();

    @PostConstruct
	public void init() {

		root.getChildren().remove(addTaskDlg);
		root.getChildren().remove(messagePopup);
		acceptButton.setOnMouseClicked(event -> messagePopup.close());
		snackbar.registerSnackbarContainer(addTaskDlg);
		progressBar.setVisible(false);

		try {
			prepareUIComponents();
		} catch (StemsCustException sce) {
			showPopupMessage("Oops, Something went wrong...", sce.getLocalizedMessage());
		}

		JFXScrollPane.smoothScrolling((ScrollPane) taskEntryScroll.getChildren().get(0));
	}
    
	private void prepareUIComponents() throws StemsCustException {

		stemsCfg = BuddyCfgHandle.getInstance().getMetaData();

		prepareProjectnDateFields();
		prepareTaskEntryTableView();
		setupAddEntryDialog();
		setupRepeaters();

		// Form button click handles
		submitButton.setOnMouseClicked(event -> validateAndSubmitData());
		clearButton.setOnMouseClicked(event -> {
			taskList.clear();
			projectCombo.setValue(null);
			timesheetDate.setValue(null);
			repeatCheckBox.setSelected(false);
			repeatedDays.selectToggle(radio1day);
		});
	}
    
	private void prepareProjectnDateFields() {
		// Project Combo box
		projectCombo.setCellFactory(event -> UIUtil.getComboCellfactory());
		projectCombo.setConverter(UIUtil.getComboConverter());
		projectCombo.getItems().addAll(stemsCfg.getProjects());

		// Date Picker
		timesheetDate.setConverter(UIUtil.dateConverter());
		timesheetDate.setDayCellFactory(UIUtil.datePickerCallBack());
		timesheetDate.setValue(LocalDate.now());
	}

    private void validateAndSubmitData() {    	
    	responseMsgs.clear();
    	StringBuilder errorMessages = new StringBuilder();
    	
    	if(null == projectCombo.getValue())  
    		errorMessages.append("You should choose your project.\n");
    	if(null == timesheetDate.getValue())
   			errorMessages.append("You should choose the date to submit your timesheet.\n");
   		if(taskList.isEmpty())
   			errorMessages.append("You should add the task entries to submit.");
   		
   		if(errorMessages.length() > 0) {
   			showPopupMessage("Hey, You missed something...", errorMessages.toString());
    	} else {    		

    		if(!service.isRunning()) {
    			submitButton.setDisable(true);
        		progressBar.setVisible(true);
        		service.start();
    		}

    		service.setOnSucceeded(event -> {
				submitButton.setDisable(false);
        		progressBar.setVisible(false);
        		
				if(!responseMsgs.isEmpty()) 
					showPopupMessage("Oh No! Something went wrong...", String.join("\n", responseMsgs));
				else
					showPopupMessage("Woohoo!", "Successfully submitted your timesheet.");
        		
	            service.reset();
	        });
    		
    		service.setOnFailed(event -> {
    			submitButton.setDisable(false);
        		progressBar.setVisible(false);
    			showPopupMessage("Oh No!", "Something went wrong...");
    			service.reset();
    		});
    	}  	    
	}

	private void setupAddEntryDialog() {
				
		tsActGrpCombo.setCellFactory(cell -> UIUtil.getComboCellfactory());
		tsActGrpCombo.setConverter(UIUtil.getComboConverter());
		tsActGrpCombo.getItems().addAll(stemsCfg.getActivityGroups());
		
		tsActivityCombo.setCellFactory(cell -> UIUtil.getComboCellfactory());
		tsActivityCombo.setConverter(UIUtil.getComboConverter());		
		
		setupTimeValidators();				
		setupTaskComboBoxes();
	}

	private void setupTaskComboBoxes() {
		tsActGrpCombo.setOnAction(event -> {
		    IdNameHolder selectedGrp = tsActGrpCombo.getSelectionModel().getSelectedItem();		    
		    if(null != selectedGrp) {
		    		tsActivityCombo.getItems().clear();
		    		
		    		if(!activitySvc.isRunning()) {
		    			actyPrgrsContainer.setVisible(true);
		    			actyPrgrsContainer.setManaged(true);
		    			activitySvc.start();
					}

		    		activitySvc.setOnSucceeded(e -> {
		    			actyPrgrsContainer.setVisible(false);
		    			actyPrgrsContainer.setManaged(false);
		    			activitySvc.reset();
			        });
		    		
		    		activitySvc.setOnFailed(e -> {
		    			actyPrgrsContainer.setVisible(false);
		    			actyPrgrsContainer.setManaged(false);
		    			snackbar.show("Oh No! Something went wrong in fetching activities...", 3000);
		    			activitySvc.reset();
		    		});
		    }
		});	
		
		tsActivityCombo.setOnAction(event -> {
			IdNameHolder selectedGrp = tsActGrpCombo.getSelectionModel().getSelectedItem();
			IdNameHolder selectedActivity = tsActivityCombo.getSelectionModel().getSelectedItem();
		    if(null != selectedGrp && null != selectedActivity) {
		    	tsMinutes.setText(StemsUtil.getAutoMinutesForActivity(selectedActivity.getId()));
		    	tsHours.setText(StemsUtil.getAutoHoursForActivity(selectedActivity.getId()));	
		    }
		});
	}

	private void setupTimeValidators() {
		tsHours.focusedProperty().addListener((arg0, oldValue, newValue) -> {
			if (!newValue) {
				String hours = tsHours.getText();

				if (StringUtils.isNotBlank(hours)) {
					hours = StringUtils.length(hours) == 1 ? StringUtils.leftPad(hours, 2, "0") : hours;
					if (!hours.matches("0[0-9]")) {
						tsHours.setText(StringUtils.EMPTY);
						snackbar.show("Invalid Value for Hours", 2000);
					} else
						tsHours.setText(hours);
				}
			}
		});
		
		tsMinutes.focusedProperty().addListener((arg0, oldValue, newValue) -> {
			if (!newValue) {
				String minutes = tsMinutes.getText();

				if (StringUtils.isNotBlank(minutes)) {
					minutes = StringUtils.length(minutes) == 1 ? StringUtils.leftPad(minutes, 2, "0") : minutes;
					if (!minutes.matches("[0-5][0-9]")) {
						tsMinutes.setText(StringUtils.EMPTY);
						snackbar.show("Invalid Value for Minutes", 2000);
					} else
						tsMinutes.setText(minutes);
				}
			}
		});
	}

	private void setupRepeaters() {		
		
		repeatCheckBox.setDisable(TODAY.getDayOfWeek() == DayOfWeek.SATURDAY || TODAY.getDayOfWeek() == DayOfWeek.SUNDAY || TODAY.getDayOfWeek() == DayOfWeek.MONDAY);
		repeatCheckBox.setSelected(false);

		disableRepeaterRadios();
		radio1day.setUserData(1);
		radio2day.setUserData(2);
		radio3day.setUserData(3);
		radio4day.setUserData(4);
				
		repeatCheckBox.setOnAction(event -> {
			if(repeatCheckBox.isSelected())
				enableRepeaterRadios();
			else
				disableRepeaterRadios();
		});
	}

	private void disableRepeaterRadios() {
		radio1day.setDisable(true);
		radio2day.setDisable(true);
		radio3day.setDisable(true);
		radio4day.setDisable(true);
	}

	private void enableRepeaterRadios() {
		
		switch (TODAY.getDayOfWeek()) {
		case FRIDAY: 
			radio1day.setDisable(false);
			radio2day.setDisable(false);
			radio3day.setDisable(false);
			radio4day.setDisable(false);
			break;
		case THURSDAY:
			radio1day.setDisable(false);
			radio2day.setDisable(false);
			radio3day.setDisable(false);
			radio4day.setDisable(true);
			break;
		case WEDNESDAY:
			radio1day.setDisable(false);
			radio2day.setDisable(false);
			radio3day.setDisable(true);
			radio4day.setDisable(true);
			break;
		case TUESDAY:
			radio1day.setDisable(false);
			radio2day.setDisable(true);
			radio3day.setDisable(true);
			radio4day.setDisable(true);
			break;
		default:
			disableRepeaterRadios();
			break;
		}		
	}

	private void prepareTaskEntryTableView() {
		UIUtil.setupCellValueFactory(colGroup, TaskEntry::getGroup);
		UIUtil.setupCellValueFactory(colActivity, TaskEntry::getActivity);
		UIUtil.setupCellValueFactory(colDuration, TaskEntry::getDuration);
		UIUtil.setupCellValueFactory(colRemarks, TaskEntry::getRemarks);

		tasksTableView.setRoot(new RecursiveTreeItem<>(taskList, RecursiveTreeObject::getChildren));
		tasksTableView.setShowRoot(false);

		addEntryBtn.setOnMouseClicked(event -> {
			
			actyPrgrsContainer.setVisible(false);
			actyPrgrsContainer.setManaged(false);
			
			tsActGrpCombo.setValue(null);
			tsActivityCombo.getItems().clear();
			tsActivityCombo.setValue(null);
			tsHours.setText(null);
			tsMinutes.setText(null);
			tsRemarks.setText(null);

			addTaskDlg.setTransitionType(DialogTransition.CENTER);
			addTaskDlg.show((StackPane) context.getRegisteredObject("ContentPane"));
		});

		addButton.setOnMouseClicked(event -> {
			if(validateTaskEntry()) {
				TaskEntry task = new TaskEntry();
	    		task.setTsActyGrp(tsActGrpCombo.getValue());
	    		task.setTsActivity(tsActivityCombo.getValue());
	    		task.setTsHours(tsHours.getText());
	    		task.setTsMinutes(tsMinutes.getText());
	    		task.setTsRemarks(tsRemarks.getText());
	    		
	    		taskList.add(task);
				addTaskDlg.close();
			}
		});

		closeButton.setOnMouseClicked(event -> addTaskDlg.close());
		delEntryBtn.disableProperty().bind(Bindings.isEmpty(tasksTableView.getSelectionModel().getSelectedItems()));

		delEntryBtn.setOnMouseClicked(event -> {
			taskList.remove(tasksTableView.getSelectionModel().selectedItemProperty().get().getValue());
			if (taskList.isEmpty())
				tasksTableView.getSelectionModel().clearSelection();
		});
	}
    
    private boolean validateTaskEntry() {
    	
    	if(null == tsActGrpCombo.getValue()) {
    		snackbar.show("Hmmm...You missed Activity Group.", 2500);
    		return false;
    	}
    	
    	if(null == tsActivityCombo.getValue()) {
    		snackbar.show("Hmmm...You missed Activity.", 2500);
    		return false;
    	}
    	
    	boolean isBlankHrs = StringUtils.isBlank(tsHours.getText());
    	boolean isBlankMins = StringUtils.isBlank(tsMinutes.getText());
    	boolean isZeroHrs = StringUtils.equalsIgnoreCase(Constants.STR_ZERO, tsHours.getText());
    	boolean isZeroMins = StringUtils.equalsIgnoreCase(Constants.STR_ZERO, tsMinutes.getText());
    	
    	if(isBlankHrs) {
    		snackbar.show("Hmmm...You missed Task Duration Hours.", 2500);
    		return false;
    	}
    	
    	if(isBlankMins) {
    		snackbar.show("Hmmm...You missed Task Duration Minutes.", 2500);
    		return false;
    	}
    	
    	String actvtId = tsActivityCombo.getValue().getId();
    	if(isZeroHrs && isZeroMins) {
    		snackbar.show("Hmmm... You have a Task with Zero Duration.", 2500);
    		return false;
    	}
    	
    	if(StringUtils.isBlank(tsRemarks.getText())) {
    		snackbar.show("Hmmm...You missed Task Details.", 2500);
    		return false;
    	}
    	
    	return true;
	}
	
	private void showPopupMessage(String title, String body) {
    	messageTitle.setText(StringUtils.isNotBlank(title) ? title : StringUtils.EMPTY);
		messageBody.setText(StringUtils.isNotBlank(body) ? body : StringUtils.EMPTY);
		messagePopup.setTransitionType(DialogTransition.BOTTOM);
		messagePopup.show((StackPane) context.getRegisteredObject("ContentPane"));
    }
	
	class ProcessService extends Service<Void> {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                	String projectId = projectCombo.getValue().getId();
                	LocalDate taskDate = timesheetDate.getValue();
                	int daysTobeRepeated = repeatCheckBox.isSelected() ? (int) repeatedDays.getSelectedToggle().getUserData() : 0;        	  

                	responseMsgs = StemsAPIConnector.submitTasks(stemsCfg, projectId, taskDate, taskList, daysTobeRepeated);
                    return null;
                }
            };
        }
    }
	
	
	class ActivityService extends Service<Void> {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                	List<IdNameHolder> activities = StemsAPIConnector.getActivitiesForGroup(tsActGrpCombo.getSelectionModel().getSelectedItem().getId(), stemsCfg);
					tsActivityCombo.getItems().addAll(activities);

                    return null;
                }
            };
        }
    }

}



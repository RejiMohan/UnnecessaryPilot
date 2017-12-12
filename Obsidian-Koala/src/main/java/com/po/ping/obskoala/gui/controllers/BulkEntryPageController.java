package com.po.ping.obskoala.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.po.ping.obskoala.datum.BuddyCfgHandle;
import com.po.ping.obskoala.datum.IdNameHolder;
import com.po.ping.obskoala.datum.StemsMetaData;
import com.po.ping.obskoala.datum.TaskEntry;
import com.po.ping.obskoala.exceptions.StemsCustException;
import com.po.ping.obskoala.gui.controllers.TaskEntryPageController.ProjectListService;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;

@ViewController(value = "/fxml/OneshotEntry.fxml", title = "Bulk Task Entry")
public class BulkEntryPageController {
	
	@FXMLViewFlowContext private ViewFlowContext context;
	@FXML private StackPane root;
    
    @FXML private JFXTreeTableView<TaskEntry> tasksTableView;
    @FXML private JFXTreeTableColumn<TaskEntry, String> colProject;
    @FXML private JFXTreeTableColumn<TaskEntry, String> colDate;
    @FXML private JFXTreeTableColumn<TaskEntry, String> colGroup;
    @FXML private JFXTreeTableColumn<TaskEntry, String> colActivity;
    @FXML private JFXTreeTableColumn<TaskEntry, String> colDuration;
    @FXML private JFXTreeTableColumn<TaskEntry, String> colRemarks;
    
    @FXML private JFXButton submitButton;
    @FXML private JFXButton clearButton; 

    @FXML private JFXComboBox<IdNameHolder> projectCombo;
    @FXML private JFXDatePicker timesheetDate;
    @FXML private HBox prjPrgrsContainer;
    @FXML private JFXComboBox<IdNameHolder> actGrpCombo;			
	@FXML private JFXComboBox<IdNameHolder> activityCombo;
	@FXML private JFXTextField hoursTxt;
	@FXML private JFXTextField minutesTxt;
	@FXML private JFXTextArea detailsTxtArea;
	
    @FXML private JFXButton addEntryBtn;
    @FXML private JFXButton delEntryBtn;
    @FXML private JFXButton resetFormBtn;
    @FXML private JFXButton duplicateBtn;
    
    @FXML private JFXDialog messagePopup;
    @FXML private Label messageTitle;
    @FXML private Label messageBody;
    @FXML private JFXButton okBtn;
    
    @FXML private JFXSnackbar snackbar;  
    @FXML private JFXProgressBar progressBar;
    @FXML private HBox actyPrgrsContainer;
    
    private ObservableList<TaskEntry> taskList = FXCollections.observableArrayList();
    private StemsMetaData stemsCfg;
    private List<String> responseMsgs = new ArrayList<>();
    private Service<Void> service = new ProcessService();
    private Service<Void> activitySvc = new ActivityService();
    private Service<Void> projectSvc = new ProjectListService();

    @PostConstruct
	public void init() {
    	
		try {
			prepareUIComponents();
		} catch (StemsCustException sce) {
			showPopupMessage("Oops, Something went wrong...", sce.getLocalizedMessage());
		}

	}
    
	private void prepareUIComponents() throws StemsCustException {
		
		root.getChildren().remove(messagePopup);
		okBtn.setOnMouseClicked(event -> messagePopup.close());
		snackbar.registerSnackbarContainer(root);
		progressBar.setVisible(false);
		progressBar.setManaged(false);
		
		actyPrgrsContainer.setVisible(false);
		actyPrgrsContainer.setManaged(false);

		stemsCfg = BuddyCfgHandle.getInstance().getMetaData();

		prepareTaskEntryTableView();
		setupAddEntryForm();

		// Form button click handles
		submitButton.setOnMouseClicked(event -> validateAndSubmitData());
		clearButton.setOnMouseClicked(event -> taskList.clear());
	}
    
    private void validateAndSubmitData() { 
    	
    	if(taskList.isEmpty()) {
    		snackbar.show("You haven't added any tasks to submit.", 2500);
    		return;
    	}
    	
    	responseMsgs.clear();
    	
    	if(!service.isRunning()) {
			submitButton.setDisable(true);
			progressBar.setManaged(true);
    		progressBar.setVisible(true);
    		service.start();
		}

		service.setOnSucceeded(event -> {
			submitButton.setDisable(false);
    		progressBar.setVisible(false);
    		progressBar.setManaged(false);
    		
			if(!responseMsgs.isEmpty()) 
				showPopupMessage("Oh No! Something went wrong...", String.join("\n", responseMsgs));
			else
				showPopupMessage("Woohoo!", "Successfully submitted your timesheet.");
    		
            service.reset();
        });
		
		service.setOnFailed(event -> {
			submitButton.setDisable(false);
    		progressBar.setVisible(false);
    		progressBar.setManaged(false);
			showPopupMessage("Oh No!", "Something went wrong...");
			service.reset();
		});	
 	    
	}

	private void setupAddEntryForm() throws StemsCustException {
		
		projectCombo.setCellFactory(event -> UIUtil.getComboCellfactory());
		projectCombo.setConverter(UIUtil.getComboConverter());
		prepareProjectListCombo();
		projectCombo.getSelectionModel().selectFirst();

		timesheetDate.setConverter(UIUtil.dateConverter());
		timesheetDate.setDayCellFactory(UIUtil.datePickerCallBack());
		timesheetDate.setValue(LocalDate.now());
				
		actGrpCombo.setCellFactory(cell -> UIUtil.getComboCellfactory());
		actGrpCombo.setConverter(UIUtil.getComboConverter());
		
		IdNameHolder selectedProject = projectCombo.getSelectionModel().getSelectedItem();
		if(null != selectedProject) {
			actGrpCombo.getItems().addAll(StemsAPIConnector.getActivityGroupsForProject(selectedProject.getId(), stemsCfg));
		}
		
		activityCombo.setCellFactory(cell -> UIUtil.getComboCellfactory());
		activityCombo.setConverter(UIUtil.getComboConverter());		
		
		setupTimeValidators();				
		setupTaskComboBoxes();
	}
	
    private void prepareProjectListCombo() {
		if(!projectSvc.isRunning()) {
			prjPrgrsContainer.setVisible(true);
			prjPrgrsContainer.setManaged(true);
			projectSvc.start();
		}

		projectSvc.setOnSucceeded(e -> {
			prjPrgrsContainer.setVisible(false);
			prjPrgrsContainer.setManaged(false);
			projectSvc.reset();
        });
		
		projectSvc.setOnFailed(e -> {
			prjPrgrsContainer.setVisible(false);
			prjPrgrsContainer.setManaged(false);
			snackbar.show("Oh No! Something went wrong in fetching projects...", 3000);
			projectSvc.reset();
		});
	}

	private void setupTaskComboBoxes() {
		actGrpCombo.setOnAction(event -> {
		    IdNameHolder selectedGrp = actGrpCombo.getSelectionModel().getSelectedItem();		    
		    if(null != selectedGrp) {
		    	try {
		    		activityCombo.getItems().clear();
//					List<IdNameHolder> activities = StemsAPIConnector.getActivitiesForGroup(selectedGrp.getId(), stemsCfg);
//					activityCombo.getItems().addAll(activities);
					
					
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
					
					
					
					
				} catch (Exception e) {
					snackbar.show("Unable to get Activity List. " + e.getLocalizedMessage(), 3000);
				}
		    }
		});	
		
		activityCombo.setOnAction(event -> {
			IdNameHolder selectedGrp = actGrpCombo.getSelectionModel().getSelectedItem();
			IdNameHolder selectedActivity = activityCombo.getSelectionModel().getSelectedItem();
		    if(null != selectedGrp && null != selectedActivity && (Constants.TRAVEL_GRP.equals(selectedGrp.getId()) || Constants.LEAVE_GRP.equals(selectedGrp.getId()))) {
		    	minutesTxt.setText(StemsUtil.getAutoMinutesForActivity(selectedActivity.getId()));
		    	hoursTxt.setText(StemsUtil.getAutoHoursForActivity(selectedActivity.getId()));	
		    }
		});
	}

	private void setupTimeValidators() {
		hoursTxt.focusedProperty().addListener((arg0, oldValue, newValue) -> {
			if (!newValue) {
				String hours = hoursTxt.getText();

				if (StringUtils.isNotBlank(hours)) {
					hours = StringUtils.length(hours) == 1 ? StringUtils.leftPad(hours, 2, "0") : hours;
					if (!hours.matches("0[0-9]")) {
						hoursTxt.setText(StringUtils.EMPTY);
						snackbar.show("Invalid Value for Hours", 2000);
					} else
						hoursTxt.setText(hours);
				}
			}
		});
		
		minutesTxt.focusedProperty().addListener((arg0, oldValue, newValue) -> {
			if (!newValue) {
				String minutes = minutesTxt.getText();

				if (StringUtils.isNotBlank(minutes)) {
					minutes = StringUtils.length(minutes) == 1 ? StringUtils.leftPad(minutes, 2, "0") : minutes;
					if (!minutes.matches("[0-5][0-9]")) {
						minutesTxt.setText(StringUtils.EMPTY);
						snackbar.show("Invalid Value for Minutes", 2000);
					} else
						minutesTxt.setText(minutes);
				}
			}
		});
	}

	private void prepareTaskEntryTableView() {
		UIUtil.setupCellValueFactory(colProject, TaskEntry::getProject);
		UIUtil.setupCellValueFactory(colDate, TaskEntry::getDate);
		UIUtil.setupCellValueFactory(colGroup, TaskEntry::getGroup);
		UIUtil.setupCellValueFactory(colActivity, TaskEntry::getActivity);
		UIUtil.setupCellValueFactory(colDuration, TaskEntry::getDuration);
		UIUtil.setupCellValueFactory(colRemarks, TaskEntry::getRemarks);

		tasksTableView.setRoot(new RecursiveTreeItem<>(taskList, RecursiveTreeObject::getChildren));
		tasksTableView.setShowRoot(false);

		addEntryBtn.setOnMouseClicked(event -> {
			if(validateTaskEntry()) {
				TaskEntry task = new TaskEntry();
				task.setTsProject(projectCombo.getValue());
				task.setTsDate(timesheetDate.getValue());
	    		task.setTsActyGrp(actGrpCombo.getValue());
	    		task.setTsActivity(activityCombo.getValue());
	    		task.setTsHours(hoursTxt.getText());
	    		task.setTsMinutes(minutesTxt.getText());
	    		task.setTsRemarks(detailsTxtArea.getText());
	    		
	    		taskList.add(task);
			}
		});

		delEntryBtn.disableProperty().bind(Bindings.isEmpty(tasksTableView.getSelectionModel().getSelectedItems()));
		duplicateBtn.disableProperty().bind(Bindings.isEmpty(tasksTableView.getSelectionModel().getSelectedItems()));

		delEntryBtn.setOnMouseClicked(event -> {
			taskList.remove(tasksTableView.getSelectionModel().selectedItemProperty().get().getValue());
			if (taskList.isEmpty())
				tasksTableView.getSelectionModel().clearSelection();
		});
		
		duplicateBtn.setOnMouseClicked(event -> {
			TaskEntry selectedEntry = tasksTableView.getSelectionModel().selectedItemProperty().get().getValue();
			try {
				taskList.add((TaskEntry) selectedEntry.clone());
			} catch (CloneNotSupportedException e) {
				snackbar.show("Hmmm... Failed to duplicate task.", 2500);
			}
		});
		
		resetFormBtn.setOnMouseClicked(event -> {
			projectCombo.setValue(null);
			timesheetDate.setValue(null);
			actGrpCombo.setValue(null);
			activityCombo.setValue(null);
			hoursTxt.setText(null);
			minutesTxt.setText(null);
			detailsTxtArea.setText(null);
		});
	}
    
    private boolean validateTaskEntry() {
    	
    	if(null == projectCombo.getValue()) {
    		snackbar.show("Hmmm... You missed the project.", 2500);
    		return false;
    	}
    	
    	if(null == timesheetDate.getValue()) {
    		snackbar.show("Hmmm... You should choose timesheet Date.", 2500);
    		return false;
    	}
    	
    	if(null == actGrpCombo.getValue()) {
    		snackbar.show("Hmmm...You missed Activity Group.", 2500);
    		return false;
    	}
    	
    	if(null == activityCombo.getValue()) {
    		snackbar.show("Hmmm...You missed Activity.", 2500);
    		return false;
    	}
    	
    	boolean isBlankHrs = StringUtils.isBlank(hoursTxt.getText());
    	boolean isBlankMins = StringUtils.isBlank(minutesTxt.getText());
    	boolean isZeroHrs = StringUtils.equalsIgnoreCase(Constants.STR_ZERO, hoursTxt.getText());
    	boolean isZeroMins = StringUtils.equalsIgnoreCase(Constants.STR_ZERO, minutesTxt.getText());
    	
    	if(isBlankHrs) {
    		snackbar.show("Hmmm...You missed Task Duration Hours.", 2500);
    		return false;
    	}
    	
    	if(isBlankMins) {
    		snackbar.show("Hmmm...You missed Task Duration Minutes.", 2500);
    		return false;
    	}
    	
    	String actvtId = activityCombo.getValue().getId();
    	if(isZeroHrs && isZeroMins && !(Constants.HOLIDAY.equalsIgnoreCase(actvtId) || Constants.COMP_OFF.equalsIgnoreCase(actvtId))) {
    		snackbar.show("Hmmm... You have a Task with Zero Duration.", 2500);
    		return false;
    	}
    	
    	if(StringUtils.isBlank(detailsTxtArea.getText())) {
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
                	responseMsgs = StemsAPIConnector.submitTasks(stemsCfg, taskList);
                    return null;
                }
            };
        }
    }
	
	class ProjectListService extends Service<Void> {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                	List<IdNameHolder> projects = StemsAPIConnector.getAllocatedProjects(stemsCfg);
                	projectCombo.getItems().addAll(projects);
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
                	List<IdNameHolder> activities = StemsAPIConnector.getActivitiesForGroup(actGrpCombo.getSelectionModel().getSelectedItem().getId(), stemsCfg);
                	activityCombo.getItems().addAll(activities);

                    return null;
                }
            };
        }
    }

}



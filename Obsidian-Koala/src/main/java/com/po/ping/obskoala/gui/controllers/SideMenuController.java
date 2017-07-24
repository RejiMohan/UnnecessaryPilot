package com.po.ping.obskoala.gui.controllers;

import com.jfoenix.controls.JFXListView;

import io.datafx.controller.ViewController;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.FlowHandler;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import io.datafx.controller.util.VetoException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@ViewController(value = "/fxml/SideMenu.fxml", title = "Material Design Example")
public class SideMenuController {

    @FXMLViewFlowContext private ViewFlowContext context;
    
    @FXML
    @ActionTrigger("taskEntryTrigger")
    private Label taskEntryTrigger;
    
    @FXML
    @ActionTrigger("taskHistoryTrigger")
    private Label taskHistoryTrigger;
    
    @FXML
    @ActionTrigger("bulkEntryTrigger")
    private Label bulkEntryTrigger;
    
    @FXML private JFXListView<Label> sideList;
    
    private static final Logger LOGGER = Logger.getLogger(SideMenuController.class.getName());


    @PostConstruct
    public void init() {
        Objects.requireNonNull(context, "context");
        FlowHandler contentFlowHandler = (FlowHandler) context.getRegisteredObject("ContentFlowHandler");
        sideList.propagateMouseEventsToParent();
        sideList.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    contentFlowHandler.handle(newVal.getId());
                } catch (VetoException | FlowException exc) {
                	LOGGER.log(Level.SEVERE, "Something Went Wrong", exc);
                }
            }
        });
        Flow contentFlow = (Flow) context.getRegisteredObject("ContentFlow");

        bindNodeToController(taskEntryTrigger, TaskEntryPageController.class, contentFlow);
        bindNodeToController(taskHistoryTrigger, TaskHistoryPageController.class, contentFlow);
        bindNodeToController(bulkEntryTrigger, BulkEntryPageController.class, contentFlow);
    }

    private void bindNodeToController(Node node, Class<?> controllerClass, Flow flow) {
        flow.withGlobalLink(node.getId(), controllerClass);
    }

}

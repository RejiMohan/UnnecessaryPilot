package com.po.ping.obskoala;

import com.jfoenix.controls.JFXDecorator;
import com.po.ping.obskoala.datum.BuddyCfgHandle;
import com.po.ping.obskoala.gui.controllers.BaseController;
import com.po.ping.obskoala.utils.StemsUtil;

import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.container.DefaultFlowContainer;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class BaseApp extends Application {

    @FXMLViewFlowContext
    private ViewFlowContext flowContext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
    
    	Flow flow = new Flow(BaseController.class);
        DefaultFlowContainer container = new DefaultFlowContainer();
        flowContext = new ViewFlowContext();
        flowContext.register("Stage", stage);
        flow.createHandler(flowContext).start(container);

        JFXDecorator decorator = new JFXDecorator(stage, container.getView());
        decorator.setCustomMaximize(true);
        Scene scene = new Scene(decorator, 600, 650);
        final ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.add(BaseApp.class.getResource("/css/custom-styles.css").toExternalForm());
        stage.setMinWidth(600);
        stage.setMinHeight(800);
        stage.setScene(scene);        
        stage.getIcons().add(new Image(BaseApp.class.getResourceAsStream("/icons/icon.png")));
        
        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) { 
				try {
					if(!StemsUtil.isValidData(BuddyCfgHandle.getInstance().getMetaData()))
						showAlert();						
				} catch (Exception e) {					
					showAlert();
				}
			}

			private void showAlert() {
				Alert alert = new Alert(AlertType.NONE);
				alert.setHeaderText("Oops! You broke it.");
				alert.setContentText("The config file doesn't look good... \nEnsure that it's configured and placed properly.");
				alert.initStyle(StageStyle.UNDECORATED);
				alert.initModality(Modality.APPLICATION_MODAL);
				alert.getButtonTypes().add(ButtonType.OK);
				
				DialogPane dialogPane = alert.getDialogPane();
				dialogPane.getStylesheets().add(BaseApp.class.getResource("/css/custom-styles.css").toExternalForm());
				
				alert.showAndWait()
				  .filter(response -> response == ButtonType.OK)
				  .ifPresent(response -> Platform.exit());
			}
		});
        
        stage.show();             
    }

}

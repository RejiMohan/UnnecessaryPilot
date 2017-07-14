package com.po.ping.obskoala.gui.controllers;

import com.jfoenix.controls.*;
import com.po.ping.obskoala.datafx.ExtendedAnimatedFlowContainer;
import com.po.ping.obskoala.exceptions.StemsCustException;

import io.datafx.controller.ViewController;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.FlowHandler;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import javax.annotation.PostConstruct;

import static io.datafx.controller.flow.container.ContainerAnimations.SWIPE_LEFT;

@ViewController(value = "/fxml/Main.fxml", title = "Home Page")
public final class BaseController {

    @FXMLViewFlowContext private ViewFlowContext context;
    @FXML private StackPane root;
    @FXML private StackPane titleBurgerContainer;
    @FXML private JFXHamburger titleBurger;
    @FXML private JFXDrawer drawer;


    @PostConstruct
    public void init() throws StemsCustException {
    	    	
        // init the title hamburger icon
        drawer.setOnDrawerOpening(e -> {
            final Transition animation = titleBurger.getAnimation();
            animation.setRate(1);
            animation.play();
        });
        drawer.setOnDrawerClosing(e -> {
            final Transition animation = titleBurger.getAnimation();
            animation.setRate(-1);
            animation.play();
        });
        titleBurgerContainer.setOnMouseClicked(e -> {
            if (drawer.isHidden() || drawer.isHiding()) {
                drawer.open();
            } else {
                drawer.close();
            }
        });

        try {
			context = new ViewFlowContext();
			Flow innerFlow = new Flow(TaskEntryPageController.class);

			final FlowHandler flowHandler = innerFlow.createHandler(context);
			context.register("ContentFlowHandler", flowHandler);
			context.register("ContentFlow", innerFlow);
			final Duration containerAnimationDuration = Duration.millis(320);
			drawer.setContent(flowHandler.start(new ExtendedAnimatedFlowContainer(containerAnimationDuration, SWIPE_LEFT)));
			context.register("ContentPane", drawer.getContent().get(0));

			// side controller will add links to the content flow
			Flow sideMenuFlow = new Flow(SideMenuController.class);
			final FlowHandler sideMenuFlowHandler = sideMenuFlow.createHandler(context);
			drawer.setSidePane(sideMenuFlowHandler.start(new ExtendedAnimatedFlowContainer(containerAnimationDuration,
			                                                                               SWIPE_LEFT)));
		} catch (FlowException ex) {
			throw new StemsCustException("Something went Wrong.", ex);
		}
    }
    
}

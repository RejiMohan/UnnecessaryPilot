package com.po.ping.obskoala.datum;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TaskEntry extends RecursiveTreeObject<TaskEntry> {
	private LocalDate tsDate;
	private IdNameHolder tsProject;
	private IdNameHolder tsActyGrp;
	private IdNameHolder tsActivity;
	private String tsHours;
	private String tsMinutes;
	private String tsRemarks;

	public LocalDate getTsDate() {
		return tsDate;
	}

	public void setTsDate(LocalDate tsDate) {
		this.tsDate = tsDate;
	}

	public IdNameHolder getTsProject() {
		return tsProject;
	}

	public void setTsProject(IdNameHolder tsProject) {
		this.tsProject = tsProject;
	}

	public IdNameHolder getTsActyGrp() {
		return tsActyGrp;
	}

	public void setTsActyGrp(IdNameHolder tsActyGrp) {
		this.tsActyGrp = tsActyGrp;
	}

	public IdNameHolder getTsActivity() {
		return tsActivity;
	}

	public void setTsActivity(IdNameHolder tsActivity) {
		this.tsActivity = tsActivity;
	}

	public String getTsHours() {
		return tsHours;
	}

	public void setTsHours(String tsHours) {
		this.tsHours = tsHours;
	}

	public String getTsMinutes() {
		return tsMinutes;
	}

	public void setTsMinutes(String tsMinutes) {
		this.tsMinutes = tsMinutes;
	}

	public String getTsRemarks() {
		return tsRemarks;
	}

	public void setTsRemarks(String tsRemarks) {
		this.tsRemarks = tsRemarks;
	}

	// For UI Table
	public StringProperty getProject() {
		return new SimpleStringProperty(this.tsProject.getName());
	}

	public StringProperty getDate() {
		return new SimpleStringProperty(this.tsDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")));
	}
	
	public StringProperty getGroup() {
		return new SimpleStringProperty(this.tsActyGrp.getName());
	}

	public StringProperty getActivity() {
		return new SimpleStringProperty(this.tsActivity.getName());
	}

	public StringProperty getRemarks() {
		return new SimpleStringProperty(this.tsRemarks);
	}

	public StringProperty getDuration() {
		return new SimpleStringProperty(String.join(":", this.tsHours, this.tsMinutes));
	}

}

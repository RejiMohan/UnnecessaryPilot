package com.po.ping.obskoala.datum;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TaskEntry extends RecursiveTreeObject<TaskEntry> {
	private IdNameHolder tsActyGrp;
	private IdNameHolder tsActivity;
	private String tsHours;
	private String tsMinutes;
	private String tsRemarks;

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

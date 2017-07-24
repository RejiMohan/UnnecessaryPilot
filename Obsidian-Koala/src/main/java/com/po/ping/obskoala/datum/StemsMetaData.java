package com.po.ping.obskoala.datum;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StemsMetaData {

@SerializedName("empId")
@Expose
private String empId;
@SerializedName("username")
@Expose
private String username;
@SerializedName("password")
@Expose
private String password;
@SerializedName("projects")
@Expose
private List<IdNameHolder> projects = null;
@SerializedName("activityGroups")
@Expose
private List<IdNameHolder> activityGroups = null;

public String getEmpId() {
return empId;
}

public void setEmpId(String empId) {
this.empId = empId;
}

public String getUsername() {
return username;
}

public void setUsername(String username) {
this.username = username;
}

public String getPassword() {
return password;
}

public void setPassword(String password) {
this.password = password;
}

public List<IdNameHolder> getProjects() {
return projects;
}

public void setProjects(List<IdNameHolder> projects) {
this.projects = projects;
}

public List<IdNameHolder> getActivityGroups() {
return activityGroups;
}

public void setActivityGroups(List<IdNameHolder> activityGroups) {
this.activityGroups = activityGroups;
}

}
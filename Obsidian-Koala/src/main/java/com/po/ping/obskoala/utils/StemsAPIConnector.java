package com.po.ping.obskoala.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.po.ping.obskoala.datum.BuddyCfgHandle;
import com.po.ping.obskoala.datum.IdNameHolder;
import com.po.ping.obskoala.datum.StemsMetaData;
import com.po.ping.obskoala.datum.TaskEntry;
import com.po.ping.obskoala.exceptions.StemsCustException;

import javafx.collections.ObservableList;

import static com.po.ping.obskoala.utils.Constants.CACHE_CTRL;
import static com.po.ping.obskoala.utils.Constants.NO_CACHE;
import static com.po.ping.obskoala.utils.Constants.COOKIE;

public class StemsAPIConnector {
	
	private StemsAPIConnector() {}

	private static String cookie;
	private static List<IdNameHolder> projectsList;
	
	public static void doStemsLogin(StemsMetaData stemsData) throws StemsCustException {
		try {
			HttpResponse<String> response = Unirest.get(StemsUtil.buildLoginUrl(stemsData))
					.header(CACHE_CTRL, NO_CACHE).asString();
			if (response.getStatus() == 200 && "Success".equalsIgnoreCase(response.getBody().trim())) {
				List<String> cookies = response.getHeaders().get("Set-Cookie");
				cookie = null != cookies && !cookies.isEmpty() ? cookies.get(0).split(";\\s*")[0] : null;
			} else
				throw new StemsCustException("STEMS Login Failed!");

			// Navigate pages
			Unirest.get(StemsUtil.buildLandingUrl(stemsData)).header(CACHE_CTRL, NO_CACHE)
					.header(COOKIE, cookie).asString();

			response = Unirest.get(StemsUtil.buildTimeSheetEntryUrl(stemsData)).header(CACHE_CTRL, NO_CACHE)
					.header(COOKIE, cookie).asString();
			
			parseAllocatedProjectsForUser(response);


		} catch (UnirestException ex) {
			throw new StemsCustException("Something Went Wrong!!!", ex);
		}
	}
	
	private static void parseAllocatedProjectsForUser(HttpResponse<String> response) {
		System.out.println(response);
		Document document = Jsoup.parse(response.getBody(), Charsets.UTF_8.name());
		Elements projects = document.select("select[name=projectId] > option");
		projectsList = new ArrayList<>();
		
		projects.stream().skip(1).forEach(option -> {
			if(null != option) {
				IdNameHolder projectIdName = new IdNameHolder();
				projectIdName.setId(option.attr("value"));
				projectIdName.setName(option.text());
				projectsList.add(projectIdName);
			}
		});
	}
	
	public static List<IdNameHolder> getAllocatedProjects(StemsMetaData stemsData) throws StemsCustException {
		if(null == projectsList || projectsList.isEmpty()) {
			doStemsLogin(stemsData);
		}
		
		return projectsList;
	}

	public static String getTimeSheetEntryForTheDay(LocalDate date) throws StemsCustException {

		try {
			StemsMetaData stemsData = BuddyCfgHandle.getInstance().getMetaData();
			if(isSessionExpired(stemsData))
				doStemsLogin(stemsData);
			
			String queryDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			String formData = ""; //TODO Add Formdata

			HttpResponse<String> response = Unirest
					.post("url") //TODO Add url
					.header("content-type", "application/x-www-form-urlencoded").header(CACHE_CTRL, NO_CACHE)
					.header(COOKIE, cookie).body(formData).asString();
			if (response.getStatus() == 200) {
				Document document = Jsoup.parse(response.getBody(), Charsets.UTF_8.name());
				if (null != document) {
					if(null != document.select(Constants.ERR_DIV_SELECTOR) && StringUtils.isNotBlank(document.select(Constants.ERR_DIV_SELECTOR).first().text()))
						return "00 : 00";
					else if (null != document.select(Constants.TOTAL_SELECTOR)){
						return document.select(Constants.TOTAL_SELECTOR).first().text();						
					}
				}					
			}

		} catch (DateTimeException | UnirestException ex) {
			throw new StemsCustException("Failed to check timesheet deails on " + date, ex );
		}
		
		return Constants.UNKNOWN;
	}
	
	
	private static boolean isSessionExpired(StemsMetaData stemsData) throws StemsCustException {
		try {
			HttpResponse<String> response = Unirest.get(StemsUtil.buildTimeSheetEntryUrl(stemsData)).header(CACHE_CTRL, NO_CACHE)
			.header(COOKIE, cookie).asString();
			
			return response.getStatus() != 200 || StringUtils.containsIgnoreCase(response.getBody(), "Your Session has been Expired");
			
		} catch (UnirestException | StemsCustException e) {
			throw new StemsCustException("Session Check Failed", e); 
		}
	}

	public static List<IdNameHolder> getActivitiesForGroup(String groupId, StemsMetaData stemsData) throws StemsCustException {
				
		if(isSessionExpired(stemsData))
			doStemsLogin(stemsData);
		
		try {
			if(StringUtils.isNotBlank(groupId)) {
				String queryParams = ""; //TODO
				String requestUrl =  URLEncoder.encode("URL", Charsets.UTF_8.name()); //TODO ADD URL
				
				HttpResponse<String> response = Unirest.get(requestUrl)
						.header(COOKIE, cookie)
						.header(CACHE_CTRL, NO_CACHE).asString();
				
				if (response.getStatus() == 200) {
					return StemsUtil.extractNameIdPairsFromResponse(response.getBody());
				}  
			}
			throw new StemsCustException("Failed to retrieve activities.");
		} catch (UnsupportedEncodingException | UnirestException ex) {
			throw new StemsCustException("Failed to retrieve activities.", ex);
		}
	}

	public static List<String> submitTasks(StemsMetaData stemsData, String projectId, LocalDate taskDate, List<TaskEntry> taskList, int daysTobeRepeated) throws StemsCustException {
		List<String> errorMessages = new ArrayList<>();
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
		int daysCounter = daysTobeRepeated;
		
		if(isSessionExpired(stemsData))
			doStemsLogin(stemsData);
		
		for (TaskEntry task : taskList) {			
			String result = updateTaskInStems(projectId, taskDate, task);
			if(StringUtils.isNotBlank(result))
				errorMessages.add("Failed to submit task '" + task.getTsRemarks() + "' on " + taskDate + ". Reason : '" + result + "'");
			
//			Repeat the same task if required
			while(daysCounter > 0) {
				LocalDate targetedDate = today.minusDays(daysCounter);				
				if(!targetedDate.equals(taskDate)) {
					result = updateTaskInStems(projectId, targetedDate, task);
					if(StringUtils.isNotBlank(result))
						errorMessages.add("Failed to submit task '" + task.getTsRemarks() + "' on " + targetedDate + ". Reason : '" + result + "'");
				}
				--daysCounter;				
			}
		}
		
		return errorMessages;
	}
	
	private static String updateTaskInStems(String projectId, LocalDate taskDate, TaskEntry task) {
		try {
			HttpResponse<String> response = Unirest
					.post("url") //TODO
					.header("content-type", "application/x-www-form-urlencoded").header(CACHE_CTRL, NO_CACHE)
					.header(COOKIE, cookie).body(StemsUtil.buildTimeSheet(projectId, taskDate, task)).asString();
			if (response.getStatus() == 200) {
				Document document = Jsoup.parse(response.getBody(), Charsets.UTF_8.name());
				if (null != document && null != document.select(Constants.ERR_DIV_SELECTOR)) {
					return document.select(Constants.ERR_DIV_SELECTOR).first().text();
				}
			} else {
				return "Server returned non success response code on submit.";
			}
		} catch (Exception e) {
			return e.getLocalizedMessage();
		}
		return null;
	}

	public static List<String> submitTasks(StemsMetaData stemsData, ObservableList<TaskEntry> taskList) throws StemsCustException {
		List<String> errorMessages = new ArrayList<>();
		
		if(isSessionExpired(stemsData))
			doStemsLogin(stemsData);
		
		for (TaskEntry task : taskList) {			
			String result = updateTaskInStems(task.getTsProject().getId(), task.getTsDate(), task);
			if(StringUtils.isNotBlank(result))
				errorMessages.add("Failed to submit task '" + task.getTsRemarks() + "' on " + task.getTsDate() + "; Reason : '" + result + "'");			
		}
		
		return errorMessages;
	}
}

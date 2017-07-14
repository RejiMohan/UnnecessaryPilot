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
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.po.ping.obskoala.datum.BuddyCfgHandle;
import com.po.ping.obskoala.datum.IdNameHolder;
import com.po.ping.obskoala.datum.StemsMetaData;
import com.po.ping.obskoala.datum.TaskEntry;
import com.po.ping.obskoala.exceptions.StemsCustException;

import static com.po.ping.obskoala.utils.Constants.CACHE_CTRL;
import static com.po.ping.obskoala.utils.Constants.NO_CACHE;
import static com.po.ping.obskoala.utils.Constants.COOKIE;

public class StemsAPIConnector {
	
	private StemsAPIConnector() {}

	private static String cookie;
	
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

			Unirest.get(StemsUtil.buildTimeSheetEntryUrl(stemsData)).header(CACHE_CTRL, NO_CACHE)
					.header(COOKIE, cookie).asString();


		} catch (UnirestException ex) {
			throw new StemsCustException("Something Went Wrong!!!", ex);
		}
	}
	
	public static String getTimeSheetEntryForTheDay(LocalDate date) throws StemsCustException {

		try {
			
			if(StringUtils.isBlank(cookie)) {
				StemsMetaData stemsData = BuddyCfgHandle.getInstance().getMetaData();
				doStemsLogin(stemsData);
			}
			
			String queryDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			String formData = ""; //TODO Add Formdata

			HttpResponse<String> response = Unirest
					.post("url") //TODO Add url
					.header("content-type", "application/x-www-form-urlencoded").header(CACHE_CTRL, NO_CACHE)
					.header(COOKIE, cookie).body(formData).asString();
			if (response.getStatus() == 200) {
				Document document = Jsoup.parse(response.getBody(), "UTF-8");
				if (null != document) {
					if(null != document.select(Constants.ERR_DIV_SELECTOR) && StringUtils.isNotBlank(document.select(Constants.ERR_DIV_SELECTOR).first().text()))
						return "00 : 00";
					else if (null != document.select(Constants.TOTAL_SELECTOR)){
						return document.select(Constants.TOTAL_SELECTOR).first().text();						
					}
				}					
			}

		} catch (DateTimeException | UnirestException | IOException ex) {
			throw new StemsCustException("Failed to check deails on " + date, ex );
		}
		
		return Constants.UNKNOWN;
	}
	
	
	public static List<IdNameHolder> getActivitiesForGroup(String groupId, StemsMetaData stemsData) throws StemsCustException {
				
		if(StringUtils.isBlank(cookie))
			doStemsLogin(stemsData);
		
		try {
			if(StringUtils.isNotBlank(groupId)) {
				String queryParams = ""; //TODO
				String requestUrl =  ""; //TODO
				
				HttpResponse<String> response = Unirest.get(requestUrl)
						.header(COOKIE, cookie)
						.header(CACHE_CTRL, NO_CACHE).asString();
				
				if (response.getStatus() == 200) {
					return StemsUtil.extractNameIdPairsFromResponse(response.getBody());
					
				}	
			}
			throw new StemsCustException("Failed to retrieve activities.");
		} catch (UnsupportedEncodingException |UnirestException ex) {
			throw new StemsCustException("Failed to retrieve activities.", ex);
		}
	}

	public static List<String> submitTasks(StemsMetaData stemsData, String projectId, LocalDate taskDate, List<TaskEntry> taskList, int daysTobeRepeated) throws StemsCustException {
		List<String> errorMessages = new ArrayList<>();
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
		int daysCounter = daysTobeRepeated;
		
		if(StringUtils.isBlank(cookie))
			doStemsLogin(stemsData);
		
		for (TaskEntry task : taskList) {			
			String result = updateTaskInStems(projectId, taskDate, task);
			if(StringUtils.isNotBlank(result))
				errorMessages.add("Failed '" + task.getTsRemarks() + "' on " + taskDate + ". Reason : '" + result + "'");
			
//			Repeat the same task if required
			while(daysCounter > 0) {
				LocalDate targetedDate = today.minusDays(daysCounter);				
				if(!targetedDate.equals(taskDate)) {
					result = updateTaskInStems(projectId, targetedDate, task);
					if(StringUtils.isNotBlank(result))
						errorMessages.add("Failed '" + task.getTsRemarks() + "' on " + targetedDate + ". Reason : '" + result + "'");
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
				Document document = Jsoup.parse(response.getBody(), "UTF-8");
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
}

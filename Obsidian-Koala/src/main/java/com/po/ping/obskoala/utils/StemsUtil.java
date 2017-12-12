package com.po.ping.obskoala.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;

import com.po.ping.obskoala.datum.IdNameHolder;
import com.po.ping.obskoala.datum.StemsMetaData;
import com.po.ping.obskoala.datum.TaskEntry;
import com.po.ping.obskoala.exceptions.StemsCustException;

public class StemsUtil {
	
	private static final String USERNAMEKEY = "userName=";
	
	private StemsUtil() {}

	public static boolean isValidData(StemsMetaData stemsCfg) {

		return null != stemsCfg && StringUtils.isNotBlank(stemsCfg.getEmpId())
				&& StringUtils.isNotBlank(stemsCfg.getUsername()) && StringUtils.isNotBlank(stemsCfg.getPassword())
				&& null != stemsCfg.getProjects() && null != stemsCfg.getActivityGroups()
				&& !stemsCfg.getProjects().isEmpty() && !stemsCfg.getActivityGroups().isEmpty();
	}

	public static String buildTimeSheet(String projectId, LocalDate taskDate, TaskEntry task) throws StemsCustException {

		try {
			String timeSheetDate = taskDate.format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
			
			return URLEncoder.encode("BODY", Charsets.UTF_8.name()); //TODO Build Body

		} catch (UnsupportedEncodingException uee) {
			throw new StemsCustException("Failed to parse Entry. ", uee);
		}
	}

	public static String buildLoginUrl(StemsMetaData stemsData) throws StemsCustException {

		try {

			return URLEncoder.encode("URL", Charsets.UTF_8.name()); //TODO Build URL
		} catch (UnsupportedEncodingException e) {
			throw new StemsCustException("Failed to build Login URL");
		}
	}

	public static String buildLandingUrl(StemsMetaData stemsData) throws StemsCustException {
		try {
			return URLEncoder.encode("URL", Charsets.UTF_8.name()); //TODO Build URL
		} catch (UnsupportedEncodingException e) {
			throw new StemsCustException("Failed to build Landing page URL");
		}
	}

	public static String buildTimeSheetEntryUrl(StemsMetaData stemsData) throws StemsCustException {
		try {
			
			return URLEncoder.encode("URL", Charsets.UTF_8.name()); //TODO Build URL
		} catch (UnsupportedEncodingException e) {
			throw new StemsCustException("Failed to build TS Entry page URL");
		}
	}

	public static List<IdNameHolder> extractNameIdPairsFromResponse(String body) {
		List<IdNameHolder> idNamePairsList = new ArrayList<>();

		if (StringUtils.isNotBlank(body) && (body.contains(Constants.ITEM_SPLITTER) || body.contains(Constants.PAIR_SPLITTER))) {
			List<String> pairs = Arrays.asList(body.split(Constants.ITEM_SPLITTER));

			pairs.forEach(pair -> {
				if (!pair.startsWith(Constants.DUMMY_ITEM) && pair.contains(Constants.PAIR_SPLITTER)) {
					String[] nameValue = pair.split(Constants.PAIR_SPLITTER_REGEX);
					IdNameHolder holder = new IdNameHolder();
					holder.setId(nameValue[0].trim());
					holder.setName(nameValue[1].trim());
					idNamePairsList.add(holder);
				}
			});
		}
		return idNamePairsList;
	}
	
	public static String getAutoHoursForActivity(String activityId) {
		if(StringUtils.isNotBlank(activityId)) {
			switch (activityId) {
			case Constants.TRAVEL_FULL: case Constants.CASUAL_FULL: case Constants.PRVLG_FULL: case Constants.LOP_FULL: return Constants.HRS_FULL;
			case Constants.TRAVEL_HALF: case Constants.CASUAL_HALF: case Constants.PRVLG_HALF: case Constants.LOP_HALF: return Constants.HRS_HALF;
			case Constants.COMP_OFF: case Constants.HOLIDAY: return Constants.STR_ZERO;
			default: return StringUtils.EMPTY;
			}
		}
		return StringUtils.EMPTY;
	}

	public static String getAutoMinutesForActivity(String activityId) {
		if(StringUtils.isNotBlank(activityId)) {
			switch (activityId) {
			case Constants.TRAVEL_FULL: case Constants.CASUAL_FULL: case Constants.PRVLG_FULL: case Constants.LOP_FULL: 
			case Constants.TRAVEL_HALF: case Constants.CASUAL_HALF: case Constants.PRVLG_HALF: case Constants.LOP_HALF: 
			case Constants.COMP_OFF: case Constants.HOLIDAY: return Constants.STR_ZERO;
			default: return StringUtils.EMPTY;
			}			
		}
		return StringUtils.EMPTY;
	}
}

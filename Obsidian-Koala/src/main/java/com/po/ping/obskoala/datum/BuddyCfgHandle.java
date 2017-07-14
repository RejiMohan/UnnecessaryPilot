package com.po.ping.obskoala.datum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.po.ping.obskoala.exceptions.StemsCustException;
import com.po.ping.obskoala.utils.StemsUtil;

public final class BuddyCfgHandle {

	private static BuddyCfgHandle instance;
	private StemsMetaData stemsCfg;

	private BuddyCfgHandle() throws StemsCustException {
        File rootPath = new File(StemsUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath());

        try {
        	String inputFilePath = rootPath.getParent() + "/config.json"; 
        	stemsCfg = new Gson().fromJson(new FileReader(inputFilePath), StemsMetaData.class);
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException ex) {
			throw new StemsCustException("Failed to read/Parse Config Data.", ex);
		}
	}

	public static synchronized BuddyCfgHandle getInstance() throws StemsCustException {
		if (instance == null) {
			instance = new BuddyCfgHandle();
		}
		return instance;
	}
	
	public StemsMetaData getMetaData() {
		return this.stemsCfg;
		
	}

}

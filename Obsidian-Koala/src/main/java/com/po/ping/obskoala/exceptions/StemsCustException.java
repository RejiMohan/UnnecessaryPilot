package com.po.ping.obskoala.exceptions;

public class StemsCustException extends Exception {

	private static final long serialVersionUID = 4159806600152011377L;
	
	public StemsCustException(String message, Throwable cause) {
		super(message, cause);
	}

	public StemsCustException(String message) {
		super(message);
	}

}

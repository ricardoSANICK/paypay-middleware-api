package com.paypay.baymax.front.controller;

public interface VueEndPoints {
	
	public final String API						= "";
	public final String HOME					= "/home";
	public final String AUTH					= "/auth";
	public final String EXPIRED_PWD				= "/expiredpwd";
	public final String RESET_PWD				= "/resetpwd";
	public final String FORGET_PWD				= "/forgetpwd";
	public final String SESSION_EXPIRED_MSG		= "/sessionexpiredmessage";
	
	public final String AUTH_EXPIRED_PWD		= AUTH+"/expiredpwd";
	public final String AUTH_RESET_PWD			= AUTH+"/resetpwd";
	public final String AUTH_FORGET_PWD			= AUTH+"/forgetpwd";
	public final String AUTH_SESSION_EXPIRED_MSG= AUTH+"/sessionexpiredmessage";
	
	
	public final String API_COMBOS				= API+"/combos";

}

package com.notification.common;

import com.notification.common.exception.ApplicationException;

//@Service("basicAuthenticationService")
public interface BasicAuthenticationService
{
	public boolean authenticate(String authCredentials) throws ApplicationException;
}


package com.notification.common.dto;

public enum MPResponseStatus
{
	SUCCESS(true), FAILURE(false);
	
	private boolean value;
	
	private MPResponseStatus(boolean value)
	{
		this.value = value;
	}
};

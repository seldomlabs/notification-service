
package com.notification.common.dto;

/**
 * Basic request class for all Authenticated requests.
 * Contains a session token that is validated
 * @author raunak
 *
 */
public class MPRequest
{
	
	private Long id;
	
	private String token;
	
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getToken()
	{
		return token;
	}
	
	public void setToken(String token)
	{
		this.token = token;
	}
	
}

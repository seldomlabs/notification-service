
package com.notification.common.exception;

/**
 *
 * @author vishalaksh
 */
public class BadRequestException extends RuntimeException
{

	/*
	 * Please do not define constants here. Use ExceptionConstants so that they can be shared by one and all. Keep this class as light as possible
	 * with minimal logic
	 */

	private static final long serialVersionUID = 1646005858397483908L;

	/*
	 * Code 1 - 4998 - Fatal [Last used code: 10] Code 4999 - Unknown fatal exception used by DataAccess layer Code 5000 - 9999 - Application errors
	 * [Last used code: 5057]
	 */
	private int code = ExceptionConstants.CODE_UNDEFINED;

	public BadRequestException(String message)
	{
		super(message);
	}

	public BadRequestException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public BadRequestException(int code, String message)
	{
		super(message);
		this.code = code;
	}

	public BadRequestException(int code, Throwable cause)
	{
		super(cause);
		this.code = code;
	}

	public BadRequestException(int code, String message, Throwable cause)
	{
		super(message, cause);
		this.code = code;
	}
	
	public boolean isFatal()
	{
		return (this.code != ExceptionConstants.CODE_UNDEFINED && (this.code > 1 && this.code < 5000));
	}
	
	public int getErrorCode()
	{
		return this.code;
	}
}

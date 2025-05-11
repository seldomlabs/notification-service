
package com.notification.common.exception;

public class NotFoundException extends Exception
{

    private static final long serialVersionUID = 1126005857397483602L;

    private int code = ExceptionConstants.CODE_UNDEFINED;

    public NotFoundException(String message)
    {
        super(message);
    }

    public NotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NotFoundException(int code, String message)
    {
        super(message);
        this.code = code;
    }

    public NotFoundException(int code, Throwable cause)
    {
        super(cause);
        this.code = code;
    }

    public NotFoundException(int code, String message, Throwable cause)
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

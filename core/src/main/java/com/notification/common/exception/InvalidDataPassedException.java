
package com.notification.common.exception;

public class InvalidDataPassedException extends Exception
{

    private static final long serialVersionUID = 1126005857397483602L;

    private int code = ExceptionConstants.CODE_UNDEFINED;

    public InvalidDataPassedException(String message)
    {
        super(message);
    }

    public InvalidDataPassedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InvalidDataPassedException(int code, String message)
    {
        super(message);
        this.code = code;
    }

    public InvalidDataPassedException(int code, Throwable cause)
    {
        super(cause);
        this.code = code;
    }

    public InvalidDataPassedException(int code, String message, Throwable cause)
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

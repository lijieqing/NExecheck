package com.kstech.nexecheck.exception;


public class ExcException extends RuntimeException{

	private static final long serialVersionUID = 5728665240847900976L;

	private Exception exception;
	private String errorMsg;
	/**
	 * @param e
	 * @param string
	 */
	public ExcException(Exception exception, String errorMsg) {
		this.exception = exception;
		this.errorMsg = errorMsg;
	}
	public Exception getException() {
		return exception;
	}
	public void setException(Exception exception) {
		this.exception = exception;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
}

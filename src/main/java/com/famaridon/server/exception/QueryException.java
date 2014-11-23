package com.famaridon.server.exception;

/**
 * Created by rolfone on 09/11/14.
 */
public class QueryException extends Exception {
	public QueryException() {
	}

	public QueryException(String message) {
		super(message);
	}

	public QueryException(String message, Throwable cause) {
		super(message, cause);
	}

	public QueryException(Throwable cause) {
		super(cause);
	}

	public QueryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

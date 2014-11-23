package com.famaridon.server.beans;

import java.io.Serializable;

/**
 * Created by rolfone on 11/11/14.
 */
public abstract class Query implements Serializable {

	protected final String project;

	protected Query(String project) {
		this.project = project;
	}

	public String getProject() {
		return project;
	}
}

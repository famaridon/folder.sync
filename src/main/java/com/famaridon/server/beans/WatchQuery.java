package com.famaridon.server.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rolfone on 09/11/14.
 */
public class WatchQuery extends Query{
	private final String projectPath;
	private final String vdocPath;
	private List<String> excludeList = new ArrayList<>();

	public WatchQuery(String project, String projectPath, String vdocPath) {
		super(project);
		this.projectPath = projectPath;
		this.vdocPath = vdocPath;
	}


	public String getProjectPath() {
		return projectPath;
	}

	public String getVdocPath() {
		return vdocPath;
	}

	public List<String> getExcludeList() {
		return excludeList;
	}

	public boolean addExclude(String exclude) {
		return this.excludeList.add(exclude);
	}
}

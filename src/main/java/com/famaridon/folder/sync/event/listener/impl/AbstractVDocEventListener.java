package com.famaridon.folder.sync.event.listener.impl;

import com.famaridon.folder.sync.event.listener.FolderEventListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Created by rolfone on 07/11/14.
 */
public abstract class AbstractVDocEventListener implements FolderEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractVDocEventListener.class);

	protected final Path watchedFolder;
	protected final Path vdocHome;
	protected final Path ear;
	protected final Path war;
	protected final Path custom;

	public AbstractVDocEventListener(Path watchedFolder, Path vdocHome) {
		this.watchedFolder = watchedFolder;
		this.vdocHome = vdocHome;
		this.ear = vdocHome.resolve("JBoss/server/all/deploy/vdoc.ear/");
		this.war = ear.resolve("vdoc.war");
		this.custom = war.resolve("WEB-INF/storage/custom");
	}


	protected Path getTo(Path path) {
		if(FilenameUtils.wildcardMatch(FilenameUtils.separatorsToUnix(path.toString()),"*webapp/*")){
			return war.resolve(path.subpath(1,path.getNameCount()));
		}else{
			return custom.resolve(path);
		}
	}
}

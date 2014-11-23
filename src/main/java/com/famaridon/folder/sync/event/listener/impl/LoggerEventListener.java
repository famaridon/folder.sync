package com.famaridon.folder.sync.event.listener.impl;

import com.famaridon.folder.sync.event.listener.FolderEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Created by rolfone on 07/11/14.
 */
public class LoggerEventListener implements FolderEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerEventListener.class);

	@Override
	public void onCreate(Path path) {
		LOGGER.info("Created : {}", path);
	}

	@Override
	public void onDelete(Path path) {
		LOGGER.info("Delete : {}", path);
	}

	@Override
	public void onModify(Path path) {
		LOGGER.info("Modify : {}", path);
	}
}

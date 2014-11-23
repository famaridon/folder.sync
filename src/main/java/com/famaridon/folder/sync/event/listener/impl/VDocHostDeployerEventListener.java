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
public class VDocHostDeployerEventListener extends AbstractVDocEventListener implements FolderEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(VDocHostDeployerEventListener.class);

	public VDocHostDeployerEventListener(Path watchedFolder, Path vdocHome) {
		super(watchedFolder,vdocHome);
	}

	@Override
	public void onCreate(Path path) {
		try {
			Path from = watchedFolder.resolve(path);
			Path to = getTo(path);
			if (Files.isDirectory(from)) {
				LOGGER.debug("Create directory '{}'", to);
				FileUtils.forceMkdir(to.toFile());
			} else {
				LOGGER.debug("Create File '{}'", to);
				FileUtils.forceMkdir(to.getParent().toFile());
				Files.createFile(to);
			}
		} catch (IOException e) {
			LOGGER.error("Create '{}' fail", path, e);
		}
	}

	@Override
	public void onDelete(Path path) {
		try {
			Path to = getTo(path);
			if (Files.exists(to)) {
				if (Files.isDirectory(to)) {
					LOGGER.debug("Delete directory '{}'", to);
					FileUtils.deleteDirectory(to.toFile());
				} else {
					LOGGER.debug("Delete File '{}'", to);
					Files.delete(to);
				}
			}
		} catch (IOException e) {
			LOGGER.error("Delete '{}' fail", path, e);
		}
		;
	}

	@Override
	public void onModify(Path path) {
		try {
			Path from = watchedFolder.resolve(path);
			Path to = getTo(path);
			if (!Files.isDirectory(from)) {
				LOGGER.debug("Copy from '{}' to '{}'", from, to);
				FileUtils.forceMkdir(to.getParent().toFile());
				Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			LOGGER.error("Copy '{}' fail", path, e);
		}
	}

}

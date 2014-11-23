package com.famaridon.folder.sync.event.listener.impl;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;
import com.famaridon.folder.sync.event.listener.FolderEventListener;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by rolfone on 07/11/14.
 */
public class LessEventListener extends AbstractVDocEventListener implements FolderEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(LessEventListener.class);
	public static final String LESS_EXTENSION = "less";
	public static final String CSS_EXTENSION = "css";
	private final LessEngine lessEngine;

	public LessEventListener(Path watchedFolder, Path vdocHome) {
		super(watchedFolder, vdocHome);
		// Instantiates a new LessEngine
		lessEngine = new LessEngine();
	}

	@Override
	public void onCreate(Path path) {
		LOGGER.warn("create less file will not compile it!");

	}

	private void compileLess(Path path) {
		if (this.isLessFile(path.toString())) {
			try {
				File source = watchedFolder.resolve(path).toFile();
				File target = getCssFile(path);
				LOGGER.info("Compile less {}",FilenameUtils.getName(source.getAbsolutePath()));
				LOGGER.debug("Compile {} to {}",source,target);

				// Creates a new file containing the compiled content
				lessEngine.compile(source, getCssFile(path));
			} catch (IOException | LessException e) {
				LOGGER.error("Error during less compilation", e);
			}
		}
	}

	@Override
	public void onDelete(Path path) {
		if (this.isLessFile(path.toString())) {
			this.getCssFile(path).delete();
		}
	}

	@Override
	public void onModify(Path path) {
		compileLess(path);
	}

	protected final boolean isLessFile(String path) {
		return LESS_EXTENSION.equals(FilenameUtils.getExtension(path.toString()));
	}

	protected File getCssFile(Path path) {
		Path target = this.getTo(path);
		String baseName = FilenameUtils.getBaseName(path.toString());
		Path targetCss = target.getParent().resolve(baseName +'.'+ CSS_EXTENSION);
		return targetCss.toFile();
	}
}

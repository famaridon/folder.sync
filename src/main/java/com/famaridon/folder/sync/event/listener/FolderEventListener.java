package com.famaridon.folder.sync.event.listener;

import java.nio.file.Path;

/**
 * Created by rolfone on 07/11/14.
 */
public interface FolderEventListener {
	public void onCreate(Path path);

	public void onDelete(Path path);

	public void onModify(Path path);
}

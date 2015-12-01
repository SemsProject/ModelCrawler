package de.unirostock.sems.ModelCrawler;

import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public abstract class Constants {

	public static final String URN_START = "urn";
	public static final String URN_SEPARATOR = ":";
	public static final String URN_VERSION_PLACEHOLDER = "!";
	
	public static final String PATH_SPLIT_REGEX = "\\/";
	
	public static final int FTP_DEFAULT_PORT = 21;
	
	public static final String VERSION_INFO_FILENAME = "info.json";
	public static final String DEFAULT_TEMP_PREFIX = "modelcrawler";
	public static final FileAttribute<Set<PosixFilePermission>> TEMP_DIR_POSIX_ATTRIBUTES = PosixFilePermissions.asFileAttribute( PosixFilePermissions.fromString("rwx------") );
}

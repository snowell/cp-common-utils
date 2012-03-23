/*
 * Copyright (c) 2005-2011 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.common.io;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Closeables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileFilter;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <p>Collection of utility functions for file-centric operations.</p>
 *
 * @author Michael Grove
 * @since 1.0
 * @version 2.0
 */
public final class Files2 {

	private Files2() {
	}

	/**
	 * Create a valid directory from the given {@link File}
	 * @param theDirectory the directory to create
	 * @throws IOException if the directory cannot be created
	 * @return the newly created directory
	 */
	public static File createDirectory(File theDirectory) throws IOException {

		if (theDirectory.exists()) {
			if (!theDirectory.isDirectory()) {
				Files.deleteRecursively(theDirectory);
			}
		}
		else {
			if (!theDirectory.mkdirs()) {
				throw new IOException("Unable to create directory "+theDirectory);
			}
		}

		return theDirectory;
	}

    /**
     * Recursively traverse the directory and all its sub directories and return a list of all the files contained within.
     * @param theDirectory the start directory
     * @return all files in the start directory and all its sub directories.
     */
    public static List<File> listFiles(File theDirectory) {
		return listFiles(theDirectory, new FileFilter() {
			public boolean accept(File theFile) {
				// an accept all file filter
				return true;
			}
		});
    }

	/**
	 * Zip the given directory.
	 * @param theDir the directory to zip
	 * @param theOutputFile the zip file to write to
	 * @throws IOException thrown if there is an error while zipping the directory or while saving the results.
	 */
    public static void zipDirectory(File theDir, File theOutputFile) throws IOException {
        ZipOutputStream aZipOut = new ZipOutputStream(new FileOutputStream(theOutputFile));

		try {
			Collection<File> aFileList = listFiles(theDir);

			String aPathToRemove = theDir.getAbsolutePath().substring(0, theDir.getAbsolutePath().lastIndexOf(File.separator));

			for (File aFile : aFileList) {
				FileInputStream aFileIn = new FileInputStream(aFile);

				try {
					ZipEntry aZipEntry = new ZipEntry(aFile.getAbsolutePath().substring(aFile.getAbsolutePath().indexOf(aPathToRemove) + aPathToRemove.length() + 1));

					aZipOut.putNextEntry(aZipEntry);

					ByteStreams.copy(aFileIn, aZipOut);
				}
				finally {
					Closeables.closeQuietly(aFileIn);
					aZipOut.closeEntry();
				}
			}
		}
		finally {
			Closeables.closeQuietly(aZipOut);
		}
    }

	/**
	 * Recursively traverse the directory and all its sub directories and return a list of all the files contained within.
	 * Only files which match the file filter will be returned.
	 * @param theDirectory the start directory
	 * @param theFilter the file filter to use.
	 * @return the list of files in the directory (and its sub directories) which match the file filter.
	 */
	public static List<File> listFiles(File theDirectory, FileFilter theFilter) {

		ArrayList<File> aList = new ArrayList<File>();

		if (!theDirectory.isDirectory()) {
			return aList;
		}

        File[] aFileList = theDirectory.listFiles();
		
		if (aFileList == null) {
			return aList;
		}

        for (File aFile : aFileList) {
            if (aFile.isDirectory()) {
                aList.addAll(listFiles(aFile, theFilter));
            }
            else if (theFilter.accept(aFile)) {
                aList.add(aFile);
            }
        }

		return aList;
	}

	public static void copyDirectory(final File theSource, final File theDest) throws IOException {

		if (theSource.isDirectory()) {
			if (!theDest.exists()) {
				if (!theDest.mkdir()) {
					throw new IOException("Could not create destination");
				}
			}

			String[] aChildren = theSource.list();
			for (String aChild : aChildren) {
				copyDirectory(new File(theSource, aChild),
							  new File(theDest, aChild));
			}
		}
		else {
			InputStream in = new FileInputStream(theSource);
			OutputStream out = new FileOutputStream(theDest);

			ByteStreams.copy(in, out);

			Closeables.closeQuietly(in);
			Closeables.closeQuietly(out);
		}
	}
}

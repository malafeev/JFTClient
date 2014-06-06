package org.jftclient;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author smalafeev
 */
public class LocalFileUtils {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileUtils.class);

    /**
     * Copy file to directory<br/>
     * Copy file to file<br/>
     * Copy directory to directory
     *
     * @param src  source
     * @param dest destination
     * @return <code>true</code> if copied otherwise <code>false</code>
     */
    public static boolean copy(File src, File dest) {
        try {
            if (dest.isDirectory()) {
                if (src.isFile()) {
                    FileUtils.copyFileToDirectory(src, dest);
                } else {
                    FileUtils.copyDirectoryToDirectory(src, dest);
                }
            } else {
                if (src.isFile()) {
                    FileUtils.copyFile(src, dest);
                } else {
                    logger.warn("cannot copy dir to file");
                    return false;
                }
            }
        } catch (IOException e) {
            logger.warn("failed to copy", e);
            return false;
        }
        return true;
    }
}

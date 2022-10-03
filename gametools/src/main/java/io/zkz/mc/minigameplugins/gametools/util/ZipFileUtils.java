package io.zkz.mc.minigameplugins.gametools.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipFileUtils {
    private ZipFileUtils() {
    }

    public static void zipDirectory(File dir, File zipFile) throws IOException {
        FileOutputStream fout = new FileOutputStream(zipFile);
        ZipOutputStream zout = new ZipOutputStream(fout);
        zipSubDirectory("", dir, zout);
        zout.close();
    }

    private static void zipSubDirectory(String basePath, File dir, ZipOutputStream zout) throws IOException {
        byte[] buffer = new byte[4096];
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                String path = basePath + file.getName() + "/"; // NOSONAR java:S1075
                zout.putNextEntry(new ZipEntry(path));
                zipSubDirectory(path, file, zout);
                zout.closeEntry();
            } else {
                try (FileInputStream fin = new FileInputStream(file)) {
                    zout.putNextEntry(new ZipEntry(basePath + file.getName()));
                    int length;
                    while ((length = fin.read(buffer)) > 0) {
                        zout.write(buffer, 0, length);
                    }
                    zout.closeEntry();
                }
            }
        }
    }
}
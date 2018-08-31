package jbl.stc.com.utils;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUtils {

    public static final String DefaultCharset = "UTF-8";

    public static boolean isDirectory(String path) {
        File file = new File(path);
        return file.isDirectory();
    }

    public static String getFileName(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }

        File f = new File(path);
        if (!f.isFile())
            return "";

        return f.getName();
    }

    public static String getParentDirectory(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }

        File f = new File(path);
        return f.getParent();
    }

    public static boolean isFileExsit(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File f = new File(path);
        return f.exists();
    }

    public static boolean isFileExsit(File file) {
        if (file == null) {
            return false;
        }
        return file.exists();
    }

    public static void createDirectory(String filePath) {
        if (filePath.endsWith("/")) {
            createDirectory(new File(filePath));
        } else {
            createParentDirectory(new File(filePath));
        }
    }

    public static void createDirectory(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static void createParentDirectory(File path) {
        File dir = path.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static long getFileSize(String path) {
        if (!isFileExsit(path)) {
            return 0;
        }

        File f = new File(path);
        return f.length();
    }

    public static long getFileSize(File file) {
        if (file == null) {
            return 0;
        }
        return file.length();
    }

    public static void deleteDirectory(String path) {
        deleteDirectory(path, true);
    }

    public static void deleteDirectory(String path, boolean deleteChild) {
        if (!isFileExsit(path))
            return;

        File f = new File(path);

        if (deleteChild) {
            File[] children = f.listFiles();
            if (children != null && children.length > 0) {
                for (File child : children) {
                    if (child.isDirectory()) {
                        deleteDirectory(child.getAbsolutePath(), true);

                    } else {
                        child.delete();
                    }
                }
            }
        }

        f.delete();
    }

    public static void deleteFile(String path) {
        if (!isFileExsit(path)) {
            return;
        }

        File f = new File(path);
        f.delete();
    }

    public static void deleteFile(File file) {
        if (!isFileExsit(file)) {
            return;
        }

        file.delete();
    }

    public static boolean renameFile(String path, String newPath) {
        if (!isFileExsit(path)) {
            return false;
        }
        File f = new File(path);
        return f.renameTo(new File(newPath));
    }

    public static boolean renameFile(File src, File dst) {
        if (!isFileExsit(src)) {
            return false;
        }
        return src.renameTo(dst);
    }

    public static void copyFile(String fromPath, String toPath, boolean rewrite) {
        if (TextUtils.isEmpty(fromPath) || TextUtils.isEmpty(toPath))
            return;

        if (fromPath.equals(toPath))
            return;

        copyFile(new File(fromPath), new File(toPath), rewrite);
    }

    public static void copyFile(File fromFile, File toFile, boolean rewrite) {
        if (fromFile.getAbsolutePath().equals(toFile.getAbsolutePath())) {
            return;
        }

        if (!fromFile.exists() || !fromFile.isFile() || !fromFile.canRead()) {
            return;
        }

        createDirectory(toFile);

        if (toFile.exists() && rewrite) {
            toFile.delete();
        }

        FileInputStream fosfrom = null;
        FileOutputStream fosto = null;
        try {
            fosfrom = new FileInputStream(fromFile);
            fosto = new FileOutputStream(toFile);

            byte[] bt = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (fosfrom != null)
                    fosfrom.close();
                if (fosto != null)
                    fosto.close();
            } catch (IOException e) {

            }
        }
    }

    public static String readText(String filepath) throws IOException {
        return readText(filepath, DefaultCharset);
    }

    public static String readText(String filepath, String charsetName) throws IOException {
        FileInputStream inputStream = null;
        InputStreamReader reader = null;
        try {
            inputStream = new FileInputStream(filepath);
            reader = new InputStreamReader(inputStream, charsetName);

            char[] chars = new char[512];
            StringBuilder sb = new StringBuilder();
            int size;

            while ((size = reader.read(chars)) > 0) {
                sb.append(chars, 0, size);
            }
            return sb.toString();
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {

            }
        }
    }

    public static void writeText(String filepath, String content) throws IOException {
        writeText(filepath, content, DefaultCharset);
    }

    public static void writeText(String filepath, String content, String charsetName) throws IOException {
        createDirectory(filepath);
        deleteFile(filepath);

        FileOutputStream outputStream = null;
        OutputStreamWriter writer = null;
        try {
            outputStream = new FileOutputStream(filepath);
            writer = new OutputStreamWriter(outputStream, charsetName);
            writer.write(content);
            writer.flush();

        } finally {
            try {
                if (writer != null)
                    writer.close();
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {

            }
        }

    }
}

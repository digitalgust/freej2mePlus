package org.mini.net.file;

import com.sun.cldc.io.ConnectionBaseInterface;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Pattern;

public class Protocol implements ConnectionBaseInterface, FileConnection {

    File file;
    int mode;
    boolean timeouts;
    InputStream inputStream;
    OutputStream outputStream;
    private final static char DIR_SEP = '/';

    private final static String DIR_SEP_STR = "/";

    Protocol() {

    }

    @Override
    public Connection openPrim(String name, int mode, boolean timeouts) throws IOException {
        if (!name.startsWith("//")) {
            throw new IOException( /* #ifdef VERBOSE_EXCEPTIONS */ /// skipped                       "bad socket connection name: " + name
                    /* #endif */);
        }
        String path = name.substring(2);
        file = new File(path);
        this.mode = mode;
        this.timeouts = timeouts;
        return this;
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        if ((mode & Connector.READ) != 0) {
            if (inputStream == null) {
                inputStream = new FileInputStream(file);
            } else {
                throw new IOException("inputStream is opened");
            }
        } else {
            throw new IOException("open mode is not read");
        }
        return inputStream;
    }

    @Override
    public DataInputStream openDataInputStream() throws IOException {
        if (inputStream == null) {
            openInputStream();
        }
        return new DataInputStream(inputStream);
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        if ((mode & Connector.WRITE) != 0) {
            if (outputStream == null) {
                outputStream = new FileOutputStream(file);
            } else {
                throw new IOException("outputStream is opened");
            }
        } else {
            throw new IOException("open mode is not write");
        }
        return outputStream;
    }

    @Override
    public DataOutputStream openDataOutputStream() throws IOException {
        if (outputStream == null) {
            openOutputStream();
        }
        return new DataOutputStream(outputStream);
    }

    @Override
    public OutputStream openOutputStream(long byteOffset) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(byteOffset);
        return new FileOutputStream(raf.getFD()) {
            public void close() throws IOException {
                Protocol.this.outputStream = null;
                super.close();
            }
        };
    }

    @Override
    public long totalSize() {
        return -1;
    }

    @Override
    public long availableSize() {
        return -1;
    }

    @Override
    public long usedSize() {
        return -1;
    }

    @Override
    public long directorySize(boolean includeSubDirs) throws IOException {
        if (!file.isDirectory()) {
            throw new IOException("Not a directory " + file.getAbsolutePath());
        }
        return directorySize(file, includeSubDirs);
    }

    private static long directorySize(File dir, boolean includeSubDirs) throws IOException {
        long size = 0;

        File[] files = dir.listFiles();
        if (files == null) { // null if security restricted
            return 0L;
        }
        for (int i = 0; i < files.length; i++) {
            File child = files[i];

            if (includeSubDirs && child.isDirectory()) {
                size += directorySize(child, true);
            } else {
                size += child.length();
            }
        }

        return size;
    }

    @Override
    public long fileSize() throws IOException {
        if (file.isDirectory()) {
            throw new IOException("is a directory");
        }
        if (file.exists()) {
            return file.length();
        }
        return 0;
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public boolean canWrite() {
        return true;
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public void setReadable(boolean readable) throws IOException {

    }

    @Override
    public void setWritable(boolean writable) throws IOException {

    }

    @Override
    public void setHidden(boolean hidden) throws IOException {

    }

    @Override
    public Enumeration list() throws IOException {
        return list(null, false);
    }


    @Override
    public Enumeration list(String filter, boolean includeHidden) throws IOException {
        return listPrivileged(filter, includeHidden);
    }

    private Enumeration listPrivileged(final String filter, boolean includeHidden) throws IOException {
        if (!this.file.isDirectory()) {
            throw new IOException("Not a directory " + this.file.getAbsolutePath());
        }
        FilenameFilter filenameFilter = null;
        if (filter != null) {
            filenameFilter = new FilenameFilter() {
                private Pattern pattern;

                {
                    /* convert simple search pattern to regexp */
                    pattern = Pattern.compile(filter.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*"));
                }

                public boolean accept(File dir, String name) {
                    return pattern.matcher(name).matches();
                }
            };
        }

        File[] files = this.file.listFiles(filenameFilter);
        if (files == null) { // null if security restricted
            return (new Vector()).elements();
        }
        Vector list = new Vector();
        for (int i = 0; i < files.length; i++) {
            File child = files[i];
            if ((!includeHidden) && (child.isHidden())) {
                continue;
            }
            if (child.isDirectory()) {
                list.add(child.getName() + DIR_SEP);
            } else {
                list.add(child.getName());
            }
        }
        return list.elements();
    }

    @Override
    public void create() throws IOException {

        if (!file.createNewFile()) {
            throw new IOException("File already exists  " + file.getAbsolutePath());
        }
        ;
    }

    @Override
    public void mkdir() throws IOException {
        if (!file.mkdir()) {
            throw new IOException("Can't create directory " + file.getAbsolutePath());
        }
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public void delete() throws IOException {
        file.delete();
    }

    @Override
    public void rename(String newName) throws IOException {
        file.renameTo(new File(newName));
    }

    @Override
    public void truncate(long byteOffset) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        try {
            raf.setLength(byteOffset);
        } finally {
            raf.close();
        }
    }

    @Override
    public void setFileConnection(String s) throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        file = new File(s);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public String getURL() {
        try {
            return file.toURL().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }
}

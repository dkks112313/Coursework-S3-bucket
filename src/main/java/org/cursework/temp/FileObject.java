package org.cursework.temp;

import java.io.IOException;

public class FileObject extends FileWork {
    private String key;
    private Byte[] data;
    private MetaData metaData;

    public FileObject(String fileName) throws IOException {
        super(fileName);
    }


}

package org.cursework.parts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PartsFile {
    private long countParts;
    private File file;

    public PartsFile(File file) {
        this.file = file;
    }

    public long calculateParts() throws IOException {
        countParts = Files.size(Path.of(file.getAbsolutePath()));
        return countParts;
    }

    public long getCountParts() {
        return countParts;
    }
}

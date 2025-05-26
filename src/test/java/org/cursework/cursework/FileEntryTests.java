package org.cursework.cursework;

import org.cursework.controller.gui.FileEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileEntryTests {

    @Test
    void testConstructorAndAccessors() {
        FileEntry entry = new FileEntry("file.txt", "ZmlsZS50eHQ=");

        assertEquals("file.txt", entry.name());
        assertEquals("ZmlsZS50eHQ=", entry.encodedName());
    }

    @Test
    void testEquality() {
        FileEntry entry1 = new FileEntry("file.txt", "ZmlsZS50eHQ=");
        FileEntry entry2 = new FileEntry("file.txt", "ZmlsZS50eHQ=");
        FileEntry entry3 = new FileEntry("another.txt", "YW5vdGhlci50eHQ=");

        assertEquals(entry1, entry2);
        assertNotEquals(entry1, entry3);
    }

    @Test
    void testHashCode() {
        FileEntry entry1 = new FileEntry("file.txt", "ZmlsZS50eHQ=");
        FileEntry entry2 = new FileEntry("file.txt", "ZmlsZS50eHQ=");

        assertEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test
    void testToString() {
        FileEntry entry = new FileEntry("file.txt", "ZmlsZS50eHQ=");
        String expected = "FileEntry[name=file.txt, encodedName=ZmlsZS50eHQ=]";

        assertEquals(expected, entry.toString());
    }
}

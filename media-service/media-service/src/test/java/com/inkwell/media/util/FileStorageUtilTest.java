package com.inkwell.media.util;

import com.inkwell.media.exception.InvalidFileTypeException;
import com.inkwell.media.exception.FileStorageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("File Storage Util Tests")
class FileStorageUtilTest {

    @Test
    @DisplayName("Should validate allowed image file")
    void validateFile_acceptsAllowedImage() {
        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", "image".getBytes());

        assertDoesNotThrow(() -> FileStorageUtil.validateFile(file));
    }

    @Test
    @DisplayName("Should reject empty or invalid files")
    void validateFile_rejectsInvalidFiles() {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);
        MockMultipartFile text = new MockMultipartFile("file", "note.txt", "text/plain", "hello".getBytes());

        assertThrows(InvalidFileTypeException.class, () -> FileStorageUtil.validateFile(empty));
        assertThrows(InvalidFileTypeException.class, () -> FileStorageUtil.validateFile(text));
    }

    @Test
    @DisplayName("Should reject oversized image file")
    void validateFile_rejectsOversizedFile() {
        byte[] oversized = new byte[10241 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "huge.png", "image/png", oversized);

        assertThrows(InvalidFileTypeException.class, () -> FileStorageUtil.validateFile(file));
    }

    @Test
    @DisplayName("Should sanitize and generate file metadata")
    void filenameAndMetadataHelpers_returnExpectedValues() {
        String unique = FileStorageUtil.generateUniqueFilename("../my cover.png");

        assertTrue(FileStorageUtil.isAllowedMimeType("IMAGE/PNG"));
        assertFalse(FileStorageUtil.isAllowedMimeType(null));
        assertEquals(".._my_cover.png", FileStorageUtil.sanitizeFilename("../my cover.png"));
        assertTrue(unique.endsWith("-.._my_cover.png"));
        assertEquals("/api/v1/media/7/download", FileStorageUtil.generatePublicUrl(7));
        assertEquals("png", FileStorageUtil.getFileExtension("cover.PNG"));
        assertEquals("", FileStorageUtil.getFileExtension("cover"));
    }

    @Test
    @DisplayName("Should save, size, and delete file")
    void fileOperations_manageUploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", "image-data".getBytes());
        String filename = "test-storage-util.png";
        Path savedPath = Path.of("uploads", filename);

        try {
            String path = FileStorageUtil.saveFile(file, filename);

            assertTrue(path.endsWith(filename));
            assertTrue(Files.exists(savedPath));
            assertEquals(0L, FileStorageUtil.getFileSizeInKb(file));
            FileStorageUtil.deleteFile(filename);
            assertFalse(Files.exists(savedPath));
        } finally {
            Files.deleteIfExists(savedPath);
        }
    }

    @Test
    @DisplayName("Should ignore delete for missing file")
    void deleteFile_ignoresMissingFile() {
        assertDoesNotThrow(() -> FileStorageUtil.deleteFile("missing-storage-util.png"));
    }

    @Test
    @DisplayName("Should throw storage exception when save target is invalid")
    void saveFile_throwsWhenTargetCannotBeWritten() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", "image-data".getBytes());
        Path blockedTarget = Path.of("uploads", "blocked-storage-target.png");
        Files.createDirectories(blockedTarget);

        try {
            assertThrows(FileStorageException.class, () -> FileStorageUtil.saveFile(file, "blocked-storage-target.png"));
        } finally {
            Files.deleteIfExists(blockedTarget);
        }
    }
}

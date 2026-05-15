package com.inkwell.media.service;

import com.inkwell.media.dto.MediaDTO;
import com.inkwell.media.dto.UploadResponse;
import com.inkwell.media.entity.Media;
import com.inkwell.media.exception.MediaNotFoundException;
import com.inkwell.media.mapper.MediaMapper;
import com.inkwell.media.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MediaServiceImpl focusing on file name generation and soft-delete logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Media Service Tests")
class MediaServiceImplTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaMapper mediaMapper;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private MediaServiceImpl mediaService;

    private Media testMedia;
    private MediaDTO testMediaDTO;
    private static final Integer TEST_MEDIA_ID = 1;
    private static final Integer TEST_UPLOADER_ID = 100;
    private static final String TEST_FILENAME = "uuid-test-image.png";
    private static final String TEST_ORIGINAL_NAME = "test-image.png";
    private static final String TEST_MIME_TYPE = "image/png";
    private static final Long TEST_SIZE_KB = 512L;

    @BeforeEach
    void setUp() {
        testMedia = Media.builder()
                .mediaId(TEST_MEDIA_ID)
                .uploaderId(TEST_UPLOADER_ID)
                .filename(TEST_FILENAME)
                .originalName(TEST_ORIGINAL_NAME)
                .url("/media/1/download")
                .mimeType(TEST_MIME_TYPE)
                .sizeKb(TEST_SIZE_KB)
                .altText("Test Image")
                .linkedPostId(null)
                .uploadedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        testMediaDTO = MediaDTO.builder()
                .mediaId(TEST_MEDIA_ID)
                .uploaderId(TEST_UPLOADER_ID)
                .filename(TEST_FILENAME)
                .originalName(TEST_ORIGINAL_NAME)
                .url("/media/1/download")
                .mimeType(TEST_MIME_TYPE)
                .sizeKb(TEST_SIZE_KB)
                .altText("Test Image")
                .linkedPostId(null)
                .uploadedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
    }

    @Test
    @DisplayName("Should soft delete media by setting isDeleted to true")
    void testDeleteMedia_SoftDelete() {
        when(mediaRepository.findById(TEST_MEDIA_ID)).thenReturn(Optional.of(testMedia));

        mediaService.deleteMedia(TEST_MEDIA_ID);

        verify(mediaRepository).findById(TEST_MEDIA_ID);
        verify(mediaRepository).softDeleteById(TEST_MEDIA_ID);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent media")
    void testDeleteMedia_NotFound() {
        when(mediaRepository.findById(TEST_MEDIA_ID)).thenReturn(Optional.empty());

        assertThrows(MediaNotFoundException.class, () -> mediaService.deleteMedia(TEST_MEDIA_ID));
        verify(mediaRepository, never()).softDeleteById(any());
    }

    @Test
    @DisplayName("Should retrieve media by uploader ID")
    void testGetMediaByUploaderId_Success() {
        List<Media> mediaList = Arrays.asList(testMedia);
        when(mediaRepository.findByUploaderId(TEST_UPLOADER_ID)).thenReturn(mediaList);
        when(mediaMapper.toDTO(any(Media.class))).thenReturn(testMediaDTO);

        List<MediaDTO> result = mediaService.getMediaByUploaderId(TEST_UPLOADER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mediaRepository).findByUploaderId(TEST_UPLOADER_ID);
    }

    @Test
    @DisplayName("Should retrieve active media by ID")
    void getMediaById_returnsMappedMedia() {
        when(mediaRepository.findActiveMediaById(TEST_MEDIA_ID)).thenReturn(Optional.of(testMedia));
        when(mediaMapper.toDTO(testMedia)).thenReturn(testMediaDTO);

        MediaDTO result = mediaService.getMediaById(TEST_MEDIA_ID);

        assertEquals(TEST_MEDIA_ID, result.getMediaId());
        verify(mediaRepository).findActiveMediaById(TEST_MEDIA_ID);
    }

    @Test
    @DisplayName("Should throw exception when active media is not found")
    void getMediaById_throwsWhenMissing() {
        when(mediaRepository.findActiveMediaById(TEST_MEDIA_ID)).thenReturn(Optional.empty());

        assertThrows(MediaNotFoundException.class, () -> mediaService.getMediaById(TEST_MEDIA_ID));
    }

    @Test
    @DisplayName("Should retrieve media by filename when not deleted")
    void getMediaByFilename_returnsMappedMedia() {
        when(mediaRepository.findByFilename(TEST_FILENAME)).thenReturn(Optional.of(testMedia));
        when(mediaMapper.toDTO(testMedia)).thenReturn(testMediaDTO);

        MediaDTO result = mediaService.getMediaByFilename(TEST_FILENAME);

        assertEquals(TEST_FILENAME, result.getFilename());
        verify(mediaRepository).findByFilename(TEST_FILENAME);
    }

    @Test
    @DisplayName("Should reject soft-deleted media by filename")
    void getMediaByFilename_throwsWhenDeleted() {
        testMedia.setIsDeleted(true);
        when(mediaRepository.findByFilename(TEST_FILENAME)).thenReturn(Optional.of(testMedia));

        assertThrows(MediaNotFoundException.class, () -> mediaService.getMediaByFilename(TEST_FILENAME));
    }

    @Test
    @DisplayName("Should throw exception when filename is missing")
    void getMediaByFilename_throwsWhenMissing() {
        when(mediaRepository.findByFilename(TEST_FILENAME)).thenReturn(Optional.empty());

        assertThrows(MediaNotFoundException.class, () -> mediaService.getMediaByFilename(TEST_FILENAME));
    }

    @Test
    @DisplayName("Should retrieve media linked to a post")
    void getMediaByPostId_returnsMappedMedia() {
        when(mediaRepository.findByLinkedPostId(55)).thenReturn(List.of(testMedia));
        when(mediaMapper.toDTO(testMedia)).thenReturn(testMediaDTO);

        List<MediaDTO> result = mediaService.getMediaByPostId(55);

        assertEquals(1, result.size());
        verify(mediaRepository).findByLinkedPostId(55);
    }

    @Test
    @DisplayName("Should link active media to post")
    void linkToPost_updatesRepository() {
        when(mediaRepository.findActiveMediaById(TEST_MEDIA_ID)).thenReturn(Optional.of(testMedia));

        mediaService.linkToPost(TEST_MEDIA_ID, 55);

        verify(mediaRepository).linkToPost(TEST_MEDIA_ID, 55);
    }

    @Test
    @DisplayName("Should unlink existing media from post")
    void unlinkFromPost_updatesRepository() {
        when(mediaRepository.findById(TEST_MEDIA_ID)).thenReturn(Optional.of(testMedia));

        mediaService.unlinkFromPost(TEST_MEDIA_ID);

        verify(mediaRepository).unlinkFromPost(TEST_MEDIA_ID);
    }

    @Test
    @DisplayName("Should permanently delete existing media record")
    void permanentlyDeleteMedia_deletesRecord() {
        when(mediaRepository.findById(TEST_MEDIA_ID)).thenReturn(Optional.of(testMedia));

        mediaService.permanentlyDeleteMedia(TEST_MEDIA_ID);

        verify(mediaRepository).deleteById(TEST_MEDIA_ID);
    }

    @Test
    @DisplayName("Should throw when permanently deleting missing media")
    void permanentlyDeleteMedia_throwsWhenMissing() {
        when(mediaRepository.findById(TEST_MEDIA_ID)).thenReturn(Optional.empty());

        assertThrows(MediaNotFoundException.class, () -> mediaService.permanentlyDeleteMedia(TEST_MEDIA_ID));
        verify(mediaRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("Should hard delete record even when physical file delete fails")
    void permanentlyDeleteMedia_continuesWhenPhysicalDeleteFails() throws Exception {
        Path uploadsDir = Path.of("uploads");
        Path blockedDirectory = uploadsDir.resolve(TEST_FILENAME);
        Files.createDirectories(blockedDirectory);
        Path childFile = blockedDirectory.resolve("child.txt");
        Files.write(childFile, "locked".getBytes());
        when(mediaRepository.findById(TEST_MEDIA_ID)).thenReturn(Optional.of(testMedia));

        try {
            mediaService.permanentlyDeleteMedia(TEST_MEDIA_ID);

            verify(mediaRepository).deleteById(TEST_MEDIA_ID);
        } finally {
            Files.deleteIfExists(childFile);
            Files.deleteIfExists(blockedDirectory);
        }
    }

    @Test
    @DisplayName("Should retrieve all active media")
    void getAllMedia_returnsMappedMedia() {
        when(mediaRepository.findAllActiveMedia()).thenReturn(List.of(testMedia));
        when(mediaMapper.toDTO(testMedia)).thenReturn(testMediaDTO);

        List<MediaDTO> result = mediaService.getAllMedia();

        assertEquals(1, result.size());
        verify(mediaRepository).findAllActiveMedia();
    }

    @Test
    @DisplayName("Should download stored media bytes")
    void downloadMedia_returnsStoredFileBytes() throws Exception {
        Path uploadsDir = Path.of("uploads");
        Files.createDirectories(uploadsDir);
        Path storedFile = uploadsDir.resolve(TEST_FILENAME);
        Files.write(storedFile, "image-bytes".getBytes());
        when(mediaRepository.findActiveMediaById(TEST_MEDIA_ID)).thenReturn(Optional.of(testMedia));

        try {
            byte[] result = mediaService.downloadMedia(TEST_MEDIA_ID);

            assertArrayEquals("image-bytes".getBytes(), result);
        } finally {
            Files.deleteIfExists(storedFile);
        }
    }

    @Test
    @DisplayName("Should throw when linking missing media")
    void linkToPost_throwsWhenMediaMissing() {
        when(mediaRepository.findActiveMediaById(TEST_MEDIA_ID)).thenReturn(Optional.empty());

        assertThrows(MediaNotFoundException.class, () -> mediaService.linkToPost(TEST_MEDIA_ID, 55));
        verify(mediaRepository, never()).linkToPost(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should throw when unlinking missing media")
    void unlinkFromPost_throwsWhenMediaMissing() {
        when(mediaRepository.findById(TEST_MEDIA_ID)).thenReturn(Optional.empty());

        assertThrows(MediaNotFoundException.class, () -> mediaService.unlinkFromPost(TEST_MEDIA_ID));
        verify(mediaRepository, never()).unlinkFromPost(anyInt());
    }

    @Test
    @DisplayName("Should throw storage exception when stored file is missing")
    void downloadMedia_throwsWhenFileCannotBeRead() {
        when(mediaRepository.findActiveMediaById(TEST_MEDIA_ID)).thenReturn(Optional.of(testMedia));

        assertThrows(com.inkwell.media.exception.FileStorageException.class,
                () -> mediaService.downloadMedia(TEST_MEDIA_ID));
    }

    @Test
    @DisplayName("Should throw when downloading missing media")
    void downloadMedia_throwsWhenMediaMissing() {
        when(mediaRepository.findActiveMediaById(TEST_MEDIA_ID)).thenReturn(Optional.empty());

        assertThrows(MediaNotFoundException.class, () -> mediaService.downloadMedia(TEST_MEDIA_ID));
    }

    @Test
    @DisplayName("Should keep soft delete when physical file delete fails")
    void deleteMedia_continuesWhenPhysicalDeleteFails() throws Exception {
        Path uploadsDir = Path.of("uploads");
        Path blockedDirectory = uploadsDir.resolve(TEST_FILENAME);
        Files.createDirectories(blockedDirectory);
        Path childFile = blockedDirectory.resolve("child.txt");
        Files.write(childFile, "locked".getBytes());
        when(mediaRepository.findById(TEST_MEDIA_ID)).thenReturn(Optional.of(testMedia));

        try {
            mediaService.deleteMedia(TEST_MEDIA_ID);

            verify(mediaRepository).softDeleteById(TEST_MEDIA_ID);
        } finally {
            Files.deleteIfExists(childFile);
            Files.deleteIfExists(blockedDirectory);
        }
    }
}

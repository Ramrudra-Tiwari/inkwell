package com.inkwell.media.controller;

import com.inkwell.media.dto.MediaDTO;
import com.inkwell.media.dto.UploadResponse;
import com.inkwell.media.service.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Media Resource Tests")
class MediaResourceTest {

    @Mock
    private MediaService mediaService;

    private MediaResource mediaResource;
    private MediaDTO mediaDTO;

    @BeforeEach
    void setUp() {
        mediaResource = new MediaResource(mediaService);
        mediaDTO = MediaDTO.builder()
                .mediaId(1)
                .uploaderId(15)
                .filename("stored.png")
                .originalName("image.png")
                .url("/api/v1/media/1/download")
                .mimeType("image/png")
                .sizeKb(25L)
                .altText("Cover")
                .uploadedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
    }

    @Test
    @DisplayName("Should upload media")
    void uploadMedia_returnsCreatedResponse() {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "data".getBytes());
        UploadResponse uploadResponse = UploadResponse.builder()
                .mediaId(1)
                .originalName("image.png")
                .statusCode(201)
                .message("File uploaded successfully")
                .build();
        when(mediaService.uploadMedia(file, 15, "Cover")).thenReturn(uploadResponse);

        ResponseEntity<UploadResponse> response = mediaResource.uploadMedia(file, 15, "Cover");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().getMediaId());
    }

    @Test
    @DisplayName("Should read media metadata through lookup endpoints")
    void lookupEndpoints_returnMediaData() {
        when(mediaService.getMediaById(1)).thenReturn(mediaDTO);
        when(mediaService.getMediaByFilename("stored.png")).thenReturn(mediaDTO);
        when(mediaService.getMediaByUploaderId(15)).thenReturn(List.of(mediaDTO));
        when(mediaService.getMediaByPostId(99)).thenReturn(List.of(mediaDTO));
        when(mediaService.getAllMedia()).thenReturn(List.of(mediaDTO));

        assertEquals(1, mediaResource.getMedia(1).getBody().getMediaId());
        assertEquals("stored.png", mediaResource.getMediaByFilename("stored.png").getBody().getFilename());
        assertEquals(1, mediaResource.getMediaByUploader(15).getBody().size());
        assertEquals(1, mediaResource.getMediaByPost(99).getBody().size());
        assertEquals(1, mediaResource.getAllMedia().getBody().size());
    }

    @Test
    @DisplayName("Should link, unlink, and delete media")
    void mutationEndpoints_returnNoContent() {
        assertEquals(HttpStatus.NO_CONTENT, mediaResource.linkToPost(1, 99).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, mediaResource.unlinkFromPost(1).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, mediaResource.deleteMedia(1).getStatusCode());

        verify(mediaService).linkToPost(1, 99);
        verify(mediaService).unlinkFromPost(1);
        verify(mediaService).deleteMedia(1);
    }

    @Test
    @DisplayName("Should download media with filename and content type")
    void downloadMedia_returnsFileBytes() {
        when(mediaService.getMediaById(1)).thenReturn(mediaDTO);
        when(mediaService.downloadMedia(1)).thenReturn("data".getBytes());

        ResponseEntity<byte[]> response = mediaResource.downloadMedia(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals("data".getBytes(), response.getBody());
        assertEquals("image/png", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("image.png"));
    }

    @Test
    @DisplayName("Should expose health endpoint")
    void health_returnsStatusText() {
        assertTrue(mediaResource.health().getBody().contains("Media Service is running"));
    }
}

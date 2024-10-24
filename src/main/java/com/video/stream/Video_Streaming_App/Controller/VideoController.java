package com.video.stream.Video_Streaming_App.Controller;

import com.video.stream.Video_Streaming_App.AppConstants;
import com.video.stream.Video_Streaming_App.Entity.Video;
import com.video.stream.Video_Streaming_App.Exception.ResourceNotFoundException;
import com.video.stream.Video_Streaming_App.Payload.CustomMessage;
import com.video.stream.Video_Streaming_App.Service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


import com.video.stream.Video_Streaming_App.AppConstants;
import com.video.stream.Video_Streaming_App.Entity.Video;
import com.video.stream.Video_Streaming_App.Exception.ResourceNotFoundException;
import com.video.stream.Video_Streaming_App.Payload.CustomMessage;
import com.video.stream.Video_Streaming_App.Service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173") // Change this to your client URL
@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {
    @Autowired
    private VideoService videoService;

    @Value("${file.video.hsl}")
    private String HSL_DIR;

    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam("thumbnail") MultipartFile thumbnail,
            @RequestParam("title") String title,
            @RequestParam("description") String description
    ) {
        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoId(UUID.randomUUID().toString());

        // Save the video and the thumbnail
        Video savedVideo = videoService.save(video, file, thumbnail);

        if (savedVideo != null) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(video);
        } else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CustomMessage.builder()
                            .message("Video or thumbnail is not uploaded")
                            .success(false)
                            .build());
        }
    }

    @GetMapping
    public List<Video> getAll() {
        return videoService.getAll();
    }

    @GetMapping("/get")
    public String say() {
        return "Hello Developer";
    }

    @GetMapping("/description/{description}/master.m3u8")
    public ResponseEntity<Resource> serveMasterFileByDescription(@PathVariable String description) {
        List<Video> videos = videoService.getVideosByDescription(description);

        if (videos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Video video = videos.get(0);  // Using the first video for now
        Path path = Paths.get(HSL_DIR, video.getVideoId(), "master.m3u8");

        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(resource);
    }

    @GetMapping("/title/{title}/master.m3u8")
    public ResponseEntity<Resource> serveMasterFileByTitle(@PathVariable String title) {
        List<Video> videos = videoService.getVideosByTitle(title);

        if (videos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Video video = videos.get(0);
        Path path = Paths.get(HSL_DIR, video.getVideoId(), "master.m3u8");

        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(resource);
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> stream(@PathVariable String videoId) {
        Video video = videoService.get(videoId);
        String contentType = video.getContentType();
        String filePath = video.getFilePath();

        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }

    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamVideoRange(
            @PathVariable String videoId,
            @RequestHeader(value = "Range", required = false) String range
    ) {
        Video video = videoService.get(videoId);
        Path path = Paths.get(video.getFilePath());
        Resource resource = new FileSystemResource(path);
        String contentType = video.getContentType();

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        long fileLength = path.toFile().length();
        if (range == null) {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }

        try {
            long rangeStart;
            long rangeEnd;

            if (range.startsWith("bytes=")) {
                String[] ranges = range.replace("bytes=", "").split("-");
                rangeStart = ranges.length > 0 && !ranges[0].isEmpty() ? Long.parseLong(ranges[0]) : 0;
                rangeEnd = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : rangeStart + AppConstants.CHUNK_SIZE - 1;

                if (rangeEnd >= fileLength) {
                    rangeEnd = fileLength - 1;
                }
            } else {
                return ResponseEntity
                        .ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            }

            InputStream inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);

            long contentLength = rangeEnd - rangeStart + 1;
            byte[] data = new byte[(int) contentLength];
            int read = inputStream.read(data, 0, data.length);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
            httpHeaders.add("Pragma", "no-cache");
            httpHeaders.add("Expires", "0");
            httpHeaders.add("x-Content-Type-Options", "nosniff");
            httpHeaders.setContentLength(contentLength);

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(httpHeaders)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{videoId}/master.m3u8")
    public ResponseEntity<Resource> serveMasterFile(@PathVariable String videoId) {
        Path path = Paths.get(HSL_DIR, videoId, "master.m3u8");

        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(resource);
    }

    @GetMapping("/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> serveSegments(
            @PathVariable String videoId,
            @PathVariable String segment
    ) {
        Path path = Paths.get(HSL_DIR, videoId, segment + ".ts");
        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Resource resource = new FileSystemResource(path);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/mp2t")
                .body(resource);
    }
}


/*@CrossOrigin(origins = "http://localhost:5173") // Change this to your client URL
@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {
    @Autowired
    private VideoService videoService;

    @Value("${file.video.hsl}")
    private String HSL_DIR;

    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam("thumbnail") MultipartFile thumbnail,
            @RequestParam("title") String title,
            @RequestParam("description") String description
    ) {
        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoId(UUID.randomUUID().toString());

        // Save the video and the thumbnail
        Video savedVideo = videoService.save(video, file, thumbnail);

        if (savedVideo != null) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(video);
        } else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CustomMessage.builder()
                            .message("Video or thumbnail is not uploaded")
                            .success(false)
                            .build());
        }
    }

    @GetMapping
    public List<Video> getAll() {
        return videoService.getAll();
    }

    @GetMapping("/get")
    public String say() {
        return "Hello Developer";
    }

    @GetMapping("/description/{description}/master.m3u8")
    public ResponseEntity<Resource> serveMasterFileByDescription(@PathVariable String description) {
        List<Video> videos = videoService.getVideosByDescription(description);

        if (videos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Video video = videos.get(0);  // Using the first video for now
        Path path = Paths.get(HSL_DIR, video.getVideoId(), "master.m3u8");

        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(resource);
    }

    @GetMapping("/title/{title}/master.m3u8")
    public ResponseEntity<Resource> serveMasterFileByTitle(@PathVariable String title) {
        List<Video> videos = videoService.getVideosByTitle(title);

        if (videos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Video video = videos.get(0);
        Path path = Paths.get(HSL_DIR, video.getVideoId(), "master.m3u8");

        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(resource);
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> stream(@PathVariable String videoId) {
        Video video = videoService.get(videoId);
        String contentType = video.getContentType();
        String filePath = video.getFilePath();

        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }

    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamVideoRange(
            @PathVariable String videoId,
            @RequestHeader(value = "Range", required = false) String range
    ) {
        Video video = videoService.get(videoId);
        Path path = Paths.get(video.getFilePath());
        Resource resource = new FileSystemResource(path);
        String contentType = video.getContentType();

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        long fileLength = path.toFile().length();
        if (range == null) {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }

        try {
            long rangeStart;
            long rangeEnd;

            if (range.startsWith("bytes=")) {
                String[] ranges = range.replace("bytes=", "").split("-");
                rangeStart = ranges.length > 0 && !ranges[0].isEmpty() ? Long.parseLong(ranges[0]) : 0;
                rangeEnd = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : rangeStart + AppConstants.CHUNK_SIZE - 1;

                if (rangeEnd >= fileLength) {
                    rangeEnd = fileLength - 1;
                }
            } else {
                return ResponseEntity
                        .ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            }

            InputStream inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);

            long contentLength = rangeEnd - rangeStart + 1;
            byte[] data = new byte[(int) contentLength];
            int read = inputStream.read(data, 0, data.length);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
            httpHeaders.add("Pragma", "no-cache");
            httpHeaders.add("Expires", "0");
            httpHeaders.add("x-Content-Type-Options", "nosniff");
            httpHeaders.setContentLength(contentLength);

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(httpHeaders)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{videoId}/master.m3u8")
    public ResponseEntity<Resource> serveMasterFile(@PathVariable String videoId) {
        Path path = Paths.get(HSL_DIR, videoId, "master.m3u8");

        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(resource);
    }

    @GetMapping("/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> serveSegments(
            @PathVariable String videoId,
            @PathVariable String segment
    ) {
        Path path = Paths.get(HSL_DIR, videoId, segment + ".ts");
        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Resource resource = new FileSystemResource(path);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/mp2t")
                .body(resource);
    }
}*/

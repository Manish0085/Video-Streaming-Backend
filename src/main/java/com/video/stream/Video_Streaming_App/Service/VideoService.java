package com.video.stream.Video_Streaming_App.Service;

import com.video.stream.Video_Streaming_App.Entity.Video;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

public interface VideoService {

    Video get(String videoId);

    Video save(Video video, MultipartFile file, MultipartFile thumbnail);


    public List<Video> getVideosByTitle(String title);

    List<Video> getVideosByDescription(String description);

    List<Video> getAll();

    String processVideo(String videoId, MultipartFile file);

}

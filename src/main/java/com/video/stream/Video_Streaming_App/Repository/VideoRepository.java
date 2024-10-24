package com.video.stream.Video_Streaming_App.Repository;

import com.video.stream.Video_Streaming_App.Entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {

    List<Video> findByTitle(String title);
    List<Video> findByDescription(String description);
}

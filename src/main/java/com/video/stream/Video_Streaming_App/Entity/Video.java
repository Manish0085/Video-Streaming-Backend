package com.video.stream.Video_Streaming_App.Entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Video {

    @Id
    private String videoId;

    private String title;

    private String description;

    private String contentType;

    private String filePath;

    private String thumbnailPath;

}

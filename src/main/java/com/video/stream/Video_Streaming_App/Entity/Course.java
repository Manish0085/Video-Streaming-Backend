package com.video.stream.Video_Streaming_App.Entity;


import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Course {

    @Id
    private String Id;

    private String title;
}

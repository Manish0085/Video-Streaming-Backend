package com.video.stream.Video_Streaming_App.Payload;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomMessage {

    private String message;
    private boolean success = false;
}

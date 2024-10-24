package com.video.stream.Video_Streaming_App.Controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/video")
public class GeneralController {

    @GetMapping("/app")
    public String say(){
        return "Hello Developer";
    }
}

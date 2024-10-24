package com.video.stream.Video_Streaming_App;

import com.video.stream.Video_Streaming_App.Service.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class VideoStreamingAppApplicationTests {

	@Autowired
	VideoService videoService;

	@Test
	void contextLoads() {

		videoService.processVideo("64a09009-d747-4a22-8183-0a9c4f8d5acf", null);
	}

}

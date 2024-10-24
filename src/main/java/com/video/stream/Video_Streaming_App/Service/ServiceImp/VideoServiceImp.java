package com.video.stream.Video_Streaming_App.Service.ServiceImp;

import com.video.stream.Video_Streaming_App.Entity.Video;
import com.video.stream.Video_Streaming_App.Exception.ResourceNotFoundException;
import com.video.stream.Video_Streaming_App.Repository.VideoRepository;
import com.video.stream.Video_Streaming_App.Service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
//import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;



import com.video.stream.Video_Streaming_App.Entity.Video;
import com.video.stream.Video_Streaming_App.Exception.ResourceNotFoundException;
import com.video.stream.Video_Streaming_App.Repository.VideoRepository;
import com.video.stream.Video_Streaming_App.Service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class VideoServiceImp implements VideoService {
    @Value("${files.video}")
    String DIR;

    @Value("${file.video.hsl}")
    String HLS_DIR;

    @Value("${file.thumbnails}")
    private String THUMBNAIL_DIR;

    @Autowired
    private VideoRepository videoRepository;

    @PostConstruct
    public void init() {
        File file = new File(DIR);
        try {
            Files.createDirectories(Paths.get(HLS_DIR));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public Video save(Video video, MultipartFile file, MultipartFile thumbnail) {
        try {
            // Validate video file type
            if (!file.getContentType().startsWith("video/")) {
                throw new IllegalArgumentException("Invalid video file type");
            }

            // Handle video file saving
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();
            String cleanFileName = StringUtils.cleanPath(fileName);
            Path videoPath = Paths.get(DIR, cleanFileName);

            Files.copy(inputStream, videoPath, StandardCopyOption.REPLACE_EXISTING);
            video.setContentType(contentType);
            video.setFilePath(videoPath.toString());  // Save the path of the video file

            // Handle thumbnail saving if provided
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbnailFileName = UUID.randomUUID().toString() + ".jpg";
                Path thumbnailPath = Paths.get(THUMBNAIL_DIR, thumbnailFileName);
                Files.copy(thumbnail.getInputStream(), thumbnailPath, StandardCopyOption.REPLACE_EXISTING);
                video.setThumbnailPath(thumbnailPath.toString());  // Save the path of the thumbnail file
            }

            // Save the video entity in the repository
            Video savedVideo = videoRepository.save(video);

            // Optionally process the video (e.g., generate HLS segments)
            processVideo(savedVideo.getVideoId(), file);

            return savedVideo;

        } catch (IOException e) {
            throw new RuntimeException("Error saving video", e);
        }
    }


    @Override
    public List<Video> getAll() {
        return videoRepository.findAll();
    }

    @Override
    public Video get(String id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
    }

    @Override
    public List<Video> getVideosByDescription(String description) {
        return videoRepository.findByDescription(description);
    }

    @Override
    public List<Video> getVideosByTitle(String title) {
        return videoRepository.findByTitle(title);
    }

    // This method would handle the FFmpeg processing
    @Override
    public String processVideo(String videoId, MultipartFile file) {

        Video video = this.get(videoId);
        String filePath = video.getFilePath();

        //path where to store data:
        Path videoPath = Paths.get(filePath);


//        String output360p = HSL_DIR + videoId + "/360p/";
//        String output720p = HSL_DIR + videoId + "/720p/";
//        String output1080p = HSL_DIR + videoId + "/1080p/";

        try {
//            Files.createDirectories(Paths.get(output360p));
//            Files.createDirectories(Paths.get(output720p));
//            Files.createDirectories(Paths.get(output1080p));

            // ffmpeg command
            Path outputPath = Paths.get(HLS_DIR, videoId);

            Files.createDirectories(outputPath);


            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
                    videoPath, outputPath, outputPath
            );

//            StringBuilder ffmpegCmd = new StringBuilder();
//            ffmpegCmd.append("ffmpeg  -i ")
//                    .append(videoPath.toString())
//                    .append(" -c:v libx264 -c:a aac")
//                    .append(" ")
//                    .append("-map 0:v -map 0:a -s:v:0 640x360 -b:v:0 800k ")
//                    .append("-map 0:v -map 0:a -s:v:1 1280x720 -b:v:1 2800k ")
//                    .append("-map 0:v -map 0:a -s:v:2 1920x1080 -b:v:2 5000k ")
//                    .append("-var_stream_map \"v:0,a:0 v:1,a:0 v:2,a:0\" ")
//                    .append("-master_pl_name ").append(HSL_DIR).append(videoId).append("/master.m3u8 ")
//                    .append("-f hls -hls_time 10 -hls_list_size 0 ")
//                    .append("-hls_segment_filename \"").append(HSL_DIR).append(videoId).append("/v%v/fileSequence%d.ts\" ")
//                    .append("\"").append(HSL_DIR).append(videoId).append("/v%v/prog_index.m3u8\"");


            System.out.println(ffmpegCmd);
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("video processing failed!!");
            }

            return videoId;


        } catch (IOException ex) {
            throw new RuntimeException("Video processing fail!!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}


/*@Service
public class VideoServiceImp implements VideoService {
    @Value("${files.video}")
    String DIR;

    @Value("${file.video.hsl}")
    String HLS_DIR;
    @Value("${file.thumbnails}")
    private String THUMBNAIL_DIR;

    @PostConstruct
    public void init(){
        File file = new File(DIR);

        try {
            Files.createDirectories(Paths.get(HLS_DIR));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!file.exists()){
            file.mkdir();
            System.out.println("File has been created");
        }
        else {
            System.out.println("File already exits");
        }
    }
    @Autowired
    private VideoRepository videoRepository;
    @Override
    public Video get(String videoId) {

        Video video = videoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("Video Not Found"));
        return video;
    }




    @Override
    public Video save(Video video, MultipartFile file, MultipartFile thumbnail) {
        try {
            // Handle video file saving
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();

            String cleanFileName = StringUtils.cleanPath(fileName);
            String cleanFolder = StringUtils.cleanPath(DIR);

            Path path = Paths.get(cleanFolder, cleanFileName);

            System.out.println(contentType);
            System.out.println(path);

            // Save video file
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            video.setContentType(contentType);
            video.setFilePath(path.toString());

            // Handle thumbnail saving
            if (!thumbnail.isEmpty()) {
                String thumbnailFileName = UUID.randomUUID().toString() + ".jpg";  // Assuming .jpg for thumbnails
                String cleanThumbnailFolder = StringUtils.cleanPath(THUMBNAIL_DIR);  // Assuming a THUMBNAIL_DIR constant
                Path thumbnailPath = Paths.get(cleanThumbnailFolder, thumbnailFileName);

                InputStream thumbnailInputStream = thumbnail.getInputStream();
                Files.copy(thumbnailInputStream, thumbnailPath, StandardCopyOption.REPLACE_EXISTING);

                // Set the thumbnail path in the video entity
                video.setThumbnailPath(thumbnailPath.toString());
            }

            // Save the video entity to the repository
            Video savedVideo = videoRepository.save(video);

            // Process the video (e.g., transcoding)
            processVideo(savedVideo.getVideoId(), file);

            return savedVideo;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /*@Override
    public Video save(Video video, MultipartFile file) {


        try {


            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();


            String cleanFileName = StringUtils.cleanPath(fileName);
            String cleanFolder = StringUtils.cleanPath(DIR);

            Path path = Paths.get(cleanFolder, cleanFileName);


            System.out.println(contentType);
            System.out.println(path);

            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            video.setContentType(contentType);
            video.setFilePath(path.toString());

            Video savedVideo = videoRepository.save(video);

            processVideo(savedVideo.getVideoId(), file);

            return savedVideo;
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }

    }*/

    /*@Override
    public List<Video> getVideosByTitle(String title) {
        return videoRepository.findByTitle(title);
    }
    @Override
    public List<Video> getVideosByDescription(String description) {
        return videoRepository.findByDescription(description); // Ensure this query works as expected
    }

    @Override
    public List<Video> getAll() {
        return videoRepository.findAll();
    }



    @Override
    public String processVideo(String videoId, MultipartFile file) {

        Video video = this.get(videoId);
        String filePath = video.getFilePath();

        //path where to store data:
        Path videoPath = Paths.get(filePath);


//        String output360p = HSL_DIR + videoId + "/360p/";
//        String output720p = HSL_DIR + videoId + "/720p/";
//        String output1080p = HSL_DIR + videoId + "/1080p/";

        try {
//            Files.createDirectories(Paths.get(output360p));
//            Files.createDirectories(Paths.get(output720p));
//            Files.createDirectories(Paths.get(output1080p));

            // ffmpeg command
            Path outputPath = Paths.get(HLS_DIR, videoId);

            Files.createDirectories(outputPath);


            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
                    videoPath, outputPath, outputPath
            );

//            StringBuilder ffmpegCmd = new StringBuilder();
//            ffmpegCmd.append("ffmpeg  -i ")
//                    .append(videoPath.toString())
//                    .append(" -c:v libx264 -c:a aac")
//                    .append(" ")
//                    .append("-map 0:v -map 0:a -s:v:0 640x360 -b:v:0 800k ")
//                    .append("-map 0:v -map 0:a -s:v:1 1280x720 -b:v:1 2800k ")
//                    .append("-map 0:v -map 0:a -s:v:2 1920x1080 -b:v:2 5000k ")
//                    .append("-var_stream_map \"v:0,a:0 v:1,a:0 v:2,a:0\" ")
//                    .append("-master_pl_name ").append(HSL_DIR).append(videoId).append("/master.m3u8 ")
//                    .append("-f hls -hls_time 10 -hls_list_size 0 ")
//                    .append("-hls_segment_filename \"").append(HSL_DIR).append(videoId).append("/v%v/fileSequence%d.ts\" ")
//                    .append("\"").append(HSL_DIR).append(videoId).append("/v%v/prog_index.m3u8\"");


            System.out.println(ffmpegCmd);
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("video processing failed!!");
            }

            return videoId;


        } catch (IOException ex) {
            throw new RuntimeException("Video processing fail!!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}*/

import React, { useEffect, useState } from "react";


const VideoPlayer = ({ description }) => {
    const [videoSource, setVideoSource] = useState("");
    const [error, setError] = useState(null);
  
    const fetchVideoByDescription = async (description) => {
      try {
        const response = await fetch(`http://localhost:8081/api/v1/videos/description/${description}/master.m3u8`);
        
        if (!response.ok) {
          throw new Error("Video not found with description: " + description);
        }
  
        const playlistData = await response.text();
  
        setVideoSource(`http://localhost:8081/api/v1/videos/description/${description}/master.m3u8`);
        
      } catch (error) {
        console.error(error);
        setError(error.message);
      }
    };
  
    useEffect(() => {
      // Fetch video by description when the component mounts or when description changes
      fetchVideoByDescription(description);
    }, [description]);
  
    return (
      <div>
        <h3>Playing video for: {description}</h3>
  
        {error && <p>Error: {error}</p>}
  
        <video id="videoPlayer" controls width="640" height="360">
          {videoSource && (
            <source src={videoSource} type="application/vnd.apple.mpegurl" />
          )}
          Your browser does not support the video tag.
        </video>
      </div>
    );
  };
  
  export default VideoPlayer;
import { useState } from 'react';
import './App.css';
import VideoUpload from './components/VideoUpload';
import { Toaster } from 'react-hot-toast';
import VideoPlayer from './components/VideoPlayer';  // Make sure this path is correct
import { Button, TextInput } from 'flowbite-react';

function App() {
    const [videoId, setVideoId] = useState("a2978f0e-666c-4e60-8e3f-a8ab5103cfd2");
    const [fieldValue, setFieldValue] = useState(null);
    const [description, setDescription] = useState("");
    const [title, setTitle] = useState("");

    function playVideo(videoId) {
        setVideoId(videoId);
    }

   
function App() {
  return (
    <div className="App">
      <VideoPlayer description="recording" />
    </div>
  );
}

    const fetchVideoByDescription = async (description) => {
        try {
            const response = await fetch(`http://localhost:8081/api/v1/videos/description/${description}/master.m3u8`);
            
            // Check if the request was successful
            if (!response.ok) {
                throw new Error("Video not found with description: " + description);
            }
    
            // Since .m3u8 is a text-based format, use .text() instead of .json()
            const playlistData = await response.text();
    
            // You can use the playlist data to load the video into an HLS-capable player
            console.log(playlistData);  // For debugging purposes
            
            // Assuming you have a video player that supports HLS, you would pass this URL
            // to the player. Example with a video player could be:
            setVideoSource(`http://localhost:8081/api/v1/videos/description/${description}/master.m3u8`);
        } catch (error) {
            console.error(error);
            // Handle the error (e.g., display a message to the user)
        }
    };

    const fetchVideoByTitle = async () => {
        try {
            const response = await fetch(`http://localhost:8081/api/v1/videos/title?title=${title}`);
            if (!response.ok) {
                throw new Error("Video not found with title: " + title);
            }
            const video = await response.json();
            setVideoId(video.id); 
               } catch (error) {
            console.error(error);
        }
    };

    return (
        <>
            <Toaster />
            <div className="flex justify-center flex-col items-center space-y-9 py-9 mx-4 md:mx-10 lg:mx-20">
                <h1 className="text-3xl font-bold text-gray-700 dark:text-gray-100">Video Streaming App</h1>
                <div className="flex flex-col w-full lg:flex-row space-y-5 lg:space-y-0 lg:space-x-5 justify-between">
                    <div className="w-full lg:w-1/2 ">
                        <h2 className="text-white text-2xl font-bold text-center mt-2 shadow-md tracking-wide bg-gray-800 p-2 rounded-lg">
                            Playing Video
                        </h2>
                        <div className="bg-gray-800 p-4 rounded-lg">
                            <VideoPlayer src={`http://localhost:8081/api/v1/videos/${videoId}/master.m3u8`} />
                        </div>
                    </div>

                    <div className="w-full lg:w-1/2">
                        <h2 className="text-white text-2xl font-bold text-center mt-2 shadow-md tracking-wide bg-gray-800 p-2 rounded-lg">
                            Upload Video
                        </h2>
                        <div className="bg-gray-800 p-4 rounded-lg">
                            <VideoUpload />
                        </div>
                    </div>
                </div>

                <div className="my-4 flex space-x-5">
                    <TextInput
                        onChange={(event) => setFieldValue(event.target.value)}
                        placeholder="Enter video Id here"
                        name="video_id_field"
                        className="bg-gray-700 text-white px-4 py-2 rounded-md"
                    />
                    <Button
                        onClick={() => setVideoId(fieldValue)}
                        className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md"
                    >
                        Play
                    </Button>
                </div>

                {/* Fetch by Description */}
                <div className="my-4 flex space-x-5">
                    <TextInput
                        onChange={(event) => setDescription(event.target.value)}
                        placeholder="Enter video description"
                        name="video_description_field"
                        className="bg-gray-700 text-white px-4 py-2 rounded-md"
                    />
                    <Button
                        onClick={fetchVideoByDescription}
                        className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md"
                    >
                        Fetch by Description
                    </Button>
                </div>

                {/* Fetch by Title */}
                <div className="my-4 flex space-x-5">
                    <TextInput
                        onChange={(event) => setTitle(event.target.value)}
                        placeholder="Enter video title"
                        name="video_title_field"
                        className="bg-gray-700 text-white px-4 py-2 rounded-md"
                    />
                    <Button
                        onClick={fetchVideoByTitle}
                        className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md"
                    >
                        Fetch by Title
                    </Button>
                </div>
            </div>
        </>
    );
}

export default App;

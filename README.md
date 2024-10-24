Video Streaming Web Backend

This is a Spring Boot-based backend service for a video streaming web application. The application supports video upload, streaming, and HLS (HTTP Live Streaming) segment serving.

Features
Video Upload: Users can upload videos to the platform, including associated thumbnails.
Video Streaming: Supports streaming videos with HLS.
User Management: Each user can stream multiple videos, but each video is associated with a single user.
Thumbnail Support: Thumbnails are stored with each video and displayed on the frontend.
Progress and Notifications: Progress bar for uploads with success/failure notifications.
Tech Stack
Java
Spring Boot
H2/MySQL (Database) (Specify the database you're using)
Maven for dependency management
FFmpeg for video processing
HLS for streaming
Prerequisites
Java 17+
Maven 3.6+
Node.js (if frontend integration)
FFmpeg installed on your system


Backend Setup
Clone the repository:
git clone https://github.com/yourusername/your-repo-name.git
cd your-repo-name

Install dependencies:
mvn clean install

Configure the database in application.properties (update if you're using an external DB):
spring.datasource.url=jdbc:h2:mem:your-db
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update

Run the application:
mvn spring-boot:run

Future Enhancements
Comment section for videos
Improved search and filter functionality
Adaptive bitrate streaming

Contributing
Feel free to submit issues or pull requests. Contributions are welcome!

License
This project is licensed under the MIT License - see the LICENSE file for details.

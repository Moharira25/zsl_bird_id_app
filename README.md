# ZSL Bird ID - Bird Song Quiz Application

## Overview

The **ZSL Bird ID** application is a web-based platform designed to help users identify birds through their songs. The application allows users to participate in interactive sessions where they can listen to bird songs, watch related videos, and answer questions to improve their bird identification skills.

This project uses **Spring Boot** for the backend, **Thymeleaf** for server-side rendering, **WebSockets** for real-time communication, and **MySQL** as the database. The frontend is built using HTML, CSS, and JavaScript.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Setup and Installation](#setup-and-installation)
- [Configuration](#configuration)
- [Bird Data Directory](#bird-data-directory)
- [Running the Application](#running-the-application)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Create and manage sessions**: Admin users can create and manage sessions for individual or group quizzes.
- **Real-time updates**: Leverages WebSockets to provide real-time updates to participants during quizzes.
- **Bird song quiz**: Allows users to listen to bird songs and select the bird they believe corresponds to the song.
- **Score tracking and statistics**: Provides real-time feedback and statistics for quizzes, including minimum, maximum, average, and median scores.
- **Interactive multimedia**: Displays videos and images of birds to enhance learning and engagement.
- **Responsive design**: Optimized for both desktop and mobile devices.

## Technologies Used

- **Java**: The primary programming language for the backend.
- **Spring Boot**: Provides the main framework for the application, including web, JPA, and WebSocket modules.
- **Thymeleaf**: Template engine for rendering HTML on the server side.
- **WebSockets**: Used for real-time communication between the server and the client.
- **MySQL**: Relational database management system used to store data.
- **HTML/CSS/JavaScript**: Frontend technologies for building the user interface.
- **Lombok**: Reduces boilerplate code in Java classes.
- **Dotenv**: Manages environment variables.

## Prerequisites

Before you begin, ensure you have the following installed on your machine:

- **Java Development Kit (JDK) 17** or higher
- **Apache Maven**: For building and managing the project's dependencies.
- **MySQL Server**: For the application's database.
- **Node.js and npm** (optional): If you plan to manage frontend dependencies and build tools.
- **Git**: For version control.

## Setup and Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/zsl-bird-id.git
   cd zsl-bird-id
2. **Set Up MySQL Database**:

* Create a new MySQL database for the project.

* Note down the database name, username, and password.

3. **Configure Environment Variables**:

* Create or update the application.properties file in the src/main/resources directory with the following content:

```properties

spring.application.name=zsl_birdID

spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name

spring.datasource.username=your_database_username

spring.datasource.password=your_database_password

spring.jpa.hibernate.ddl-auto=create-drop

spring.jpa.show-sql=true

logging.level.org.hibernate.SQL=debug

SERVER_IP=192.168.0.34

SERVER_PORT=8080

```

   * Replace SERVER_IP with the IP address of the machine running the server. Ensure this IP is accessible by other users for the QR code to work.

4. **Install Dependencies**:

*. Ensure you have Maven installed and run the following command to download the project dependencies:

```bash

./mvnw clean install

```

5. **Build the Project**:

* Use Maven to compile the project and build the executable JAR file:

```bash

./mvnw clean package

```

## Configuration

The application uses application.properties to manage its configuration. Key settings include:

 *Database Configuration**: Database URL, username, and password.

 *Server IP and Port**: Define the server IP and port. The SERVER_IP must be accessible to clients using the QR code.

## Bird Data Directory
The application initializes bird data from a specified directory on the server. You need to set the path to this directory in the BirdInitializer component. Update the baseDirectory variable in BirdInitializer with the path where bird files are stored.

Example:

```java
Copy code
private final String baseDirectory = "C:\\Path\\To\\Birds";
```
The BirdInitializer component scans this directory for bird data files (audio, image, video) and updates the database accordingly. Ensure the directory structure is as expected, with directories for each bird and optional subdirectories for additional options.

## Running the Application

1. Start the MySQL Server:

   * Ensure your MySQL server is running and the database is accessible.

2. Run the Application:

   * You can start the application using the Spring Boot Maven plugin:

```bash

./mvnw spring-boot:run

```

   * Alternatively, run the packaged JAR file:

```bash

java -jar target/zsl-bird-id-0.0.1-SNAPSHOT.jar

```

3. Access the Application:

   * Open your web browser and navigate to http://localhost:8080 to view the application.

   * Users can join sessions by scanning the QR code displayed at the root endpoint **/**. This QR code directs users to the index page, eliminating the need to manually type in the address http//:192.168.0.1:8080 (note the ip address and the por number in here are just examples).

## Usage

 **Creating a Session**: go to the endpoint **/explore** or from the landing page and use the **Create a new session** form to set up a quiz session.

 **Participating in a Session**: Users can join active sessions clicking on the join session button in the endpoint **/explore**.

 **Viewing Results**: Admin users can view the results and statistics for each session once it is completed.

## Contributing

Contributions are welcome! Please follow these steps to contribute:

1. **Fork the Repository**: Create a fork of this repository on your GitHub account.

2. **Create a Branch**: Create a new branch for your feature or bug fix.
```bash
git checkout -b feature/your-feature-name
```
3. **Make Changes**: Commit your changes to the branch.

4. **Push Changes**: Push your changes to your forked repository.
```bash
git push origin feature/your-feature-name
```
5. **Create a Pull Request**: Open a pull request to the main repository with a description of your changes.
    - Go to the Pull Requests tab of the original repository on GitHub.
    - Click the New Pull Request button.
    - Select your branch from the dropdown.
    - Review the changes, add a title and description, and submit the pull request.
6. **Review and Feedback**: Engage with reviewers if there are any comments or feedback on your pull request. Make necessary updates and respond to feedback.
7. **Merge**: Once the pull request is reviewed and approved, it will be merged into the main repository by the project maintainers.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

For any inquiries or additional information, please contact the project maintainer at [GitHub - Moharira25](https://github.com/Moharira25).



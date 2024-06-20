# ISOConnect - Islamic Society App

## Overview

Welcome to ISOConnect, the full-stack Android application designed to enhance communication and collaboration among members of Islamic societies across universities. Developed using Kotlin and Android Studio, the app leverages Firebase for backend services and Google Cloud Services for key functionalities. It provides a platform for users to connect, share information, and stay updated with society events and activities.

## Features

- **User Authentication**: Secure sign-in and sign-up functionality using Firebase Authentication.
- **Messaging System**: Private and group chat functionalities for seamless communication.
- **Encrypted Messages**: End-to-end encryption algorithms which protect converations between users.
- **Event Management**: Post and manage society events with date, time, and location details.
- **Prayer Timetable**: Display daily prayer timings based on location.
- **Announcement Board**: Share important announcements and updates with society members.
- **Public Events Map**: Visual representation of events on an interactive map for easy navigation.
- **User-Friendly Interface**: Intuitive and attractive UI designed with Figma.


## Technologies Used

- **Kotlin**: Primary programming language for Android app development.
- **Android Studio**: Integrated Development Environment (IDE) used for development.
- **Firebase**: Backend services including Authentication, Firestore, and Cloud Functions.
- **Google Cloud Services**: Maps and Places APIs used to integrate the events map.
- **Figma**: For UI/UX design.
- **Agile Methodology**: Iterative development approach for continuous improvement.


## Installation

1. **Clone the Repository**:
    ```bash
    git clone https://github.com/Soran03/ISOConnect.git
    ```
2. **Open in Android Studio**:
    - Open Android Studio.
    - Click on `File` > `Open` and navigate to the cloned repository.

3. **Configure Firebase**:
    - Create a new project in [Firebase Console](https://console.firebase.google.com/).
    - Add an Android app to your Firebase project.
    - Download the `google-services.json` file and place it in the `app/` directory of your Android project.
    - Enable Authentication and Firestore in the Firebase Console.
    - Create `api_keys.xml` file in the values folder of your project resources folder and add your API key:
        ```properties
        API_KEY=your_api_key_here
        ```

4. **Build and Run**:
    - Connect your Android device or start an emulator.
    - Click on `Run` > `Run 'app'`.


## Screenshots

The following images are a select few screenshots of the app, full list can be found in the screenshots folder.

![Login Screen](screenshots.Sign_in.png)
![Home Page](screenshots/Home.png)
![Isocs Page](screenshots/Isocs.png)
![Isoc Chat Page](screenshots/isoc_chat.png)
![Event Details Page](screenshots/event_details.png)




## Contact

If you have any questions or suggestions, feel free to reach out:

- **Email**: azizsoran01@gmail.com
- **LinkedIn**: [Aziz Soran](https://www.linkedin.com/in/aziz-soran/)

---

Thank you for checking out the Car Rental App! We hope you find it useful and easy to use.


# MRP Group 9 - Celtic Knotwork

## Setup steps:
1. clone the github repository
2. Import the project as a gradle project into your IDE (IntelliJ or Eclipse)

**(Optional)** Additional steps for Canny-Edge Detector (OpenCV)
1) go to the [OpenCV website](https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html) and follow the install instructions for your operating system.
2) follow the [setup instructions](https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html#set-up-opencv-for-java-in-eclipse) for your IDE

Gradle should take care of the rest. Generally the code should be executable with JDK8 or higher.

### Troubleshooting

In case your eclipse IDE version is old gradle might not be included as a plugin.
Here you can find instructions on how to install the plugin and import a gradle project in eclipse:
- http://makble.com/how-to-import-gradle-project-into-eclipse

If you are using IntelliJ and have issues importing the gradle project check this tutorial:
- https://www.jetbrains.com/help/idea/gradle.html#gradle_import_project

In case you have issues with gradle, follow these steps:
1. Make sure the build tool gradle is install: https://gradle.org/install/
2. run:`gradle build`in the project root directory
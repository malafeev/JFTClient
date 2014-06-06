## JFTClient

JavaFX based ssh client. 

## Key Features:

- copy files/folders by drag and drop
- rename, delete files/folders
- create new folders   

## Screenshots

![Alt text](/screenshots/screenshot.png?raw=true)

## Building from Source

### Prerequisites

- JDK 8
- Gradle 
- rsync & sshpass

### Supported OS

- Linux
- OS X

### Installation

In the directory that contains build.gradle run

     gradle install
     
It will build, test and install application to *~/jftclient* folder.
 

### To run

If it was installed then start JFTClient: 

    ~/jftclient/bin/jftclient

Otherwise in the directory that contains build.gradle run:
 
    gradle run 

## License 
GNU GPLv3.
Copyright (C) 2014 Sergei Malafeev <sergeymalafeev@gmail.com>

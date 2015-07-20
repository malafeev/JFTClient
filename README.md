## JFTClient

JavaFX based ssh client. 

## Features

- copy files/folders by drag and drop
- rename, delete files/folders
- create new folders   
- simple local and remote terminals

## Screenshots

![Alt text](/screenshots/screenshot.png?raw=true)


## Prerequisites

- JDK 8 (update 51 or later)
- rsync & sshpass
- Gradle (only needed if building from source)

## Supported OS

- Linux
- OS X

## To Run

Download jftclient-XX.zip

https://github.com/malafeev/JFTClient/releases

Start JFTClient

     ./bin/jftclient

## Building from Source

### Installation

In the directory that contains build.gradle run

     gradle install
     
It will build, test and install application to `~/jftclient` folder.
 
### Run

If it was installed then start JFTClient: 

    ~/jftclient/bin/jftclient

Otherwise in the directory that contains build.gradle run:
 
    gradle run 

## Implementation Details

JFTClient uses embedded H2 database. Data file is saved in `~/.jftclient/` folder.

## License 
GNU GPLv3.
Copyright (C) 2014 Sergei Malafeev <sergeymalafeev@gmail.com>

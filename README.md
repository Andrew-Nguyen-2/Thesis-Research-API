This project is part of thesis submitted for Master of Science in Computer Science Degree . This API is intended for use by a small physics collaboration group (Hall A, B, C, or D at Jefferson Labs and their collaborators) who share a professional relationship. This API uses open-source tools: RabbitMQ (message broker), CloudAMQP (RabbitMQ server provider), and Magic-Wormhole (secure file transfer).

# Thesis Abstract
Technical advances are enabling scientists to acquire data at unprecedented rates. At the same time, there is increased pressure to share data and to visualize data in a manner that is accessible to a wider audience. Sharing data will lead to more interdisciplinary scientific collaboration. Collaboration, however, can be difficult as data is produced in different formats where it may not be usable by others and the transmission of data takes focus, time, and resources away from the researchers. In this project, a service-oriented framework was designed and created to increase collaboration by minimizing the amount of human interaction needed to share data. The framework allows users to notify others of the data produced or translated, and of the data formats available or requested without having to manually search for and then request the data. This is done by utilizing the RabbitMQ message broker system and a heterogeneous client base using both Python and Java, the latter distributed as JAR files which can be included directly into the applications used to produce, translate, analyze, or visualize the data. 

This work was funded by SURA grant 2020-FEMT-006-03

# Folder Guide
## api
- Contains ResearchAPI.java file and is the entry point for the API
## constants
- Contains constants used throughout project including the host URL for CloudAMQP server
## example
- Example of JSON message sent and received using RabbitMQ
## logging
- Message logging
## message
- Executive.java: Used to make command line calls in Java (Magic-Wormhole)
- FileData.java: Class to create objects containing file information (file name and file size)
- Message.java: Creates and converts message object for RabbitMQ
- Metadata.java: Creates and converts message metadata
- ProcessMessage.java: Processes received messages and determines how to handle the message (request data, request translation, or ignore message)
- Wormhole.java: Called by ProcessMessage and makes the Magic-Wormhole request or send by creating command line arguments to be passed to Executive
## rabbitmq
- Create the connection to RabbitMQ server and send direct or broadcast messages
## user
- Stores information relevant to the user such as wantFormats, convertFormats, or file paths

# Multi-User Multi-Transaction Server
The project involves building a multi-user, multi-transaction server that can run in the UNIX/LINUX environment. The goal is to utilize threads, thread synchronization techniques (such as semaphores), and parallel processing to create a scalable and efficient system. This project demonstrates the use of Java, C++ and client-server architecture. (2021).

### About Chat Room Using This Server:
This is a chat window application that is composed of two main components, a server-side component and a client-side component. This means that the server
backend can run in a separate environment than the client. The client is written in Java, so that it can run in any environment that supports Java.

### How It Works:
* On the client side, users have the functionality to connect to one of multiple chat-rooms and engage in text-conversation
    * They may disconnect from the chatroom when they desire, this sends a message to other chatroom members of the user’s disconnection
* On the server-side, the administering user is able to specify the amount of chat-rooms they would like to create via the command-line interface
    * The server can host a theoretical infinite amount of chatrooms and maintains connections to multiple clients over the implementation of sockets and threads

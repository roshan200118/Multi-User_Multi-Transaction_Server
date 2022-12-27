#include "thread.h"
#include "socketserver.h"
#include <stdlib.h>
#include <time.h>
#include <list>
#include <vector>
#include <thread>
#include "socket.h"
#include <vector>
#include "Semaphore.h"
#include <algorithm>

using namespace Sync;
using namespace std;

//SocketThread class
//Socket of each client
class SocketThread : public Thread
{
private:
    //Variable to reference to the connected socket
    Socket &socket;

    //Variable to store the data received from the socket
    ByteArray input;

    //Variable to check termination
    bool &terminate;

    //Variable to reference the port number
    int portNumber;

    //Vector to store references to the client socket threads
    vector<SocketThread *> &clientSocketThreads;

    //Variable to reference the chatroom number
    int chatRoomNumber;

public:
    //SocketThread constructor 
    SocketThread(Socket &socket, bool &terminate, int portNumber, vector<SocketThread *> &clientSocketThreads)
        : socket(socket), terminate(terminate), portNumber(portNumber), clientSocketThreads(clientSocketThreads)
    {
    }

    //SocketThread destructor 
    ~SocketThread()
    {
    }

    //Getter for socket
    Socket &GetSocket()
    {
        return socket;
    }

    //Getter for chat room number
    const int GetChatRoom()
    {
        return chatRoomNumber;
    }

    //Receives data from the socket, and outputs it to all sockets
    //Gets data from a client and sends to all clients
    virtual long ThreadMain()
    {
        //Convert the port number into a string
        string portStr = to_string(portNumber);

        //Get a reference to the Semaphore
        Semaphore protect(portStr);

        //passed to this function is a socket and an array of sockets by reference
        try
        {
            //Read the data from the socket (chatroom number)
            socket.Read(input);

            //Store the chat room number
            string chatRoomNum = input.ToString();
            cout << chatRoomNum << endl;

            //Convert the chat room number into a int
            chatRoomNumber = stoi(chatRoomNum);
            cout << chatRoomNumber << endl;

            //While the socket is not to be terminated
            while (!terminate)
            {
                //If nothing is read, do nothing
                int i = socket.Read(input);
                if (i == 0)
                {
                    break;
                }

                //If there is data, convert it into a string
                string receivedStr = input.ToString();

                //Print the user's input
                cout << receivedStr << endl;

                //This is case of chat change 
                if (receivedStr[0] == '/')
                {
                    //Get the chat number and save into a string
                    string chatStr = receivedStr.substr(1, receivedStr.size() - 1);

                    //Convert the chat string into an int
                    chatRoomNumber = stoi(chatStr);

                    cout << chatRoomNumber << endl;
                    continue;
                }

                //Get the semaphore so thread can enter critical area
                protect.Wait();

                //Loop through client sockeck threads
                for (int i = 0; i < clientSocketThreads.size(); i++)
                {
                    //Store the current client's socket thread
                    SocketThread *clientSocketThread = clientSocketThreads[i];

                    //If the client socket thread has the same chat room number
                    if (clientSocketThread->GetChatRoom() == chatRoomNumber)
                    {
                        //Get the socket of the client and save it into a variable
                        Socket &clientSocket = clientSocketThread->GetSocket();

                        //Convert the received data into a ByteArray
                        ByteArray sendMsg(receivedStr);

                        //Send the data into the clients socket
                        clientSocket.Write(sendMsg);
                    }
                }
                //Release the semaphore so other processes can use it
                protect.Signal();

            }
        }

        //Error handling
        catch (string &s)
        {
            cout << "Error in socketThread" << endl;
            cout << s << endl;
        }

        //Error handling
        catch (exception &e)
        {
            cout << "Error in socketThread" << endl;
            cout << e.what() << endl;
        }

        //Print client is gone 
        cout << "Client thread is terminated" << endl;

        return 0;
    }

};

//This is the server's thread
class ServerThread : public Thread
{
private:
    //Variable to store reference to server's socket
    SocketServer &server;

    //Holds reference to client sockets
    vector<SocketThread *> socketThreads;

    //Variable to check if server is terminated
    bool terminate = false;

    //Variable to store the port number
    int portNumber;

    //Variable to store the chat number
    int chatNumber;

public:
    //ServerThread constructor
    ServerThread(SocketServer &server, int portNumber, int chatNumber)
        : server(server), portNumber(portNumber), chatNumber(chatNumber)
    {
    }

    //ServerThread destructor 
    //Handles how server is terminated
    ~ServerThread()
    {
        //For all the client socket threads
        for (auto thread : socketThreads)
        {
            try
            {
                //Get the socket to close
                Socket &socketToClose = thread->GetSocket();

                //Message to be sent to clients
                string msg = "Server is shut down";

                //Convert message into ByteArray
                ByteArray sendMsg(msg);
                
                //Write the message to Client's socket
                socketToClose.Write(msg);

                //Close the client socket
                socketToClose.Close();
            }

            //Error
            catch (...)
            {
                cout << "Error in serverThread destructor" << endl;
            }
        }

        //Thread loops and terminates
        terminate = true;
    }

    //Server operations
    virtual long ThreadMain()
    {
        while (true)
        {
            try
            {
                //Convert port number to a string
                string portStr = to_string(portNumber);

                cout << "Waiting for a client to connect" << endl;

                //Get a reference to the Semaphore
                Semaphore protect(portStr, 1, true);

                //Convert number of chats to a string
                string numberOfChats = to_string(chatNumber) + '\n';

                //Convert the number of chats to a ByteArray
                ByteArray baNumChats(numberOfChats);

                //Wait for socket connection with client
                Socket socket = server.Accept();

                //Write the number of chats
                socket.Write(baNumChats);

                //Create the socket for new connection
                Socket *newConnection = new Socket(socket);

                //Reference the new socket thread
                Socket &socketReference = *newConnection;

                //Add the new socket
                socketThreads.push_back(new SocketThread(socketReference, terminate, portNumber, ref(socketThreads)));
            }

            //Error
            catch (TerminationException terminationException)
            {
                cout << "Currently shutting down" << endl;
                return terminationException;
            }

            //Error
            catch (string error)
            {
                cout << endl
                     << "[Error] " << error << endl;
                return 1;
            }
        }
    }
};

//Main program
int main(void)
{
    //Variable to store the port number
    int portNumber = 2021;

    //Variable to store the number of chat rooms
    int numberOfRooms;

    //Prompt the user
    cout << "This is Bing Bong Chat Server" << endl;
    cout << "How many Bing Bong chat rooms would you like? Please enter a number:" << endl;

    //Save the number of chat rooms 
    cin >> numberOfRooms;
    cout << "Type 'done' to terminate the Server" << endl;

    //Create the server's socket
    SocketServer server(portNumber);

    //Create the server's thread
    ServerThread st(server, portNumber, numberOfRooms);

    //Check for input
    while (true)
    {
        //Variable to store the user's inpuy
        string stopCommand;

        //Get the user's input and save it 
        getline(cin, stopCommand);

        //If the input is "done"
        if (stopCommand == "done")
        {
            //Shutdown the server
            server.Shutdown();
            break;
        }
    }
}
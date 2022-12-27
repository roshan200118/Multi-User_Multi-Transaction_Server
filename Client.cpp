
#include "thread.h"
#include "socket.h"
#include <iostream>
#include <stdlib.h>
#include <time.h>
using namespace Sync;
using namespace std;

//Declaring a variable for when the client is finished
bool finished = false;

//The ClientThread class
//This thread maintains the connection to the server
class ClientThread : public Thread
{
private:
	//Reference to the connected socket
	Socket &socket;

	//Declaring a variable to store the data as ByteArray
	ByteArray data;

	//Declaring a variable to store the data as String
	string data_str;

public:
	//ClientThread constructor
	ClientThread(Socket &socket)
		: socket(socket)
	{
	}

	//ClientThread destructor
	~ClientThread()
	{
		//CLEANUP
		cout << "Cleaning up client" << endl;
	}

	//Receieves the user input and sends it to the server
	virtual long ThreadMain()
	{
		//Open the socket
		socket.Open();

		//Loop
		while (true)
		{
			//Promt user to input
			cout << "Enter a string (or type 'done' to exit): ";
			cout.flush();

			//Save the user's input into a variable
			getline(cin, data_str);

			//If the input is 'done'
			if (data_str == "done")
			{
				//Prompt that the program is terminating
				cout << "Client terminating. " << endl;

				//Client is finished
				finished = true;

				//Close the socket
				socket.Close();

				//Break the loop
				break;
			}

			//Convert the data_str string into a ByteArray
			data = ByteArray(data_str);

			//Send the user's input to the server
			socket.Write(data);

			//Get the resulting ByteArray from the server
			socket.Read(data);

			//Convert the ByteArray into a String
			data_str = data.ToString();

			//Print the server's response
			cout << "Modified string: " << data_str << endl;
		}
		return 0;
	}
};

//The main program
int main(void)
{
	//Welcome the user
	cout << "SE3313 Lab 3 Client" << endl;

	//Create our socket
	Socket socket("127.0.0.1", 3000);

	//Creating a ClientThread object
	ClientThread clientThread(socket);

	//Loop
	while (!finished)
	{
		sleep(1);
	}

	//Close the socket
	socket.Close();

	return 0;
}

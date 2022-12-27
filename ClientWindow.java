//package Client;

//Java Swing is for GUI

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.net.URL;


//ClientWindow class
//Extends the JFrame class (creates the main window)
public class ClientWindow extends JFrame
{
    public static void main(String[] args)
	{
        //Creates a new ClientWindow
        new ClientWindow();
    }

    private JLabel lblName;                 	//Label to show the name field
    private JTextField txtName;            	    //The user's name textfield (textbox)
    private JLabel msgName;             	    //The message name label
    private JButton btnConnect;        	        //Button for connection to server (the 'Connect' button)
    private JButton btnDisconnect;    	        //Button to disconnect from server (the 'Disconnect' button)
    private JTextArea chatBox;                	//Multi-line text area for messaging
    private JScrollPane scrollPane;            	//Allows the message area to be scrollable
    private JTextField txtField;            	//The message txtField (textbox)
    private JButton btnSend;                	//The "Send" button
    private JComboBox chatRoomSelect;           //Combo box for showing the multiple chat rooms (drop-down)
    private JLabel lblRoom;                	    //Label for selecting the chat room from the combo box
    private Socket sock;                    	//Socket object for connecting with server
    private boolean isConnected;                //Checks if client is connected
    private boolean canConnect;        	        //Checks if user is initially connected to chat
    private PrintWriter buffer;                 //PrintWriter for sock's OutputStream


    public ClientWindow()
	{
        //Set the boarders to how it would look on it's native system
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Initialize variables
        this.isConnected = false;
        this.canConnect = true;

        //The window size
        this.setSize(750, 400);

        //Window title
        this.setTitle("Bing Bong Chat Room");

        //Get the icon resource
        URL iconURL = getClass().getResource("/BingBong.png");
        ImageIcon icon = new ImageIcon(iconURL);

        //Set the window icon
        this.setIconImage(icon.getImage());

        //Promts user to connect first
        final String[] chatRooms = {"Click Connect Button"};
        chatRoomSelect = new JComboBox(chatRooms);

        //Create the top panel
        JPanel panelTop = new JPanel();

        //Arrange the components horozontally (left to right)
        panelTop.setLayout(new FlowLayout());

        //Setting the text for the name label
        lblName = new JLabel("Name: ");

        //Add the lblName to the top panel
        panelTop.add(lblName);

        //The txtField box of length 5, with preset value of 'User'
        txtName = new JTextField("User", 5);

        //Add the txtName to the top panel
        panelTop.add(txtName);

        //Set the text for the chat room label
        lblRoom = new JLabel("Choose Chat Room: ");

        //Add the chat room label to the top panel
        panelTop.add(lblRoom);

        //Add the chatRoomSelect to the top panel
        panelTop.add(chatRoomSelect);

        //Create the text area
        chatBox = new JTextArea();

        //The text won't go off the screen
        chatBox.setLineWrap(true);

        //If the text goes off the screen, the whole word will be on the next line
        chatBox.setWrapStyleWord(true);

        //Create the scroll pane, and the textArea is embedded in it
        scrollPane = new JScrollPane(chatBox);

        //Monitor chatBox for changes
        chatBox.getDocument().addDocumentListener(new DocumentListener()
		{

            @Override
            public void removeUpdate(DocumentEvent arg0)
			{
                // EmptyMethod (Unused)
            }

            //Called when text is inserted
            @Override
            public void insertUpdate(DocumentEvent arg0)
			{
                //Caret is now at a new line
                chatBox.setCaretPosition(chatBox.getText().length());
            }

            @Override
            public void changedUpdate(DocumentEvent arg0)
			{
                // EmptyMethod (Unused) 

            }
        });

        //Create the bottom panel
        JPanel panelBottom = new JPanel();

        //Arrange the components horozontally (left to right)
        panelBottom.setLayout(new FlowLayout());

        //Set the message label text
        msgName = new JLabel("Message:");

        //Add the message label to the bottom
        panelBottom.add(msgName);

        //The txtField will have a size of 30, this is where message is inputted
        txtField = new JTextField(30);

        //Add the txtField to the bottom panel
        panelBottom.add(txtField);

        //Creating the Send button and set the text of it
        btnSend = new JButton("Send");

        //Defines how message is sent
        SendMessage sendBtnClick = new SendMessage();

        //When Send button is clicked, perform the sendBtnClick
        btnSend.addActionListener(sendBtnClick);

        //The Send button is originally disabled
        btnSend.setEnabled(false);

        //Add the Send button to the bottom
        panelBottom.add(btnSend);

        //Creating the Connect button
        btnConnect = new JButton("Connect");

        //Defines how the connection is made
        Connection makeConnection = new Connection(chatBox);

        //When Connect button is clicked, perform makeConnection
        btnConnect.addActionListener(makeConnection);

        //Add the Connect button to the bottom panel
        panelBottom.add(btnConnect);

        //Creating the Disconnect button
        btnDisconnect = new JButton("Disconnect");

        //Defines how the disconnection is made
        Disconnection disconnect = new Disconnection();

        //When the Disconnect button is clicked, perform the diconnectAction
        btnDisconnect.addActionListener(disconnect);

        //The Disconnect button is disabled
        btnDisconnect.setEnabled(false);

        //Add the Disconnect button to the bottom
        panelBottom.add(btnDisconnect);

        //When there is a change with the chatRoomSelect
        chatRoomSelect.addActionListener(new ActionListener()
		{
            public void actionPerformed(ActionEvent e)
			{
                //If there is not initial connection (not connected)
                if (!canConnect)
				{
                    System.out.println("User is switching rooms");

                    //Save the chatRoomSelect text into a variable
                    String chatRoomSelectNumber = String.valueOf(chatRoomSelect.getSelectedItem());

                    //Variable to hold the chat room number
                    int number;

                    //Get the number of the chat room
                    String mynum = chatRoomSelectNumber.split(" ")[2];

                    //Convert the string into a number and save it
                    number = Integer.parseInt(mynum);

                    //Send string to server to handle chat room switching
                    String sendStr = "/" + number;
                    buffer.println(sendStr);
                    System.out.println(number);
                    chatBox.setText("");
                }
            }
        });

        //If "ENTER" button is clicked, perform same function as the Send button
        txtField.addKeyListener(new KeyAdapter()
		{
            public void keyPressed(KeyEvent ke)
			{
                if (ke.getKeyChar() == KeyEvent.VK_ENTER)
				{
                    btnSend.doClick();
                }
            }
        });

        //Creates a border layout with gaps between them
        this.setLayout(new BorderLayout(5, 5));

        //Add the top panel to the top
        this.add(panelTop, BorderLayout.NORTH);

        //Add the scroll pane to the center
        this.add(scrollPane, BorderLayout.CENTER);

        //Add the bottom panel to the bottom
        this.add(panelBottom, BorderLayout.SOUTH);

        //Listener interface for window events
        this.addWindowListener(new WindowAdapter()
		{
            //Invoked when user is closing a window
            public void windowClosing(WindowEvent e)
			{
                //If the user is connected
                if (isConnected)
				{
                    try
					{
                        if (isConnected)
						{
                            //Print shutdown
                            buffer.println("Client Window Closing");
                            buffer.flush();

                            //Close the socket
                            sock.close();
                        }
                    }
					catch (IOException e1)
					{
                        System.out.println("Socket is closed");
                    }
                }

                System.out.println("The client program is exited");

                //Terminate the program
                System.exit(0);
            }
        });

        //Make the window visible
        this.setVisible(true);
    }


    class ClientReceive extends Thread
	{
        //Variable to store the number of rooms
        private int roomNumber;

        //Constructor
        ClientReceive(int num)
		{
            this.roomNumber = num;
        }

        //When the thread is running
        public void run()
		{
            try
			{
                //Creating a BufferedReader object to read the input from the server
                BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                while (true)
				{
                    //While the sock is not closed and no bytes are buffered in the socket
                    while (!sock.isClosed() && sock.getInputStream().available() <= 0)
                        ;

                    //If the sock is closed, print feedback and break
                    if (sock.isClosed())
					{
                        System.out.println("The client socket thread has been closed");
                        break;
                    }

                    //If there is an inital connection
                    if (canConnect)
					{
                        //Print the room number
                        buffer.println(this.roomNumber);

                        //Get the number of rooms from the user's input from the server
                        int numRooms = Integer.parseInt(br.readLine());

                        //Remove all the items in the chatRoomSelect
                        chatRoomSelect.removeAllItems();

                        //Loop to add the Chat room numbers to the chatRoomSelect
                        for (int i = 1; i <= numRooms; i++) {
                            String strroom = "Chat Room " + i;
                            chatRoomSelect.addItem(strroom);
                        }

                        //The user is now connected, so no inital connection
                        canConnect = false;
                        continue;
                    }

                    //Get the date and time, and add it to the text area
                    LocalDateTime dt = LocalDateTime.now();
                    chatBox.append(dt + ":\n");
                    chatBox.append(br.readLine() + '\n');
                }
            }

            //If thread can't run, then must be terminated
            catch (IOException e)
			{
                System.out.println("Goodbye!");
            }
        }
    }

    //Defines how message is sent
    class SendMessage implements ActionListener
	{
        @Override
        public void actionPerformed(ActionEvent evt)
		{
            if (isConnected)
			{
                try
				{
                    //Save the user's message into a variable
                    String getText = txtField.getText();

                    //Removes any "/" which may affect how message is sent
                    String sendText = getText.replace("/", "");

                    //Print the user's name and message in the chat
                    buffer.println(txtName.getText() + ": " + sendText);
                    buffer.flush();

                    //Make the message textbox empty
                    txtField.setText("");
                }

                //If error, print error
                catch (Exception ex)
				{
                    System.out.println("Could not send message");
                }
            }

            //Print connection error
            else
			{
                System.out.println("Connection error");
            }
        }
    }

    //Defines how connection is made
    class Connection implements ActionListener
	{
        //Reference the JTextArea
        JTextArea txtArea;

        //Constructor
        Connection(JTextArea txtArea)
		{
            this.txtArea = txtArea;
        }

        @Override
        public void actionPerformed(ActionEvent e)
		{
            try
			{
                //Clear the current text within the JTextArea
                this.txtArea.setText("");

                //Variable to reference the IP address
                String ip;

                //Variable to reference the port #
                int port;

                // The IP address is of that of the server
                // Must change if changing server host!
                ip = "192.168.56.103";
                //CHANGED TO RUN ON MAC LOCAL
                //ip = "127.0.0.1";

                //Get the chat room number
                String chatRoomSelectNumber = String.valueOf(chatRoomSelect.getSelectedItem());
                String mynum = chatRoomSelectNumber.split(" ")[2];

                //Variable to store the chat room number
                int number;

                //Using port 2021
                port = 2021;

                //If it is an initial connectino
                if (canConnect)
				{
                    //Print in
                    System.out.println("Initial connection established");

                    //Assign chat room 1 on start
                    number = 1;
                }

                //If user is already connected
                else
				{
                    //Assign the current chat room number
                    number = Integer.parseInt(mynum);
                }

                //Create the sock with IP address and port #
                sock = new Socket(ip, port);

                //If the sock is connected
                if (sock.isConnected())
				{
                    System.out.println("Successfully Connected To Server");

                    //Set connected to true
                    isConnected = true;

                    //The Connect button is disabled
                    btnConnect.setEnabled(false);

                    //The Disconnect button is enabled
                    btnDisconnect.setEnabled(true);

                    //The Send button is enabled
                    btnSend.setEnabled(true);

                    //Creating PrintWriter using sock's OutputStream with auto flush enabled
                    buffer = new PrintWriter(sock.getOutputStream(), true);

                    //The Client is now in it's respective room
                    //Start the thread
                    new ClientReceive(number).start();
                }
            }

            //If unable to find the server
            catch (UnknownHostException e1)
			{
                System.out.println("Can't find server");
            }

            //If can't connect to server
            catch (ConnectException e2)
			{
                System.out.println("Server is unable to be connected to.");
            }

            //Other error
            catch (IOException e3)
			{
                e3.printStackTrace();
            }
        }
    }


    //Defines how disconnection is handled
    class Disconnection implements ActionListener
	{

        @Override
        public void actionPerformed(ActionEvent e)
		{
            //If the user is connected
            if (isConnected)
			{
                try
				{
                    //Get the user's name
                    String userName = txtName.getText();

                    //Print that the user is disconnected
                    buffer.println(userName + " has disconnected.");
                    buffer.flush();

                    //Close the socket
                    sock.close();

                    //If the socket is closed
                    if (sock.isClosed())
					{
                        //Connected is false
                        isConnected = false;

                        //The Connect button is enabled
                        btnConnect.setEnabled(true);

                        //The Disconnect button is disabled
                        btnDisconnect.setEnabled(false);
                    }
                    //Terminate the program
                    System.exit(0);

                }
                //Print error
                catch (IOException e1)
				{
                    buffer.println("Shutting down client socket because server is not running");
                }
            }
        }
    }
}
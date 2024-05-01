# CS370TeamProject
 Chat room

[] The primary function of this chat room is to allow clients to connect to the server and chat globally with all other currently connected clients. It includes various functionality for the users such as:


   --> adding friends

			
   --> removing friends

			
   --> viewing their friends list

			
   --> saving their chat history

			
   --> viewing their chat history

			
   --> clearing their chat history

			
   --> requesting individual chats with other online friends (alpha)

			
   --> viewing the currently connected clients

			

[] There is a login feature that requires the user to login using an already registerd username and password or register as a new user if needed



[] The clients are multi-threaded allowing a large number of clients to connect at once


   
[] The data handling is done on the server side using local .ser files that live in the directory of the project


   --> this is done using a server/client protocol in which the clients send requests that are parsed by the server and data is sent back /     manipulated by the server depending on the request

   
   --> concurrent hashmaps are used as the primary data structures for the data to minimize deadlock/race conditions 


[] to compile:


--> javac *.java


[] to start server:


--> java Server


[] to start client:


--> java Client *server IP Address

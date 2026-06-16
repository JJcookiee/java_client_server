Client server chat application for programming 04 COM5015M
found at https://github.com/JJcookiee/java_client_server

## Table of contents
- [App Start](#app-start)
- [Core Functionality](#core-functionality)
- [Extended Functionality](#extended-functionality)
- [Security & Legal Considerations](#security--legal-considerations)

---

## App Start

# Building the project
mvn clean package

# Start the server
java -jar target/app-1.0-SNAPSHOT-jar-with-dependencies.jar server

# Start a client
java -jar target/app-1.0-SNAPSHOT-jar-with-dependencies.jar

## Core Functionality

Choose custom port (client and server) - user asked in App.getPort()
Choose custom address (client) - user asked in App.getAddress()
Choose custom username (client) - user asked in App.getUser()
Send/Receive messages - JSON files sent to clients with ServerRunnable.ru()
Admin can access log files - admin can type comamnds into the console with InputHandler and Server.checkCommand
Admin can start and stop server - see the functionality above
Admin can access webpage with basic info - the serverLog info is turned into html and put into a browser for the admin to read better using serverRunnable.run()


## Extended Functionality

# Content Filtering
The WordFilter class laods some banned words from a file (not explicit incase the university complains) and censors the messages client-side before even being sent to the server. Banned words are replaced with asterisks

# Web-based chat UI
the client run a local http server using WebHandler on a port (normally 8000) which auto refreshes the messages they can see. It has a form that can post messages back to the client and then to the server

## Security & Legal Considerations

It stores the ip and address of the clients perminantly. In a real scenario these would have to be deleted after 30 days to comply with GDPR. Users should also ne informed their IP is being logged



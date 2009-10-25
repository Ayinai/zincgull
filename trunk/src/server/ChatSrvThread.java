package server;
import java.io.*;
import java.net.*;

public class ChatSrvThread extends Thread {
	private ChatSrv server;
	private Socket socket;
	
	private int id;
	private double random;

	public ChatSrvThread( ChatSrv server, Socket socket ) {
		this.server = server;
		this.socket = socket;
		start();
	}

	public void run() {	
		try {
			DataInputStream dis = new DataInputStream( socket.getInputStream() );	//gets messages from client
			while (true) {
				String message = dis.readUTF();
				//System.out.println( "MSG "+ChatSrv.getTime()+": "+message );	//DEBUG
				if( !specialCommand(message) ){		//check if it's a special command or not
					System.out.println( "MSG "+ChatSrv.getTime()+": "+ ChatSrv.nick.get(id)+": "+message.substring(5) );
					server.sendToAll( ChatSrv.nick.get(id)+": "+message.substring(5) );
				}
			}
		} catch( EOFException ie ) {		//no failmsg
		} catch( IOException ie ) {
		} finally {
			server.removeConnection( socket, id );	//closing socket when connection is lost
		}
	}
	
	void sendTo( String message ) {
		try {
			DataOutputStream dos = new DataOutputStream( socket.getOutputStream() );	//get outputstreams
			dos.writeUTF( message );		//and send message
		} catch( IOException ie ) { 
			System.out.println( ChatSrv.getTime()+": "+ie ); 		//failmsg
		}
	}
	
	private void setUsername(String e, boolean a){
		if( !e.isEmpty() ){
			if( ChatSrv.nick.isEmpty() ){
				String send = e+" joined, "+ChatSrv.nick.size()+" users online";
				System.out.println( "              "+send );
				server.sendToAll( "-> "+send);
				ChatSrv.nick.add(e);
				id = ChatSrv.nick.indexOf(e);
			}else{
				boolean unique = true;
				for (int i = 0; i < ChatSrv.nick.size(); i++) {
					if( ChatSrv.nick.get(i).equals( e ) ){	//needs to be unique
						unique = false;
					}
				}
				if( unique ){
					if( a ){	//its the /HELLO-command
						ChatSrv.nick.add(e);
						id = ChatSrv.nick.indexOf(e);
						String send = e+" joined, "+ChatSrv.nick.size()+" users online";
						System.out.println( "              "+send );
						server.sendToAll( "-> "+send);
					}else{		//its the /nick-command
						System.out.println( "USR "+ChatSrv.getTime()+": "+ChatSrv.nick.get(id)+" -> "+e );
						server.sendToAll( ChatSrv.nick.get(id)+" became "+e );
						ChatSrv.nick.set(id, e);
						sendTo("/nick "+e);
					}
				}else{
					if( a ){	//its the /HELLO-command
						ChatSrv.nick.add(Double.toString(random));
						id = ChatSrv.nick.indexOf(e);
						sendTo("Chosen username already exists. Type /nick followed by another username to change.");
						String send = random+" joined, "+ChatSrv.nick.size()+" users online";
						System.out.println( "              "+send );
						server.sendToAll( "-> "+send);
					}else{		//its the /nick-command
						System.out.println( "USR "+ChatSrv.getTime()+": "+ChatSrv.nick.get(id)+" failed changing username to "+e );
						sendTo("You picked an existing nickname, try again.");
					}
				}
			}
		}else{
			
		}
	}
	
	public boolean specialCommand( String msg ){
		if( msg.substring(0, 1).equals("/") ){
			if( msg.length() >= 6){
				if( msg.substring(0, 5).equals("/msg ") ){
					return false;
				}else if( msg.substring(0, 6).equals("/nick ") ){	//expecting a hello-message at first connection
					String[] temp;
					msg = msg.substring(6);
					temp = msg.split(":");
					setUsername(temp[0], false);
					return true;
				}else if(msg.substring(0, 6).equals("/users") ){
					sendTo( "Currently there are "+ ChatSrv.nick.size() +" users online" );
					System.out.println( "USR "+ChatSrv.getTime()+": "+ ChatSrv.nick.get(id) +" asks how many users are online" );
					return true;
				}else if( msg.substring(0, 6).equals("/HELLO") ){	//expecting a hello-message at first connection
					if( msg.length() > 7 ){
						String[] temp;
						msg = msg.substring(7);
						temp = msg.split(":");
						sendTo("Welcome to the Zincgull chatserver!");		//welcome-message
						random = Double.parseDouble( temp[1] );
						setUsername(temp[0], true);	//true because its the first time
						return true;
					}
					sendTo( "You need a nickname" );	//colorize this!
					return true;
				}
			}
		}
		sendTo( "Not a real command, type /help for possible commands" );	//colorize this!
		return true;
	}
}

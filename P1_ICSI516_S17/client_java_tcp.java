/*
 * Student Name: RUJIT RAVAL
 * Albany ID: 001319222
 * Website & Code referred for algorithm: 
 * http://codereview.stackexchange.com/questions/83219/order-of-operations-algorithm-for-calculator
*/

import java.io.*;
import java.net.*;
import java.util.*;

class client_java_tcp 
{
   public static void main(String argv[]) throws Exception
   {
	   
	  try
	  {
			  Scanner input = new Scanner(System.in);
			   
			  System.out.println("Enter server name or IP address:");
			  String ServerName = input.next(); 
			  
			  System.out.println("Enter port:");
			  Integer ServerPort = input.nextInt();
			  
			  if(ServerPort<0 && ServerPort > 65535)
			  {
				  System.out.println("Invalid Port Number Terminating..");
				  System.exit(1);
			  }
			//  String sentence;
		      String Result;
		      
		     
		      Socket clientSocket = new Socket(ServerName, ServerPort);
		     
		     
		      BufferedReader inFromUser = 
		         new BufferedReader(
		         new InputStreamReader(System.in));
		
		      DataOutputStream outToServer =
		         new DataOutputStream(
		         clientSocket.getOutputStream());
		
		      BufferedReader inFromServer =
		         new BufferedReader(
		         new InputStreamReader(
		         clientSocket.getInputStream()));
		
		      System.out.println("Enter expression:" + '\n');
		 
			  
		      String Expression = inFromUser.readLine();
			  //System.out.println(Expression);
		      outToServer.writeBytes(Expression + '\n');
		      Result = inFromServer.readLine();
		      System.out.println("FROM SERVER: " + Result + '\n');
		      
		      int ex = Integer.parseInt(Result);
		      
		      for(int i = 0; i < ex; i++)
		      {
		    	  System.out.println("Socket Programming.");
		      }
	     
		      clientSocket.close();
		      input.close();
		   }
		
	  catch (UnknownHostException e){
		  System.out.println("Could not connect to the server. Terminating..");
	  }
	  catch (SocketException e){
		  System.out.println("Could not connect to server. Terminating..");
	  }
	  catch (Exception e){
		  System.out.println("Could not connect to server.. Terminating..");
	  }
      //input.close();
      	
	   
   }
}
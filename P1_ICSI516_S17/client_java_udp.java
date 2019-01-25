/*
 * Student Name: RUJIT RAVAL
 * Albany ID: 001319222
 * Website & Code referred for algorithm: 
 * http://codereview.stackexchange.com/questions/83219/order-of-operations-algorithm-for-calculator
*/

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class client_java_udp {
	public static void main(String args[]) throws Exception {
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		DatagramSocket clientSocket = new DatagramSocket(8095);

		Scanner input = new Scanner(System.in);

		System.out.println("Enter server name or IP address:");
		String ServerName = input.next();

		System.out.println("Enter port:");
		Integer ServerPort = input.nextInt();

		InetAddress IPAddress = InetAddress.getByName(ServerName);

		byte[] sendLength;
		byte[] sendData;

		byte[] receiveData = new byte[1024];
		byte[] receiveACK = new byte[1024];

		System.out.println("Enter expression:" + '\n');
		String sentence = inFromUser.readLine();

		String length = Integer.toString(sentence.length());
		sendLength = length.getBytes();
		//System.out.println("FROM SERVER: " + length);

		DatagramPacket sendPLength = new DatagramPacket(sendLength, sendLength.length, IPAddress, ServerPort);
		clientSocket.send(sendPLength);

		sendData = sentence.getBytes();

		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, ServerPort);

		clientSocket.send(sendPacket);

		DatagramPacket receiveack = new DatagramPacket(receiveACK, receiveACK.length);
		clientSocket.receive(receiveack);

		String ACKCheck = new String(receiveack.getData(), 0, receiveack.getLength());
		//System.out.println("FROM SERVER: " + ACKCheck);

		int count = 0;

		if (ACKCheck != "ACK") {
			while (count < 3) {
				clientSocket.send(sendPacket);
				count++;
			}
		}

		if (count > 3) {
			clientSocket.close();
			System.out.println("Failed to send expression. Terminating.");
		}

		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		//System.out.println("Testing......");


        try {
            clientSocket.receive(receivePacket);
            String ack = "ACK";
            byte[] sendack = ack.getBytes();
            DatagramPacket sendACK = new DatagramPacket(sendack, sendack.length, IPAddress, ServerPort);
            clientSocket.send(sendACK);
        } catch (SocketTimeoutException ste) {
            System.out.println("Result transmission failed. Terminating.");
        }

		String Result = new String(receivePacket.getData(), 0, receivePacket.getLength());
		System.out.println("FROM SERVER: " + Result);

		int ResultValue = Integer.parseInt(Result);
		for (int i = 0; i < ResultValue; i++) {
			System.out.println("Socket Programming.");
		}

		input.close();
		clientSocket.close();

	}
}
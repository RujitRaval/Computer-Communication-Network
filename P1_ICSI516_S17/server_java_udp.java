/*
 * Student Name: RUJIT RAVAL
 * Albany ID: 001319222
 * Website & Code referred for algorithm: 
 * http://codereview.stackexchange.com/questions/83219/order-of-operations-algorithm-for-calculator
*/
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.util.ArrayList;

public class server_java_udp {
	ArrayList<String> contents;
	String item;
	server_java_udp check;

	public static void main(String args[]) throws IOException

	{
		Integer ServerPort = Integer.parseInt(args[0]);
		DatagramSocket serverSocket = new DatagramSocket(ServerPort);
		

		byte[] receiveData = new byte[1024];
		byte[] receiveLen = new byte[1024];
		byte[] receiveACK = new byte[1024];
		byte[] sendData;

		while (true) {

			// FOR LENGTH
			DatagramPacket receiveLength = new DatagramPacket(receiveLen, receiveLen.length);
			serverSocket.receive(receiveLength);
			
			// -----------

			// FOR GETTING EXPRESSION
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			try {
				

				serverSocket.receive(receivePacket);
				InetAddress IPAddress1 = receivePacket.getAddress();
				int port1 = receivePacket.getPort();
				String ack = "ACK";
				byte[] sendack = ack.getBytes();
				DatagramPacket sendACK = new DatagramPacket(sendack, sendack.length, IPAddress1, port1);
				serverSocket.send(sendACK);
			} catch (SocketTimeoutException ste) {
				System.out.println("Did not receive valid expression from client. Terminating.");
			}
            
			String Expression = new String(receivePacket.getData(), 0, receivePacket.getLength());

			InetAddress IPAddress = receivePacket.getAddress();
           // System.out.println("IP ADDRESS:" + IPAddress);
			int port = receivePacket.getPort();

			server_java_udp go = new server_java_udp();
			//
			Expression = go.brackets(Expression);
			//System.out.println("Value of Expression is : " + Expression);

			//
			//
			//
			sendData = Expression.getBytes();

			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);

			DatagramPacket receiveack = new DatagramPacket(receiveACK, receiveACK.length);
			serverSocket.receive(receiveack);

			String ACKCheck = new String(receiveack.getData(), 0, receiveack.getLength());
			int count = 0;

			 if (ACKCheck != "ACK")
			 {
			 while(count<3)
			 {
			 serverSocket.send(sendPacket);
			
			 count++;
			 }
			 }
			
			 if(count>3)
			 {
			 serverSocket.close();
			 System.out.println("Failed to send expression. Terminating.");
			 }
			//
		}
	}

	public String brackets(String s) { // method which deal with brackets
										// separately
		check = new server_java_udp();
		while (s.contains(Character.toString('(')) || s.contains(Character.toString(')'))) {
			for (int o = 0; o < s.length(); o++) {
				try { // i there is not sign
					if ((s.charAt(o) == ')' || Character.isDigit(s.charAt(o))) // between
																				// separate
																				// brackets
							&& s.charAt(o + 1) == '(') { // or number and
															// bracket,
						s = s.substring(0, o + 1) + "*" + (s.substring(o + 1)); // it
																				// treat
																				// it
																				// as
					} // a multiplication
				} catch (Exception ignored) {
				} // ignore out of range ex
				if (s.charAt(o) == ')') { // search for a closing bracket
					for (int i = o; i >= 0; i--) {
						if (s.charAt(i) == '(') { // search for a opening
													// bracket
							String in = s.substring(i + 1, o);
							in = check.recognize(in);
							s = s.substring(0, i) + in + s.substring(o + 1);
							i = o = 0;
						}
					}
				}
			}
			if (s.contains(Character.toString('(')) || s.contains(Character.toString(')'))
					|| s.contains(Character.toString('(')) || s.contains(Character.toString(')'))) {
				System.out.println("Error: incorrect brackets placement");
				return "Error: incorrect brackets placement";
			}
		}
		s = check.recognize(s);
		// System.out.println("Value of Answer is : " +s);
		return s;
	}

	// methods

	public String recognize(String s) { // method divide String on numbers and
										// operators
		PutIt putIt = new PutIt();
		contents = new ArrayList<String>(); // holds numbers and operators
		item = "";
		for (int i = s.length() - 1; i >= 0; i--) { // is scan String from right
													// to left,
			if (Character.isDigit(s.charAt(i))) { // Strings are added to list,
													// if scan finds
				item = s.charAt(i) + item; // a operator, or beginning of String
				if (i == 0) {
					putIt.put();
				}
			} else {
				if (s.charAt(i) == '.') {
					item = s.charAt(i) + item;
				} else if (s.charAt(i) == '-' && (i == 0 || (!Character.isDigit(s.charAt(i - 1))))) {
					item = s.charAt(i) + item; // this part should recognize
					putIt.put(); // negative numbers
				} else {
					putIt.put(); // it add already formed number and
					item += s.charAt(i); // operators to list
					putIt.put(); // as separate Strings
					if (s.charAt(i) == '|') { // add empty String to list,
												// before "|" sign,
						item += " "; // to avoid removing of any meaningful
										// String
						putIt.put(); // in last part of result method
					}
				}
			}
		}
		contents = putIt.result(contents, "^", "|"); // check Strings
		contents = putIt.result(contents, "*", "/"); // for chosen
		contents = putIt.result(contents, "+", "-"); // operators
		return contents.get(0);
	}

	public class PutIt {
		public void put() {
			if (!item.equals("")) {
				contents.add(0, item);
				item = "";
			}
		}

		public ArrayList<String> result(ArrayList<String> arrayList, String op1, String op2) {
			int scale = 10; // controls BigDecimal decimal point accuracy
			BigDecimal result = new BigDecimal(0);
			for (int c = 0; c < arrayList.size(); c++) {
				if (arrayList.get(c).equals(op1) || arrayList.get(c).equals(op2)) {
					if (arrayList.get(c).equals("^")) {
						result = new BigDecimal(arrayList.get(c - 1)).pow(Integer.parseInt(arrayList.get(c + 1)));
					} else if (arrayList.get(c).equals("|")) {
						result = new BigDecimal(Math.sqrt(Double.parseDouble(arrayList.get(c + 1))));
					} else if (arrayList.get(c).equals("*")) {
						result = new BigDecimal(arrayList.get(c - 1)).multiply(new BigDecimal(arrayList.get(c + 1)));
					} else if (arrayList.get(c).equals("/")) {
						result = new BigDecimal(arrayList.get(c - 1)).divide(new BigDecimal(arrayList.get(c + 1)),
								scale, BigDecimal.ROUND_DOWN);
					} else if (arrayList.get(c).equals("+")) {
						result = new BigDecimal(arrayList.get(c - 1)).add(new BigDecimal(arrayList.get(c + 1)));
					} else if (arrayList.get(c).equals("-")) {
						result = new BigDecimal(arrayList.get(c - 1)).subtract(new BigDecimal(arrayList.get(c + 1)));
					}
					try { // in a case of to "out of range" ex
						arrayList.set(c,
								(result.setScale(scale, RoundingMode.HALF_DOWN).stripTrailingZeros().toPlainString()));
						arrayList.remove(c + 1); // it replace the operator with
													// result
						arrayList.remove(c - 1); // and remove used numbers from
													// list
					} catch (Exception ignored) {
					}
				} else {
					continue;
				}
				c = 0; // loop reset, as arrayList changed size
			}
			return arrayList;
		}

	}
}
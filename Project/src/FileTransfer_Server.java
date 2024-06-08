import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileTransfer_Server {
	private static final int PORT = 6969;
	private static final int MAX_CLIENTS = 5;

	public static void main(String[] args) {
		ExecutorService executorService = Executors.newFixedThreadPool(MAX_CLIENTS);

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Server started. Waiting for clients...");

			while (true) {
				Socket clientSocket = serverSocket.accept();
				executorService.submit(new ClientHandler(clientSocket));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class ClientHandler implements Runnable {
		private Socket socket;
		private DataInputStream din;
		private DataOutputStream dout;
		private StringBuilder sb = new StringBuilder();
		private String fileName, accountID, departmentID;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				din = new DataInputStream(socket.getInputStream());
				dout = new DataOutputStream(socket.getOutputStream());

				String clientName;
				int i;
				boolean stopFlag = false;

				dout.writeUTF("Enter your name ");
				clientName = din.readUTF();

				dout.writeUTF("Welcome, " + clientName);

				while (!stopFlag) {
					sb.append("Choose an option\n" +
							"        1. Download File\n" +
							"        2. List Files\n" +
							"        3. Exit");
					dout.writeUTF(sb.toString());

					i = din.readInt();
					sb.setLength(0);

					switch (i) {
						case 1:
							inputFileName();

							File file = new File("Server Files\\" + departmentID + "-" + accountID + ".pdf");
							sendFile(file, accountID, departmentID); // the file if present, is sent
							break;

						case 2:
							listFilesOption();
							break;

						case 3:
							stopFlag = true;
							break;

						default:
							sb.append("Unable to identify option. Please try again!\n");
					}
				}

				socket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		private void inputFileName() {
			try {
				do {
					dout.writeUTF("Enter the FileName which you want to Download\n" +
							"        File Naming Convention:  DepartmentID-AccountID  eg: 1234-123456");

					fileName = din.readUTF();
				} while (!(fileName.length() == 11)); // loops until the input length matches

				departmentID = fileName.substring(0, 4);
				accountID = fileName.substring(fileName.length() - 6);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		private void sendFile(File file, String accountID, String departmentID) {
			try {
				fileName = departmentID + "-" + accountID;
				dout.writeUTF(fileName);

				byte[] byteArray = new byte[(int) file.length()]; // creating byteArray with length same as file length
				dout.writeInt(byteArray.length);

				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
				dout.writeInt(0); // Write flag indicating file is present

				BufferedOutputStream bos = new BufferedOutputStream(dout);

				int count;
				while ((count = bis.read(byteArray)) != -1) {
					bos.write(byteArray, 0, count);
				}

				bos.flush();
				bis.close();

				din.readInt(); // readInt is used to reset if any bytes are present in the buffer after the
								// file transfer
			} catch (FileNotFoundException ex) {
				sb.append(
						"File " + fileName + " Not Found! \n        Please Check the input and try again.\n\n        ");
				try {
					dout.writeInt(1); // Write flag indicating file is absent
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		private void listFilesOption() {
			try {
				File file = new File("Server Files");
				File[] files = file.listFiles();
				int j = 0;
				sb.append("Total Files in folder - " + files.length + "\n");

				listFiles(files);

				while (!(j == 4)) {
					sb.append("Select an Option\n" +
							"        1. Filter Files by Department\n" +
							"        2. Enter Serial No. to Download\n" +
							"        3. Enter FileName to Download\n" +
							"        4. Go To Main Menu");
					dout.writeUTF(sb.toString());

					j = din.readInt();
					sb.setLength(0);

					switch (j) {
						case 1:
							dout.writeUTF("Enter the Department Code to Filter");
							departmentID = din.readUTF().trim();

							if (departmentID.length() < 4)
								departmentID = String.format("%4s", departmentID).replace(' ', '0');
							else if (departmentID.length() > 4)
								departmentID = departmentID.substring(departmentID.length() - 4);

							final String DEP = departmentID;
							files = file.listFiles((no, name) -> name.trim().startsWith(DEP));

							sb.append("Total Files with Department ID [" + DEP + "] - " + files.length + "\n");
							listFiles(files);
							break;

						case 2:
							dout.writeUTF("Enter the Serial No. of File to download ");
							dout.writeInt(files.length);

							int sr_no;
							do {
								sr_no = din.readInt();
							} while (!(sr_no <= files.length && sr_no > 0));

							departmentID = files[sr_no - 1].getName().substring(0, 4);
							accountID = files[sr_no - 1].getName().substring(5, 11);

							file = new File("Server Files\\" + departmentID + "-" + accountID + ".pdf");
							sendFile(file, accountID, departmentID);
							break;

						case 3:
							inputFileName();
							file = new File("Server Files\\" + departmentID + "-" + accountID + ".pdf");
							sendFile(file, accountID, departmentID);
							break;

						case 4:
							break;

						default:
							sb.append("Unable to identify option. Please try again!\n");
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		private void listFiles(File[] files) {
			int k = 0;

			sb.append("\n        +---------+----------------------+\n");
			Formatter formatter = new Formatter(sb, Locale.US);
			formatter.format("        | %-7s | %-20s |\n", "Sr No", "Filename");
			sb.append("        +---------+----------------------+\n");

			for (File f : files) {
				if (!f.isDirectory())
					formatter.format("        | %-7s | %-20s |\n", ++k, f.getName());
			}

			sb.append("        +---------+----------------------+\n\n        ");
			formatter.close();
		}
	}
}

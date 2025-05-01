package chat_server;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class server_frame extends javax.swing.JFrame {
    ArrayList<PrintWriter> clientOutputStreams;
    ArrayList<String> users;
    private Map<String, PrintWriter> userWriters = new HashMap<>();

    public class ClientHandler implements Runnable {
        BufferedReader reader;
        Socket sock;
        PrintWriter client;
        String username;

        public ClientHandler(Socket clientSocket, PrintWriter user) {
            client = user;
            try {
                sock = clientSocket;
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);
            } catch (Exception ex) {
                ta_chat.append("Unexpected error... \n");
            }
        }

        @Override
        public void run() {
            String message, connect = "Connect", disconnect = "Disconnect", 
                   chat = "Chat", whisper = "Whisper";
            String[] data;

            try {
                while ((message = reader.readLine()) != null) {
                    ta_chat.append("Received: " + message + "\n");
                    data = message.split(":", 5);

                    if (data[2].equals(connect)) {
                        this.username = data[0];
                        userWriters.put(data[0], client);
                        tellEveryone((data[0] + ":" + data[1] + ":" + chat));
                        userAdd(data[0]);
                    } 
                    else if (data[2].equals(disconnect)) {
                        tellEveryone((data[0] + ":has disconnected.:" + chat));
                        userRemove(data[0]);
                        userWriters.remove(data[0]);
                    } 
                    else if (data[2].equals(chat)) {
                        tellEveryone(message);
                    } 
                    else if (data[2].equals(whisper) && data.length >= 5) {
                        String recipient = data[1];
                        String sender = data[0];
                        String senderIP = data[3];
                        String whisperMsg = data[4];
                        
                        PrintWriter recipientWriter = userWriters.get(recipient);
                        if (recipientWriter != null) {
                            recipientWriter.println(sender + ":" + recipient + ":Whisper:" + senderIP + ":" + whisperMsg);
                            recipientWriter.flush();
                            ta_chat.append("Whisper sent from " + sender + " (" + senderIP + ") to " + recipient + "\n");
                        } else {
                            ta_chat.append("Whisper failed - recipient " + recipient + " not found\n");
                            client.println("Server:" + sender + ":WhisperFailed:" + recipient);
                            client.flush();
                        }
                        
                        client.println(sender + ":" + recipient + ":Whisper:" + senderIP + ":" + whisperMsg);
                        client.flush();
                    }
                    else if (data[2].equals("file_accept")) {
                        String recipient = data[0]; // original sender
                        String sender = data[1]; // original recipient
                        String fileName = data[3];
                        PrintWriter senderWriter = userWriters.get(recipient);
                        if (senderWriter != null) {
                            senderWriter.println("Server:" + recipient + ":file_accepted:" + fileName);
                            senderWriter.flush();
                        }
                    }
                    else if (data[2].equals("file_reject")) {
                        String recipient = data[0]; // original sender
                        String sender = data[1]; // original recipient
                        String fileName = data[3];
                        PrintWriter senderWriter = userWriters.get(recipient);
                        if (senderWriter != null) {
                            senderWriter.println("Server:" + recipient + ":file_rejected:" + fileName);
                            senderWriter.flush();
                        }
                    }
                    else {
                        ta_chat.append("No Conditions were met. \n");
                    }
                }
            } catch (Exception ex) {
                ta_chat.append("Lost a connection. \n");
                if (username != null) {
                    userWriters.remove(username);
                    tellEveryone((username + ":has disconnected.:" + chat));
                    userRemove(username);
                }
                clientOutputStreams.remove(client);
            }
        }
    }

    public server_frame() {
        initComponents();
    }

    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        ta_chat = new javax.swing.JTextArea();
        b_start = new javax.swing.JButton();
        b_end = new javax.swing.JButton();
        b_users = new javax.swing.JButton();
        b_clear = new javax.swing.JButton();
        lb_name = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat - Server's frame");
        setName("server");
        setResizable(false);

        ta_chat.setColumns(20);
        ta_chat.setRows(5);
        jScrollPane1.setViewportView(ta_chat);

        b_start.setText("START");
        b_start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_startActionPerformed(evt);
            }
        });

        b_end.setText("END");
        b_end.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_endActionPerformed(evt);
            }
        });

        b_users.setText("Online Users");
        b_users.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_usersActionPerformed(evt);
            }
        });

        b_clear.setText("Clear");
        b_clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_clearActionPerformed(evt);
            }
        });

        lb_name.setText("TechWorld3g");
        lb_name.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(b_end, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(b_start, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 291, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(b_clear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(b_users, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lb_name)
                .addGap(209, 209, 209))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(b_start)
                    .addComponent(b_users))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(b_clear)
                    .addComponent(b_end))
                .addGap(4, 4, 4)
                .addComponent(lb_name))
        );

        pack();
    }

    private void b_endActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            Thread.sleep(5000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        tellEveryone("Server:is stopping and all users will be disconnected.:Chat");
        ta_chat.append("Server stopping... \n");
        ta_chat.setText("");
    }

    private void b_startActionPerformed(java.awt.event.ActionEvent evt) {
        Thread starter = new Thread(new ServerStart());
        starter.start();
        ta_chat.append("Server started...\n");
    }

    private void b_usersActionPerformed(java.awt.event.ActionEvent evt) {
        ta_chat.append("\nOnline users:\n");
        for (String current_user : users) {
            ta_chat.append(current_user + "\n");
        }
    }

    private void b_clearActionPerformed(java.awt.event.ActionEvent evt) {
        ta_chat.setText("");
    }

    public class ServerStart implements Runnable {
        @Override
        public void run() {
            clientOutputStreams = new ArrayList<PrintWriter>();
            users = new ArrayList<String>();
    
            try {
                // Get the most relevant IP address
                String serverIP = getServerIP();
                
                // Create server socket
                ServerSocket serverSock = new ServerSocket(2222);
                ta_chat.append("\n=== Server Started ===\n");
                ta_chat.append("Server IP: " + serverIP + "\n");
                ta_chat.append("Listening on port: 2222\n");
                ta_chat.append("Clients should connect to: " + serverIP + ":2222\n\n");
    
                // Start file transfer server
                startFileTransferServer();
                
                // Main chat server loop
                while (true) {
                    Socket clientSock = serverSock.accept();
                    PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
                    clientOutputStreams.add(writer);
    
                    Thread listener = new Thread(new ClientHandler(clientSock, writer));
                    listener.start();
                    ta_chat.append("New connection from: " + 
                        clientSock.getInetAddress().getHostAddress() + "\n");
                }
            } catch (Exception ex) {
                ta_chat.append("Server error: " + ex.getMessage() + "\n");
            }
        }
    
        private String getServerIP() throws SocketException {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (isValidInterface(iface)) {
                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (isValidIP(addr)) {
                            return addr.getHostAddress();
                        }
                    }
                }
            }
            return "127.0.0.1"; // Fallback to localhost if no valid IP found
        }
    
        private boolean isValidInterface(NetworkInterface iface) {
            try {
                return iface.isUp() && !iface.isLoopback() && !iface.isVirtual() &&
                       !iface.getDisplayName().contains("Miniport");
            } catch (SocketException e) {
                return false;
            }
        }
    
        private boolean isValidIP(InetAddress addr) {
            return addr instanceof Inet4Address && 
                   !addr.getHostAddress().startsWith("169.254.");
        }
    
        private void startFileTransferServer() {
            new Thread(() -> {
                try {
                    ServerSocket fileServerSock = new ServerSocket(2223);
                    ta_chat.append("File transfer ready on port 2223\n");
                    
                    while (true) {
                        Socket clientSock = fileServerSock.accept();
                        new Thread(new FileTransferHandler(clientSock)).start();
                    }
                } catch (Exception ex) {
                    ta_chat.append("File server error: " + ex.getMessage() + "\n");
                }
            }).start();
        }
    }
    
    private class FileTransferHandler implements Runnable {
        private Socket sock;
        
        public FileTransferHandler(Socket clientSock) {
            this.sock = clientSock;
        }
        
        @Override
        public void run() {
            try {
                DataInputStream dis = new DataInputStream(sock.getInputStream());
                String header = dis.readUTF();
                
                if (header.contains(":file_transfer:")) {
                    String[] info = header.split(":");
                    String sender = info[0];
                    String recipient = info[1];
                    String fileName = info[3];
                    long fileSize = Long.parseLong(info[4]);
                    
                    // Create downloads directory if it doesn't exist
                    String downloadsPath = System.getProperty("user.home") + "/ChatDownloads/";
                    new File(downloadsPath).mkdirs();
                    
                    String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                    File outFile = new File(downloadsPath + uniqueFileName);
                    
                    // Notify recipient
                    PrintWriter recipientWriter = userWriters.get(recipient);
                    if (recipientWriter != null) {
                        recipientWriter.println(sender + ":" + recipient + ":incoming_file:" + 
                                             fileName + ":" + fileSize);
                        recipientWriter.flush();
                    }
                    
                    // Save file
                    FileOutputStream fos = new FileOutputStream(outFile);
                    byte[] buffer = new byte[1024 * 1024];
                    long totalRead = 0;
                    
                    while (totalRead < fileSize) {
                        int chunkSize = dis.readInt();
                        dis.readFully(buffer, 0, chunkSize);
                        fos.write(buffer, 0, chunkSize);
                        totalRead += chunkSize;
                        
                        // Update progress
                        int progress = (int)((totalRead * 100) / fileSize);
                        ta_chat.append("Receiving " + fileName + " from " + sender + 
                                     ": " + progress + "%\n");
                    }
                    
                    fos.close();
                    ta_chat.append("File saved to: " + outFile.getAbsolutePath() + "\n");
                    
                    // Notify recipient of completed transfer
                    if (recipientWriter != null) {
                        recipientWriter.println(sender + ":" + recipient + ":file_received:" + 
                                             uniqueFileName + ":" + downloadsPath);
                        recipientWriter.flush();
                    }
                }
                
                dis.close();
                sock.close();
            } catch (Exception e) {
                ta_chat.append("File transfer error: " + e.getMessage() + "\n");
            }
        }
    }
    
    public void userAdd(String data) {
        users.add(data);
        broadcastUserList();
    }
    
    public void userRemove(String data) {
        users.remove(data);
        broadcastUserList();
    }
    
    private void broadcastUserList() {
        String userList = "Server::UserList:" + String.join(",", users);
        tellEveryone(userList);
    }
    
    public void tellEveryone(String message) {
        Iterator<PrintWriter> it = clientOutputStreams.iterator();
        while (it.hasNext()) {
            try {
                PrintWriter writer = it.next();
                writer.println(message);
                writer.flush();
                ta_chat.append("Broadcasting: " + message + "\n");
            } catch (Exception ex) {
                ta_chat.append("Error telling everyone. \n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                server_frame frame = new server_frame();
                frame.setVisible(true);
            }
        });
    }

    // Variables declaration
    private javax.swing.JButton b_clear;
    private javax.swing.JButton b_end;
    private javax.swing.JButton b_start;
    private javax.swing.JButton b_users;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lb_name;
    private javax.swing.JTextArea ta_chat;
}
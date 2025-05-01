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
        userListModel = new DefaultListModel<>();
        userList = new javax.swing.JList<>(userListModel);
        userScrollPane = new javax.swing.JScrollPane();
        userScrollPane.setViewportView(userList);
        userScrollPane.setPreferredSize(new java.awt.Dimension(150, 200));

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

            
    // Horizontal layout
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
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 141, Short.MAX_VALUE)
                    .addComponent(userScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(b_clear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(b_users, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE))))
            .addContainerGap())
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lb_name)
            .addGap(209, 209, 209))
    );

    // Vertical layout
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                .addComponent(userScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(b_start)
                .addComponent(b_users))
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(b_clear)
                .addComponent(b_end))
            .addGap(4, 4, 4)
            .addComponent(lb_name)
            ));

            pack();
        }

        private void b_endActionPerformed(java.awt.event.ActionEvent evt) {
            private ServerSocket serverSock;
            private ServerSocket fileServerSock;
            // Notify all clients
            tellEveryone("Server:Server is shutting down:Disconnect");
            
            // Close all client connections
            for (PrintWriter writer : clientOutputStreams) {
                try {
                    writer.close();
                } catch (Exception ex) {
                    ta_chat.append("Error closing client writer: " + ex.getMessage() + "\n");
                }
            }
            clientOutputStreams.clear();
            users.clear();
            userWriters.clear();
            
            // Close server sockets
            if (serverSock != null) {
                try {
                    serverSock.close();
                } catch (IOException ex) {
                    ta_chat.append("Error closing server socket: " + ex.getMessage() + "\n");
                }
            }
            
            if (fileServerSock != null) {
                try {
                    fileServerSock.close();
                } catch (IOException ex) {
                    ta_chat.append("Error closing file server socket: " + ex.getMessage() + "\n");
                }
            }
            
            ta_chat.append("Server stopped.\n");
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
        private static final int BUFFER_SIZE = 1024 * 1024; // 1MB buffer
        private final Socket clientSocket;
        
        public FileTransferHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
        
        @Override
        public void run() {
            try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream())) {
                // Read file transfer header
                String header = dis.readUTF();
                String[] parts = header.split(":", 5); // Split into max 5 parts
                
                if (parts.length >= 5 && parts[2].equals("file_transfer")) {
                    String sender = parts[0];
                    String recipient = parts[1];
                    String fileName = parts[3];
                    long fileSize = Long.parseLong(parts[4]);
                    
                    ta_chat.append(String.format(
                        "Incoming file from %s to %s: %s (%,d bytes)\n", 
                        sender, recipient, fileName, fileSize
                    ));
                    
                    // Verify recipient is online
                    PrintWriter recipientWriter = userWriters.get(recipient);
                    if (recipientWriter == null) {
                        ta_chat.append("File transfer failed: Recipient " + recipient + " not found\n");
                        notifySender(sender, "file_failed:" + recipient + ":User not online");
                        return;
                    }
                    
                    // Create downloads directory if it doesn't exist
                    File downloadsDir = new File(System.getProperty("user.home") + "/ChatDownloads/");
                    if (!downloadsDir.exists()) {
                        downloadsDir.mkdirs();
                    }
                    
                    // Generate unique filename to prevent overwrites
                    String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                    File outputFile = new File(downloadsDir, uniqueFileName);
                    
                    // Notify recipient about incoming file
                    recipientWriter.println(String.format(
                        "%s:%s:incoming_file:%s:%d",
                        sender, recipient, fileName, fileSize
                    ));
                    recipientWriter.flush();
                    
                    // Receive and save the file
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        long totalRead = 0;
                        int progress = 0;
                        
                        while (totalRead < fileSize) {
                            int chunkSize = dis.readInt();
                            if (chunkSize <= 0) break;
                            
                            dis.readFully(buffer, 0, chunkSize);
                            fos.write(buffer, 0, chunkSize);
                            totalRead += chunkSize;
                            
                            // Update progress every 5%
                            int newProgress = (int)((totalRead * 100) / fileSize);
                            if (newProgress >= progress + 5 || totalRead == fileSize) {
                                progress = newProgress;
                                ta_chat.append(String.format(
                                    "Receiving %s: %d%% (%,d/%,d bytes)\n",
                                    fileName, progress, totalRead, fileSize
                                ));
                            }
                        }
                        
                        // Verify complete transfer
                        if (totalRead == fileSize) {
                            ta_chat.append("File saved to: " + outputFile.getAbsolutePath() + "\n");
                            
                            // Notify recipient of completed transfer
                            recipientWriter.println(String.format(
                                "%s:%s:file_received:%s:%s",
                                sender, recipient, uniqueFileName, downloadsDir.getAbsolutePath()
                            ));
                            recipientWriter.flush();
                            
                            // Notify sender of successful transfer
                            notifySender(sender, "file_success:" + recipient + ":" + fileName);
                        } else {
                            ta_chat.append("File transfer incomplete. Expected " + fileSize + 
                                           " bytes, received " + totalRead + " bytes\n");
                            outputFile.delete(); // Delete incomplete file
                            notifySender(sender, "file_failed:" + recipient + ":Transfer incomplete");
                        }
                    }
                }
            } catch (NumberFormatException e) {
                ta_chat.append("Invalid file size format in transfer header\n");
            } catch (IOException e) {
                ta_chat.append("File transfer error: " + e.getMessage() + "\n");
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    ta_chat.append("Error closing file transfer socket: " + e.getMessage() + "\n");
                }
            }
        }
        
        private void notifySender(String sender, String message) {
            PrintWriter senderWriter = userWriters.get(sender);
            if (senderWriter != null) {
                senderWriter.println("Server:" + sender + ":" + message);
                senderWriter.flush();
            }
        }
    }
    
    public void userAdd(String data) {
        users.add(data);
        userListModel.addElement(data);
        broadcastUserList();
    }
    
    public void userRemove(String data) {
        users.remove(data);
        userListModel.removeElement(data);
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
    private javax.swing.JList<String> userList;
    private javax.swing.JScrollPane userScrollPane;
    private DefaultListModel<String> userListModel;
}
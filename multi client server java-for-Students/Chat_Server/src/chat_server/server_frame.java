package chat_server;

import java.io.*;
import java.net.*;
import java.util.*;

public class server_frame extends javax.swing.JFrame {
    ArrayList clientOutputStreams;
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
                    data = message.split(":", 4);

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
                    else if (data[2].equals(whisper) && data.length >= 4) {
                        String recipient = data[1];
                        String sender = data[0];
                        String whisperMsg = data[3];
                        
                        // Send to recipient
                        PrintWriter recipientWriter = userWriters.get(recipient);
                        if (recipientWriter != null) {
                            recipientWriter.println(sender + ":" + recipient + ":Whisper:" + whisperMsg);
                            recipientWriter.flush();
                            ta_chat.append("Whisper sent from " + sender + " to " + recipient + "\n");
                        } else {
                            ta_chat.append("Whisper failed - recipient " + recipient + " not found\n");
                            // Notify sender that whisper failed
                            client.println("Server:" + sender + ":WhisperFailed:" + recipient);
                            client.flush();
                        }
                        
                        // Send back to sender for their own display
                        client.println(sender + ":" + recipient + ":Whisper:" + whisperMsg);
                        client.flush();
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

    @SuppressWarnings("unchecked")
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

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new server_frame().setVisible(true);
            }
        });
    }
    
    public class ServerStart implements Runnable {
        @Override
        public void run() {
            clientOutputStreams = new ArrayList();
            users = new ArrayList();

            try {
                ServerSocket serverSock = new ServerSocket(2222);
                ta_chat.append("Server listening on port 2222...\n");

                while (true) {
                    Socket clientSock = serverSock.accept();
                    PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
                    clientOutputStreams.add(writer);

                    Thread listener = new Thread(new ClientHandler(clientSock, writer));
                    listener.start();
                    ta_chat.append("Got a connection. \n");
                }
            } catch (Exception ex) {
                ta_chat.append("Error making a connection. \n");
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
        Iterator it = clientOutputStreams.iterator();
        while (it.hasNext()) {
            try {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(message);
                writer.flush();
                ta_chat.append("Broadcasting: " + message + "\n");
            } catch (Exception ex) {
                ta_chat.append("Error telling everyone. \n");
            }
        }
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
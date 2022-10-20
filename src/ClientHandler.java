import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler{
    private Server myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String name;

    public String getName(){
        return name;
    }

    public ClientHandler(Server myServer, Socket socket){
        try{
            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            new Thread(() -> {
                try{
                    authentication();
                    readMessages();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("ClientHandler invoke error");
        }
    }

    public void authentication() throws IOException{
        while (true){
            String str = in.readUTF();
            if (str.startsWith("/auth")){
                String[] parts = str.split("\\s");
                String nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                if (nick !=null){
                    if (!myServer.isNickBusy(nick)){
                        sendMsg("/authok " + nick);
                        name = nick;
                        myServer.broadcastMsg(name + "joined the chat.");
                        myServer.subscribe(this);
                        return;
                    }else {
                        sendMsg("Already in use.");
                    }
                }else{
                    sendMsg("Login/Pass incorrect.");
                }
            }
        }
    }

    public void readMessages() throws IOException{
        while (true){
            String strFromClient = in.readUTF();
            if (strFromClient.startsWith("/w")) {
                String[] parts = strFromClient.split(" ");
                String nickTo = parts[1];
                String message = parts[2];

                if (myServer.isNickBusy(nickTo)) {
                    myServer.broadcastMsgToNick(name, parts[1], parts[2]);

                } else {
                    myServer.broadcastMsg(name + ": " + strFromClient);
                }
            } else {
                myServer.broadcastMsg(name + ": " + strFromClient);
            }
            System.out.println("от " + name + ": " + strFromClient);
            if (strFromClient.equals("/end")){
                return;
            }
        }
    }

    public void sendMsg(String msg){
        try{
            out.writeUTF(msg);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMsg(name + " left the chat.");
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
package bgu.spl.net.impl.tftp;

// Added
import java.util.concurrent.ConcurrentHashMap;
import java.util.Vector;

public class holder{

    public static ConcurrentHashMap<Integer, Boolean> ids_login = new ConcurrentHashMap<>();
    public static Vector<String> usernames = new Vector<>();
    private static Integer idCounter = 0;
    private static Object idLock = new Object();

    // // check if client is logged in
    // public static boolean isLoggedin(int id){
    //     boolean output = ids_login.get(id);
    //     return output;
    // }

    // // add a new client to ids_login or update login status
    // public static void addIdsLogin(int id, boolean toLogin){
    //     ids_login.put(id, toLogin);
    // }
    
    // // remove a client from ids_login
    // public static void removeIdsLogin(int id){
    //     ids_login.remove(id);
    // }

    // // broadcast a message to all logged in clients
    // public static void sendToAll(byte[] msg){
    //     ids_login.forEach((key, logged) -> {
    //         if (logged == true){
    //             connections.send(key, msg);
    //         }
    //     });
    // }

    // public static boolean containsUsername(String name){
    //     return usernames.contains(name);
    // }

    // public static void addUsername(String name){
    //     usernames.add(name);
    // }

    public static int getMyId(){
        int output;
        synchronized (idLock) {
            output = idCounter;
            idCounter++;
        }
        return output;
    }
    
}

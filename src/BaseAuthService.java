import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService{
    private class Entry{
        private String login;
        private String pass;
        private String nick;
        public Entry(String login, String pass, String nick){
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }
    private List<Entry> entries;
    @Override
    public void start(){
        System.out.println("Auth is up.");
    }
    @Override
    public void stop() {
        System.out.println("Auth stopped.");
    }
    public BaseAuthService(){
        entries = new ArrayList<>();
        entries.add(new Entry("jay12","123","JayzzerJK"));
        entries.add(new Entry("test","test","TestUser"));
        entries.add(new Entry("amogus","sus","Sus Amogus"));
    }
    @Override
    public String getNickByLoginPass(String login, String pass){
        for (Entry o : entries) {
            if(o.login.equals(login) && o.pass.equals(pass)) return o.nick;
        }
        return null;
    }
}
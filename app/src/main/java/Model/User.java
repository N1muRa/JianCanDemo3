package Model;

/**
 * Created by №zero on 2018/3/11.
 */

public class User {
    private String username;
    private String password;

    public User(String _username, String _password){
        username = _username;
        password = _password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

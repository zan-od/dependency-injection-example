package demo.disample.sample;

import demo.disample.annotations.Column;
import demo.disample.annotations.Id;
import demo.disample.annotations.Table;

@Table(name="users")
public class User {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "username")
    private String username;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString(){
        return "User: " + getUsername() + " (" + getId() + ")";
    }
}

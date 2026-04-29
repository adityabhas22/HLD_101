import java.util.Objects;

public class User {
    private final long id;
    private final String email;

    public User(long id, String email) {
        this.id = id;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof User)) return false;

        User user = (User) other;
        return id == user.id && email.equals(user.email);
    }
}

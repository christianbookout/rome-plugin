package romeplugin.election;

import java.util.UUID;

import romeplugin.empires.role.Role;
import romeplugin.title.Title;

public class Candidate implements Comparable<Candidate> {
    private int votes;
    private final Role role;
    private final UUID uuid;

    public Candidate(UUID uuid, Role role) {
        this.votes = 0;
        this.uuid = uuid;
        this.role = role;
    }

    public int getVotes() {
        return this.votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void addVote() {
        this.votes++;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public Role getRole() {
        return this.role;
    }

    @Override
    public int compareTo(Candidate o) {
        return this.votes - o.votes;
    }

}

package romeplugin.election;

import java.util.UUID;

import romeplugin.title.Title;

public class Candidate implements Comparable<Candidate> {
    private int votes;
    private final Title title;
    private final UUID uuid;

    public Candidate(UUID uuid, Title title) {
        this.votes = 0;
        this.uuid = uuid;
        this.title = title;
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

    public Title getTitle() {
        return this.title;
    }

    @Override
    public int compareTo(Candidate o) {
        return o.votes - this.votes;
    }

}

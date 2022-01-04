package romeplugin.election;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import romeplugin.title.Title;

public class Election {
    private Collection<Candidate> candidates = new ArrayList<>();

    public Candidate getCandidate(UUID uuid) {
        for(Candidate c: candidates) {
            if (c.getUniqueId().equals(uuid)) {
                return c;
            }
        }
        return null;
    }

    public Collection<Candidate> getCandidates() {
        return this.candidates;
    }

    public boolean addCandidate(Candidate candidate) {
        return candidates.add(candidate);
    }

    public boolean removeCandidate(Candidate candidate) {
        return candidates.remove(candidate);
    }

    public boolean vote(UUID uuid) {
        Candidate candidate = getCandidate(uuid);

        if (candidate == null) 
            return false;

        candidate.addVote();
        return true;
    }
    //Go through each candidate, sort them into their respective titles, compare them to eachother under the sectioned lists, and return the winners for each title.
    public Collection<Candidate> endElection() {
        HashMap<Title, Collection<Candidate>> splitCandidates = new HashMap<>(); 
        
        for (Title title: Title.values())
            splitCandidates.put(title, new ArrayList<>());
        
        candidates.forEach(c -> 
            splitCandidates.get(c.getTitle()).add(c)
        );

        Collection<Candidate> winners = new ArrayList<>();

        splitCandidates.values().forEach(collection -> 
            winners.add(Collections.max(collection))
        );

        return winners;
    }
}

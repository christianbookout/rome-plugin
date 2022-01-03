package romeplugin.election;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import romeplugin.newtitle.Title;

public class Election {
    private Collection<Candidate> candidates = new ArrayList<>();

    public Collection<Candidate> getCandidates() {
        return this.candidates;
    }

    public void addCandidate(Candidate candidate) {
        candidates.add(candidate);
    }

    public void removeCandidate(Candidate candidate) {
        candidates.remove(candidate);
    }

    public boolean vote(UUID uuid) {
        Candidate candidate = null;
        for(Candidate c: candidates) {
            if (c.getUniqueId().equals(uuid)) {
                candidate = c;
                break;
            }
        }

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

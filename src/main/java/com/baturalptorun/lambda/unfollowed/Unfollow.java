package com.baturalptorun.lambda.unfollowed;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Checks current followers and compares with the previous run.
 *
 * @author baturalp
 */
public class Unfollow {

    private static final Logger LOG = LoggerFactory.getLogger(Unfollow.class);

    private final Twitter twitter;
    private final StorageManager storage;
    private final String screenName;

    @Inject
    public Unfollow(Twitter twitter, StorageManager storage) {
        this.twitter = twitter;
        this.storage = storage;
        this.screenName = Objects.requireNonNull(getScreenName());
    }

    public Collection<String> getUnfollowers() {
        // Fetch current followers
        LOG.info("Checking followers of @{}", screenName);
        Set<Long> followers = getCurrentFollowers();
        LOG.info("@{} has {} followers", screenName, followers.size());

        // Compare followers with the previous follower list
        Collection<Long> prev = storage.readFile();
        List<String> unfollowedBy = prev.stream()
                .filter(id -> !followers.contains(id))
                .map(id -> {
                    try {
                        return twitter.users().showUser(id).getScreenName();
                    } catch (TwitterException e) {
                        LOG.error("Cannot find the screen name for id=" + id);
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        // Send a dm for all unfollowers
        if (!unfollowedBy.isEmpty())
            sendMessage(unfollowedBy);

        // Write current followers back to S3 file
        storage.writeFile(followers);
        return unfollowedBy;
    }

    private String getScreenName() {
        try {
            return twitter.verifyCredentials().getScreenName();
        } catch (TwitterException e) {
            LOG.error("Cannot verify user.", e);
            throw new RuntimeException(e);
        }
    }

    private Set<Long> getCurrentFollowers() {
        final Set<Long> followers = new TreeSet<>();
        IDs ids = null;
        try {
            do {
                long cursor = (ids == null) ? -1 : ids.getNextCursor();
                ids = twitter.getFollowersIDs(screenName, cursor);
                LOG.info("{} api requests left until rate limiting.",
                        ids.getRateLimitStatus().getRemaining());

                for (long id : ids.getIDs())
                    followers.add(id);

            } while (ids.hasNext());
        } catch (TwitterException e) {
            LOG.error("Cannot get followers.", e);
        }
        return followers;
    }

    private void sendMessage(List<String> unfollowed) {
        String users = unfollowed.stream().map(name -> "@" + name).
                collect(Collectors.joining(", "));
        String txt = "Unfollowed by " + users;

        try {
            twitter.sendDirectMessage(screenName, txt);
            LOG.info("DM sent={}", txt);
        } catch (TwitterException e) {
            LOG.error("Cannot send dm");
            throw new RuntimeException(e);
        }
    }
}

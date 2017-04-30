package me.raymondcai.graphql.model;

import java.util.List;

public interface Actor {
    String getId();
    String getName();
    List<Actor> getFriends();
    List<Episode> getAppearsIn();
}
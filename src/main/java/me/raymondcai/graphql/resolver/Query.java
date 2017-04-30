package me.raymondcai.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLRootResolver;
import me.raymondcai.graphql.datastore.CharacterRepository;
import me.raymondcai.graphql.model.Actor;
import me.raymondcai.graphql.model.Droid;
import me.raymondcai.graphql.model.Episode;
import me.raymondcai.graphql.model.Human;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Query implements GraphQLRootResolver {

    @Autowired
    private CharacterRepository characterRepository;

    public Actor hero(Episode episode) {
        return episode != null ? characterRepository.getHeroes().get(episode) : characterRepository.getCharacters().get("1000");
    }

    public Human human(String id) {
        return (Human) characterRepository.getCharacters().values().stream()
                .filter(character -> character instanceof Human && character.getId().equals(id))
                .findFirst()
                .orElseGet(null);
    }

    public Droid droid(String id) {
        return (Droid) characterRepository.getCharacters().values().stream()
                .filter(character -> character instanceof Droid && character.getId().equals(id))
                .findFirst()
                .orElseGet(null);
    }

    public Actor actor(String id) {
        return characterRepository.getCharacters().get(id);
    }
}

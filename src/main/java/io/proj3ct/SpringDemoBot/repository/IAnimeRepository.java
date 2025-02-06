package io.proj3ct.SpringDemoBot.repository;

import io.proj3ct.SpringDemoBot.entity.Anime;
import org.springframework.data.repository.CrudRepository;

public interface IAnimeRepository extends CrudRepository<Anime, String> {
}

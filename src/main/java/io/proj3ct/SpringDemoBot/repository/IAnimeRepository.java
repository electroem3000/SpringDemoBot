package io.proj3ct.SpringDemoBot.repository;

import io.proj3ct.SpringDemoBot.entity.Anime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IAnimeRepository extends CrudRepository<Anime, String> {
    @Query(value = "SELECT * FROM anime WHERE kind = :kind AND status = :status ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Anime> findByRandom(@Param("kind") String kind, @Param("status") String status);


    @Query("SELECT DISTINCT kind FROM Anime")
    List<String> findAllKinds();

    @Query("SELECT DISTINCT status FROM Anime")
    List<String> findAllStatuses();
}

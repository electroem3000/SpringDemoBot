package io.proj3ct.SpringDemoBot.repository;

import io.proj3ct.SpringDemoBot.entity.BotUser;
import org.springframework.data.repository.CrudRepository;

public interface IUserRepository extends CrudRepository<BotUser, Long> {
}

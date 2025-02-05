package io.proj3ct.SpringDemoBot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
@Entity
@Table(name = "users")
public class BotUser {
    @Id
    private Long chatId;
    private String username;
    private Timestamp registeredAt;
}

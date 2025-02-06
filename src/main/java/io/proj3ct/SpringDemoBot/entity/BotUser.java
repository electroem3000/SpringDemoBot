package io.proj3ct.SpringDemoBot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "users")
public class BotUser {
    @Id
    private Long chatId;
    private String status;
    private String kind;
}

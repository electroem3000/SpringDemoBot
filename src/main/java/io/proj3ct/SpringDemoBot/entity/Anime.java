package io.proj3ct.SpringDemoBot.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
@Table(name = "anime")
public class Anime {
    @Id
    private Long id; // Теперь id берется из JSON, а не генерируется автоматически

    private String name;

    private String russian;

    private String url;
    @Column(length = 20)
    private String kind;

    private double score;
    @Column(length = 40)
    private String status;

    private Integer episodes;

    @JsonProperty("episodes_aired")
    private Integer episodesAired;

    @JsonProperty("aired_on")
    private String airedOn;

    @JsonProperty("released_on")
    private String releasedOn;
}

package com.example.lol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String puuid;
    private String matchId;
    private String queueType;
    private Long gameDurationMinutes;
    private Long gameDurationSeconds;
    private boolean win;
    private Long endTimeStamp;
    private String endTime;
    @OneToOne
    @JoinColumn(name = "my_info_id")
    private MyInfo myInfoDTO;
}

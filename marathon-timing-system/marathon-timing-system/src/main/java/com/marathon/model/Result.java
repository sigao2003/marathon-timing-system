package com.marathon.model;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "results")
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    @Column(name = "total_time", nullable = false)
    private LocalTime totalTime;

    private Boolean valid = true;

    private Integer ranking;

    @Column(name = "age_group_ranking")
    private Integer ageGroupRanking;

    @Column(name = "gender_ranking")
    private Integer genderRanking;

    // 构造函数、getter和setter方法
    public Result() {}

    public Result(Athlete athlete, LocalTime totalTime) {
        this.athlete = athlete;
        this.totalTime = totalTime;
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Athlete getAthlete() { return athlete; }
    public void setAthlete(Athlete athlete) { this.athlete = athlete; }

    public LocalTime getTotalTime() { return totalTime; }
    public void setTotalTime(LocalTime totalTime) { this.totalTime = totalTime; }

    public Boolean getValid() { return valid; }
    public void setValid(Boolean valid) { this.valid = valid; }

    public Integer getRanking() { return ranking; }
    public void setRanking(Integer ranking) { this.ranking = ranking; }

    public Integer getAgeGroupRanking() { return ageGroupRanking; }
    public void setAgeGroupRanking(Integer ageGroupRanking) { this.ageGroupRanking = ageGroupRanking; }

    public Integer getGenderRanking() { return genderRanking; }
    public void setGenderRanking(Integer genderRanking) { this.genderRanking = genderRanking; }
}
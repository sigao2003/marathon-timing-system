package com.marathon.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "race_records")
public class RaceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    @ManyToOne
    @JoinColumn(name = "checkpoint_id", nullable = false)
    private Checkpoint checkpoint;

    @Column(name = "pass_time", nullable = false)
    private LocalDateTime passTime;

    private String duration;

    // 构造函数、getter和setter方法
    public RaceRecord() {}

    public RaceRecord(Athlete athlete, Checkpoint checkpoint, LocalDateTime passTime) {
        this.athlete = athlete;
        this.checkpoint = checkpoint;
        this.passTime = passTime;
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Athlete getAthlete() { return athlete; }
    public void setAthlete(Athlete athlete) { this.athlete = athlete; }

    public Checkpoint getCheckpoint() { return checkpoint; }
    public void setCheckpoint(Checkpoint checkpoint) { this.checkpoint = checkpoint; }

    public LocalDateTime getPassTime() { return passTime; }
    public void setPassTime(LocalDateTime passTime) { this.passTime = passTime; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
}
package com.marathon.model;

import javax.persistence.*;

@Entity
@Table(name = "checkpoints")
public class Checkpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String location;

    @Column(nullable = false)
    private Float distance;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "is_start")
    private Boolean isStart = false;

    @Column(name = "is_finish")
    private Boolean isFinish = false;

    @Column(name = "is_midpoint")
    private Boolean isMidpoint = false;

    // 构造函数、getter和setter方法
    public Checkpoint() {}

    public Checkpoint(String name, String location, Float distance, Integer orderIndex,
                      Boolean isStart, Boolean isFinish, Boolean isMidpoint) {
        this.name = name;
        this.location = location;
        this.distance = distance;
        this.orderIndex = orderIndex;
        this.isStart = isStart;
        this.isFinish = isFinish;
        this.isMidpoint = isMidpoint;
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Float getDistance() { return distance; }
    public void setDistance(Float distance) { this.distance = distance; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public Boolean getIsStart() { return isStart; }
    public void setIsStart(Boolean isStart) { this.isStart = isStart; }

    public Boolean getIsFinish() { return isFinish; }
    public void setIsFinish(Boolean isFinish) { this.isFinish = isFinish; }

    public Boolean getIsMidpoint() { return isMidpoint; }
    public void setIsMidpoint(Boolean isMidpoint) { this.isMidpoint = isMidpoint; }
}
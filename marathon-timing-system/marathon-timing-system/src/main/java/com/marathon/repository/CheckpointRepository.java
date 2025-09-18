package com.marathon.repository;

import com.marathon.model.Checkpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckpointRepository extends JpaRepository<Checkpoint, Long> {

    Optional<Checkpoint> findByName(String name);

    List<Checkpoint> findAllByOrderByOrderIndexAsc();

    @Query("SELECT c FROM Checkpoint c WHERE c.isStart = true")
    Optional<Checkpoint> findStartCheckpoint();

    @Query("SELECT c FROM Checkpoint c WHERE c.isFinish = true")
    Optional<Checkpoint> findFinishCheckpoint();

    @Query("SELECT c FROM Checkpoint c WHERE c.isMidpoint = true")
    List<Checkpoint> findMidpointCheckpoints();

    @Query("SELECT COUNT(c) FROM Checkpoint c")
    long count();
}
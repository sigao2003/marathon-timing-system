package com.marathon.repository;

import com.marathon.model.RaceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RaceRecordRepository extends JpaRepository<RaceRecord, Long> {

    List<RaceRecord> findByAthleteId(Long athleteId);

    List<RaceRecord> findByCheckpointId(Long checkpointId);

    Optional<RaceRecord> findByAthleteIdAndCheckpointId(Long athleteId, Long checkpointId);

    @Query("SELECT rr FROM RaceRecord rr WHERE rr.athlete.id = :athleteId AND " +
            "(rr.checkpoint.isStart = true OR rr.checkpoint.isFinish = true)")
    List<RaceRecord> findStartFinishRecords(@Param("athleteId") Long athleteId);

    @Query("SELECT COUNT(DISTINCT rr.checkpoint.id) FROM RaceRecord rr WHERE rr.athlete.id = :athleteId")
    long countDistinctCheckpointsByAthlete(@Param("athleteId") Long athleteId);

    @Query("SELECT rr FROM RaceRecord rr WHERE rr.passTime BETWEEN :startTime AND :endTime")
    List<RaceRecord> findByPassTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT rr FROM RaceRecord rr WHERE rr.athlete.id = :athleteId " +
            "ORDER BY rr.checkpoint.orderIndex ASC")
    List<RaceRecord> findByAthleteIdOrderByCheckpointOrder(@Param("athleteId") Long athleteId);

    @Query("SELECT rr FROM RaceRecord rr WHERE rr.checkpoint.id = :checkpointId " +
            "ORDER BY rr.passTime ASC")
    List<RaceRecord> findByCheckpointIdOrderByPassTime(@Param("checkpointId") Long checkpointId);
}
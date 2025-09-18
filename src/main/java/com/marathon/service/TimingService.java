package com.marathon.service;

import com.marathon.model.Athlete;
import com.marathon.model.Checkpoint;
import com.marathon.model.RaceRecord;
import com.marathon.model.Result;
import com.marathon.repository.AthleteRepository;
import com.marathon.repository.CheckpointRepository;
import com.marathon.repository.RaceRecordRepository;
import com.marathon.repository.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class TimingService {

    @Autowired
    private RaceRecordRepository raceRecordRepository;

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private ResultRepository resultRepository;

    public void calculateResult(Long athleteId) {
        Optional<Athlete> athleteOpt = athleteRepository.findById(athleteId);
        if (!athleteOpt.isPresent()) {
            return;
        }

        Athlete athlete = athleteOpt.get();

        // 获取起点和终点打卡记录
        List<RaceRecord> startFinishRecords = raceRecordRepository.findStartFinishRecords(athleteId);
        if (startFinishRecords.size() < 2) {
            return;
        }

        RaceRecord startRecord = null;
        RaceRecord finishRecord = null;

        for (RaceRecord record : startFinishRecords) {
            if (record.getCheckpoint().getIsStart()) {
                startRecord = record;
            } else if (record.getCheckpoint().getIsFinish()) {
                finishRecord = record;
            }
        }

        if (startRecord == null || finishRecord == null) {
            return;
        }

        // 计算总时间
        Duration duration = Duration.between(startRecord.getPassTime(), finishRecord.getPassTime());
        LocalTime totalTime = LocalTime.ofNanoOfDay(duration.toNanos());

        // 检查是否通过所有打卡点
        long totalCheckpoints = checkpointRepository.count();
        long athleteCheckpoints = raceRecordRepository.countDistinctCheckpointsByAthlete(athleteId);

        boolean isValid = (athleteCheckpoints == totalCheckpoints);

        // 保存成绩
        Result result = new Result(athlete, totalTime);
        result.setValid(isValid);
        resultRepository.save(result);

        // 更新排名
        updateRankings();
    }

    private void updateRankings() {
        // 更新总排名
        List<Result> allResults = resultRepository.findAllByValidTrueOrderByTotalTimeAsc();
        for (int i = 0; i < allResults.size(); i++) {
            Result result = allResults.get(i);
            result.setRanking(i + 1);
            resultRepository.save(result);
        }

        // 更新性别排名
        updateGenderRankings("男");
        updateGenderRankings("女");

        // 更新年龄组排名
        updateAgeGroupRankings(18, 30);
        updateAgeGroupRankings(31, 45);
        updateAgeGroupRankings(46, 60);
        updateAgeGroupRankings(61, 100);
    }

    private void updateGenderRankings(String gender) {
        List<Result> genderResults = resultRepository.findByAthleteGenderAndValidTrueOrderByTotalTimeAsc(gender);
        for (int i = 0; i < genderResults.size(); i++) {
            Result result = genderResults.get(i);
            result.setGenderRanking(i + 1);
            resultRepository.save(result);
        }
    }

    private void updateAgeGroupRankings(int minAge, int maxAge) {
        List<Result> ageGroupResults = resultRepository.findByAthleteAgeBetweenAndValidTrueOrderByTotalTimeAsc(minAge, maxAge);
        for (int i = 0; i < ageGroupResults.size(); i++) {
            Result result = ageGroupResults.get(i);
            result.setAgeGroupRanking(i + 1);
            resultRepository.save(result);
        }
    }
}

public List<RaceRecord> getAthleteRecords(Long athleteId) {
    return raceRecordRepository.findByAthleteIdOrderByPassTimeAsc(athleteId);
}

public void createManualRecord(String cardId, Long checkpointId, String timestamp) {
    // 实现与MQTT处理类似的逻辑，但不通过MQTT
    Optional<Athlete> athleteOpt = athleteRepository.findByCardId(cardId);
    if (!athleteOpt.isPresent()) {
        throw new RuntimeException("No athlete found with card ID: " + cardId);
    }

    Optional<Checkpoint> checkpointOpt = checkpointRepository.findById(checkpointId);
    if (!checkpointOpt.isPresent()) {
        throw new RuntimeException("No checkpoint found with ID: " + checkpointId);
    }

    Athlete athlete = athleteOpt.get();
    Checkpoint checkpoint = checkpointOpt.get();
    LocalDateTime passTime = LocalDateTime.parse(timestamp);

    RaceRecord record = new RaceRecord(athlete, checkpoint, passTime);
    raceRecordRepository.save(record);

    // 如果是终点打卡，计算成绩
    if (checkpoint.getIsFinish()) {
        calculateResult(athlete.getId());
    }
}

public List<Result> getLeaderboard(Integer limit) {
    return resultRepository.findTop10ByValidTrueOrderByTotalTimeAsc();
}
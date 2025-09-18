package com.marathon.service;

import com.marathon.model.Checkpoint;
import com.marathon.repository.CheckpointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CheckpointService {

    @Autowired
    private CheckpointRepository checkpointRepository;

    public Checkpoint createCheckpoint(Checkpoint checkpoint) {
        // 验证打卡点顺序是否冲突
        validateCheckpointOrder(checkpoint.getOrderIndex());

        return checkpointRepository.save(checkpoint);
    }

    public List<Checkpoint> getAllCheckpoints() {
        return checkpointRepository.findAllByOrderByOrderIndexAsc();
    }

    public Optional<Checkpoint> getCheckpointById(Long id) {
        return checkpointRepository.findById(id);
    }

    public Checkpoint updateCheckpoint(Long id, Checkpoint checkpointDetails) {
        Optional<Checkpoint> checkpointOpt = checkpointRepository.findById(id);
        if (!checkpointOpt.isPresent()) {
            throw new RuntimeException("Checkpoint not found with id: " + id);
        }

        Checkpoint checkpoint = checkpointOpt.get();

        // 如果修改了顺序，需要验证
        if (!checkpoint.getOrderIndex().equals(checkpointDetails.getOrderIndex())) {
            validateCheckpointOrder(checkpointDetails.getOrderIndex());
        }

        checkpoint.setName(checkpointDetails.getName());
        checkpoint.setLocation(checkpointDetails.getLocation());
        checkpoint.setDistance(checkpointDetails.getDistance());
        checkpoint.setOrderIndex(checkpointDetails.getOrderIndex());
        checkpoint.setIsStart(checkpointDetails.getIsStart());
        checkpoint.setIsFinish(checkpointDetails.getIsFinish());
        checkpoint.setIsMidpoint(checkpointDetails.getIsMidpoint());

        return checkpointRepository.save(checkpoint);
    }

    public void deleteCheckpoint(Long id) {
        Optional<Checkpoint> checkpointOpt = checkpointRepository.findById(id);
        if (!checkpointOpt.isPresent()) {
            throw new RuntimeException("Checkpoint not found with id: " + id);
        }

        checkpointRepository.deleteById(id);
    }

    public Optional<Checkpoint> getStartPoint() {
        return checkpointRepository.findByIsStartTrue();
    }

    public Optional<Checkpoint> getFinishPoint() {
        return checkpointRepository.findByIsFinishTrue();
    }

    public List<Checkpoint> getMidPoints() {
        return checkpointRepository.findByIsMidpointTrueOrderByOrderIndexAsc();
    }

    private void validateCheckpointOrder(Integer orderIndex) {
        Optional<Checkpoint> existingCheckpoint = checkpointRepository.findByOrderIndex(orderIndex);
        if (existingCheckpoint.isPresent()) {
            throw new RuntimeException("Checkpoint with order index " + orderIndex + " already exists");
        }
    }
}
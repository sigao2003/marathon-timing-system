package com.marathon.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marathon.model.Athlete;
import com.marathon.model.Checkpoint;
import com.marathon.model.RaceRecord;
import com.marathon.repository.AthleteRepository;
import com.marathon.repository.CheckpointRepository;
import com.marathon.repository.RaceRecordRepository;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class MqttMessageHandler implements MessageHandler {

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Autowired
    private RaceRecordRepository raceRecordRepository;

    @Autowired
    private TimingService timingService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            String payload = (String) message.getPayload();
            System.out.println("Received MQTT message: " + payload);

            // 解析JSON数据
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String cardId = (String) data.get("cardId");
            Integer checkpointId = (Integer) data.get("checkpointId");
            String timestamp = (String) data.get("timestamp");

            // 查找运动员
            Optional<Athlete> athleteOpt = athleteRepository.findByCardId(cardId);
            if (!athleteOpt.isPresent()) {
                System.err.println("No athlete found with card ID: " + cardId);
                return;
            }

            // 查找打卡点
            Optional<Checkpoint> checkpointOpt = checkpointRepository.findById(checkpointId.longValue());
            if (!checkpointOpt.isPresent()) {
                System.err.println("No checkpoint found with ID: " + checkpointId);
                return;
            }

            // 创建打卡记录
            Athlete athlete = athleteOpt.get();
            Checkpoint checkpoint = checkpointOpt.get();
            LocalDateTime passTime = LocalDateTime.parse(timestamp);

            RaceRecord record = new RaceRecord(athlete, checkpoint, passTime);
            raceRecordRepository.save(record);

            System.out.println("Saved timing record for athlete: " + athlete.getName() +
                    " at checkpoint: " + checkpoint.getName());

            // 如果是终点打卡，计算成绩
            if (checkpoint.getIsFinish()) {
                timingService.calculateResult(athlete.getId());
            }

        } catch (Exception e) {
            System.err.println("Error processing MQTT message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
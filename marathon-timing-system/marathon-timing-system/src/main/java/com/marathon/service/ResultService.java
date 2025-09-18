package com.marathon.service;

import com.marathon.model.Athlete;
import com.marathon.model.Result;
import com.marathon.repository.AthleteRepository;
import com.marathon.repository.ResultRepository;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ResultService {

    private final ResultRepository resultRepository;
    private final AthleteRepository athleteRepository;

    @Value("${twilio.account.sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @Autowired
    public ResultService(ResultRepository resultRepository, AthleteRepository athleteRepository) {
        this.resultRepository = resultRepository;
        this.athleteRepository = athleteRepository;
    }

    /**
     * 获取所有成绩
     */
    public List<Result> getAllResults() {
        return resultRepository.findAllByOrderByTotalTimeAsc();
    }

    /**
     * 根据ID获取成绩
     */
    public Optional<Result> getResultById(Long id) {
        return resultRepository.findById(id);
    }

    /**
     * 根据运动员ID获取成绩
     */
    public Optional<Result> getResultByAthleteId(Long athleteId) {
        return resultRepository.findByAthleteId(athleteId);
    }

    /**
     * 根据性别筛选成绩
     */
    public List<Result> getResultsByGender(String gender) {
        return resultRepository.findByAthleteGenderAndValidTrueOrderByTotalTimeAsc(gender);
    }

    /**
     * 根据年龄组筛选成绩
     */
    public List<Result> getResultsByAgeGroup(int minAge, int maxAge) {
        return resultRepository.findByAthleteAgeBetweenAndValidTrueOrderByTotalTimeAsc(minAge, maxAge);
    }

    /**
     * 根据性别和年龄组筛选成绩
     */
    public List<Result> getResultsByGenderAndAgeGroup(String gender, int minAge, int maxAge) {
        return resultRepository.findByAthleteGenderAndAthleteAgeBetweenAndValidTrueOrderByTotalTimeAsc(
                gender, minAge, maxAge);
    }

    /**
     * 获取有效成绩（按总时间排序）
     */
    public List<Result> getValidResults() {
        return resultRepository.findByValidTrueOrderByTotalTimeAsc();
    }

    /**
     * 获取无效成绩
     */
    public List<Result> getInvalidResults() {
        return resultRepository.findByValidFalse();
    }

    /**
     * 更新成绩有效性
     */
    public Result updateResultValidity(Long id, Boolean isValid) {
        Optional<Result> resultOptional = resultRepository.findById(id);

        if (resultOptional.isPresent()) {
            Result result = resultOptional.get();
            result.setValid(isValid);
            return resultRepository.save(result);
        } else {
            throw new RuntimeException("未找到ID为 " + id + " 的成绩");
        }
    }

    /**
     * 导出成绩到CSV
     */
    public void exportResultsToCsv(Writer writer) throws IOException {
        List<Result> results = getAllResults();

        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            // 写入CSV表头
            String[] header = {"排名", "姓名", "性别", "年龄", "成绩", "是否有效", "总排名", "年龄组排名", "性别排名"};
            csvWriter.writeNext(header);

            // 格式化时间
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            // 写入数据
            for (Result result : results) {
                String[] data = {
                        result.getRanking() != null ? result.getRanking().toString() : "N/A",
                        result.getAthlete().getName(),
                        result.getAthlete().getGender(),
                        result.getAthlete().getAge().toString(),
                        result.getTotalTime().format(timeFormatter),
                        result.getValid() ? "是" : "否",
                        result.getRanking() != null ? result.getRanking().toString() : "N/A",
                        result.getAgeGroupRanking() != null ? result.getAgeGroupRanking().toString() : "N/A",
                        result.getGenderRanking() != null ? result.getGenderRanking().toString() : "N/A"
                };
                csvWriter.writeNext(data);
            }
        }
    }

    /**
     * 发送成绩短信通知
     */
    public boolean sendResultSms(Long athleteId) {
        Optional<Result> resultOptional = resultRepository.findByAthleteId(athleteId);

        if (resultOptional.isPresent()) {
            Result result = resultOptional.get();
            Athlete athlete = result.getAthlete();

            if (athlete.getPhone() == null || athlete.getPhone().trim().isEmpty()) {
                throw new RuntimeException("该运动员未提供手机号码");
            }

            // 初始化Twilio
            Twilio.init(twilioAccountSid, twilioAuthToken);

            // 格式化时间
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH小时mm分ss秒");
            String formattedTime = result.getTotalTime().format(timeFormatter);

            // 构建短信内容
            String messageBody = String.format(
                    "【马拉松成绩通知】尊敬的%s，您的马拉松成绩为：%s，总排名：%d，%s组排名：%d，%s排名：%d。感谢您的参与！",
                    athlete.getName(),
                    formattedTime,
                    result.getRanking(),
                    getAgeGroup(athlete.getAge()),
                    result.getAgeGroupRanking(),
                    athlete.getGender(),
                    result.getGenderRanking()
            );

            try {
                // 发送短信
                Message message = Message.creator(
                        new PhoneNumber(athlete.getPhone()),
                        new PhoneNumber(twilioPhoneNumber),
                        messageBody
                ).create();

                return message.getStatus() == Message.Status.SENT;
            } catch (Exception e) {
                throw new RuntimeException("发送短信失败: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("未找到该运动员的成绩记录");
        }
    }

    /**
     * 根据年龄获取年龄组
     */
    private String getAgeGroup(Integer age) {
        if (age >= 18 && age <= 30) {
            return "18-30岁";
        } else if (age >= 31 && age <= 45) {
            return "31-45岁";
        } else if (age >= 46 && age <= 60) {
            return "46-60岁";
        } else {
            return "61岁以上";
        }
    }

    /**
     * 获取前N名成绩
     */
    public List<Result> getTopNResults(int n) {
        return resultRepository.findTopNByValidTrueOrderByTotalTimeAsc(n);
    }

    /**
     * 获取某个性别的排名
     */
    public Integer getGenderRanking(Long athleteId) {
        Optional<Result> result = resultRepository.findByAthleteId(athleteId);
        return result.map(Result::getGenderRanking).orElse(null);
    }

    /**
     * 获取某个年龄组的排名
     */
    public Integer getAgeGroupRanking(Long athleteId) {
        Optional<Result> result = resultRepository.findByAthleteId(athleteId);
        return result.map(Result::getAgeGroupRanking).orElse(null);
    }

    /**
     * 获取平均完赛时间
     */
    public LocalTime getAverageFinishTime() {
        List<Result> validResults = getValidResults();

        if (validResults.isEmpty()) {
            return LocalTime.of(0, 0, 0);
        }

        long totalSeconds = 0;
        for (Result result : validResults) {
            LocalTime time = result.getTotalTime();
            totalSeconds += time.toSecondOfDay();
        }

        long averageSeconds = totalSeconds / validResults.size();
        return LocalTime.ofSecondOfDay(averageSeconds);
    }
}
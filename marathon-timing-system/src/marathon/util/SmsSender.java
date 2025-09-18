package com.marathon.util;

import com.marathon.model.Result;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class SmsSender {
    private static final Logger logger = LoggerFactory.getLogger(SmsSender.class);

    // Twilio账户配置（从application.properties注入）
    @Value("${twilio.account.sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token:}")
    private String twilioAuthToken;

    @Value("${twilio.phone.number:}")
    private String twilioPhoneNumber;

    // 短信模板
    private static final String SMS_TEMPLATE =
            "【马拉松赛事】尊敬的%s，您的成绩：%s，总排名：%d，%s排名：%d。感谢参与！";

    private static final String SMS_TEMPLATE_EN =
            "[Marathon] Dear %s, your result: %s, overall ranking: %d, %s ranking: %d. Thank you for participating!";

    // 模拟发送记录（在实际应用中应使用数据库存储）
    private final Map<Long, String> sentRecords = new HashMap<>();

    /**
     * 初始化Twilio客户端
     */
    public void init() {
        if (isTwilioConfigured()) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            logger.info("Twilio客户端初始化成功");
        } else {
            logger.warn("Twilio未配置，短信功能将无法使用");
        }
    }

    /**
     * 检查Twilio是否已配置
     * @return 是否已配置
     */
    public boolean isTwilioConfigured() {
        return twilioAccountSid != null && !twilioAccountSid.isEmpty() &&
                twilioAuthToken != null && !twilioAuthToken.isEmpty() &&
                twilioPhoneNumber != null && !twilioPhoneNumber.isEmpty();
    }

    /**
     * 发送成绩短信
     * @param result 成绩信息
     * @return 是否发送成功
     */
    public boolean sendResultSms(Result result) {
        if (result == null || result.getAthlete() == null) {
            logger.error("发送短信失败：成绩或运动员信息为空");
            return false;
        }

        String phoneNumber = result.getAthlete().getPhone();
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            logger.warn("运动员{}没有手机号，无法发送短信", result.getAthlete().getName());
            return false;
        }

        // 格式化手机号（假设是中国手机号）
        String formattedPhone = formatPhoneNumber(phoneNumber);
        if (formattedPhone == null) {
            logger.error("手机号格式不正确: {}", phoneNumber);
            return false;
        }

        // 生成短信内容
        String messageContent = generateSmsContent(result);

        // 记录发送尝试
        logger.info("尝试向{}发送短信: {}", formattedPhone, messageContent);

        // 异步发送短信
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                if (isTwilioConfigured()) {
                    // 使用Twilio发送短信
                    Message message = Message.creator(
                            new PhoneNumber(formattedPhone),
                            new PhoneNumber(twilioPhoneNumber),
                            messageContent
                    ).create();

                    logger.info("短信发送成功，SID: {}", message.getSid());
                    sentRecords.put(result.getAthlete().getId(), message.getSid());
                    return true;
                } else {
                    // Twilio未配置，模拟发送成功
                    logger.warn("Twilio未配置，模拟发送短信到: {}", formattedPhone);
                    sentRecords.put(result.getAthlete().getId(), "simulated-" + System.currentTimeMillis());
                    return true;
                }
            } catch (Exception e) {
                logger.error("短信发送失败: {}", e.getMessage(), e);
                return false;
            }
        });

        // 等待发送完成（可以设置超时时间）
        try {
            return future.get();
        } catch (Exception e) {
            logger.error("短信发送异步处理失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量发送成绩短信
     * @param results 成绩列表
     * @return 成功发送的数量
     */
    public int sendBatchResultSms(List<Result> results) {
        if (results == null || results.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Result result : results) {
            if (sendResultSms(result)) {
                successCount++;
            }
            // 添加短暂延迟，避免发送频率过高
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.info("批量发送完成，成功: {}，失败: {}", successCount, results.size() - successCount);
        return successCount;
    }

    /**
     * 生成短信内容
     * @param result 成绩信息
     * @return 短信内容
     */
    private String generateSmsContent(Result result) {
        String athleteName = result.getAthlete().getName();
        String totalTime = new SimpleDateFormat("HH:mm:ss").format(result.getTotalTime());
        int overallRanking = result.getRanking() != null ? result.getRanking() : 0;

        // 确定显示哪种排名（年龄组或性别）
        String rankingType;
        int rankingValue;

        if (result.getAgeGroupRanking() != null) {
            rankingType = "年龄组";
            rankingValue = result.getAgeGroupRanking();
        } else if (result.getGenderRanking() != null) {
            rankingType = "性别";
            rankingValue = result.getGenderRanking();
        } else {
            rankingType = "年龄组";
            rankingValue = 0;
        }

        // 根据运动员信息判断使用中文还是英文模板
        boolean useEnglish = shouldUseEnglishTemplate(result.getAthlete());

        if (useEnglish) {
            return String.format(SMS_TEMPLATE_EN, athleteName, totalTime, overallRanking, rankingType, rankingValue);
        } else {
            return String.format(SMS_TEMPLATE, athleteName, totalTime, overallRanking, rankingType, rankingValue);
        }
    }

    /**
     * 判断是否使用英文模板（简单实现，可根据需要扩展）
     * @param athlete 运动员信息
     * @return 是否使用英文
     */
    private boolean shouldUseEnglishTemplate(Object athlete) {
        // 简单实现：根据姓名是否包含英文字符判断
        // 实际应用中可以根据更多信息判断，如国籍、注册信息等
        if (athlete instanceof com.marathon.model.Athlete) {
            String name = ((com.marathon.model.Athlete) athlete).getName();
            return name != null && name.matches(".*[a-zA-Z].*");
        }
        return false;
    }

    /**
     * 格式化手机号（针对中国手机号）
     * @param phoneNumber 原始手机号
     * @return 格式化后的手机号
     */
    private String formatPhoneNumber(String phoneNumber) {
        // 移除所有非数字字符
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        // 中国手机号校验（11位，以1开头）
        if (cleaned.length() == 11 && cleaned.startsWith("1")) {
            // 添加国际区号
            return "+86" + cleaned;
        }

        // 如果已经是国际格式，直接返回
        if (cleaned.startsWith("+")) {
            return cleaned;
        }

        logger.warn("手机号格式不支持: {}", phoneNumber);
        return null;
    }

    /**
     * 检查是否已向指定运动员发送过短信
     * @param athleteId 运动员ID
     * @return 是否已发送
     */
    public boolean hasSentSms(Long athleteId) {
        return sentRecords.containsKey(athleteId);
    }

    /**
     * 获取短信发送记录ID
     * @param athleteId 运动员ID
     * @return 短信记录ID
     */
    public String getSmsRecordId(Long athleteId) {
        return sentRecords.get(athleteId);
    }

    /**
     * 获取所有发送记录
     * @return 发送记录映射
     */
    public Map<Long, String> getAllSentRecords() {
        return new HashMap<>(sentRecords);
    }

    /**
     * 清除发送记录（用于测试或重置）
     */
    public void clearSentRecords() {
        sentRecords.clear();
        logger.info("已清除所有短信发送记录");
    }
}
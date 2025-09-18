package com.marathon.util;

import com.marathon.model.Result;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Component
public class CsvExporter {

    /**
     * 导出成绩数据到CSV
     * @param results 成绩列表
     * @param writer 输出写入器
     * @throws IOException 写入异常
     */
    public void exportResultsToCsv(List<Result> results, Writer writer) throws IOException {
        try {
            // 定义CSV列映射策略
            ColumnPositionMappingStrategy<Result> mappingStrategy = new ColumnPositionMappingStrategy<>();
            mappingStrategy.setType(Result.class);

            // 定义CSV列顺序
            String[] columns = {
                    "ranking", "athleteName", "athleteGender", "athleteAge",
                    "totalTime", "ageGroupRanking", "genderRanking", "valid"
            };
            mappingStrategy.setColumnMapping(columns);

            // 创建CSV写入器
            StatefulBeanToCsv<Result> beanToCsv = new StatefulBeanToCsvBuilder<Result>(writer)
                    .withMappingStrategy(mappingStrategy)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .build();

            // 写入表头
            writer.write("排名,姓名,性别,年龄,总成绩,年龄组排名,性别排名,成绩有效性\n");

            // 写入数据
            beanToCsv.write(results);

        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new IOException("CSV导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出详细成绩数据到CSV（包含更多字段）
     * @param results 成绩列表
     * @param writer 输出写入器
     * @throws IOException 写入异常
     */
    public void exportDetailedResultsToCsv(List<Result> results, Writer writer) throws IOException {
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            // 写入表头
            String[] header = {
                    "排名", "姓名", "性别", "年龄", "身份证号", "手机号",
                    "总成绩", "年龄组排名", "性别排名", "成绩有效性"
            };
            csvWriter.writeNext(header);

            // 写入数据行
            for (Result result : results) {
                String[] row = {
                        String.valueOf(result.getRanking()),
                        result.getAthlete().getName(),
                        result.getAthlete().getGender(),
                        String.valueOf(result.getAthlete().getAge()),
                        result.getAthlete().getIdCard(),
                        result.getAthlete().getPhone() != null ? result.getAthlete().getPhone() : "",
                        result.getTotalTime().toString(),
                        result.getAgeGroupRanking() != null ? String.valueOf(result.getAgeGroupRanking()) : "",
                        result.getGenderRanking() != null ? String.valueOf(result.getGenderRanking()) : "",
                        result.getValid() ? "有效" : "无效"
                };
                csvWriter.writeNext(row);
            }

        } catch (Exception e) {
            throw new IOException("详细CSV导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出打卡记录到CSV
     * @param records 打卡记录数据（这里使用Object数组，实际应用中应使用具体类型）
     * @param writer 输出写入器
     * @throws IOException 写入异常
     */
    public void exportCheckpointRecordsToCsv(List<Object[]> records, Writer writer) throws IOException {
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            // 写入表头
            String[] header = {
                    "运动员姓名", "打卡点名称", "通过时间", "用时", "打卡点距离(公里)"
            };
            csvWriter.writeNext(header);

            // 写入数据行
            for (Object[] record : records) {
                String[] row = {
                        (String) record[0], // 运动员姓名
                        (String) record[1], // 打卡点名称
                        record[2].toString(), // 通过时间
                        record[3] != null ? record[3].toString() : "", // 用时
                        record[4] != null ? record[4].toString() : "" // 打卡点距离
                };
                csvWriter.writeNext(row);
            }

        } catch (Exception e) {
            throw new IOException("打卡记录CSV导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出运动员列表到CSV
     * @param athletes 运动员数据（这里使用Object数组，实际应用中应使用具体类型）
     * @param writer 输出写入器
     * @throws IOException 写入异常
     */
    public void exportAthletesToCsv(List<Object[]> athletes, Writer writer) throws IOException {
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            // 写入表头
            String[] header = {
                    "姓名", "性别", "年龄", "身份证号", "手机号", "RFID卡号", "注册时间"
            };
            csvWriter.writeNext(header);

            // 写入数据行
            for (Object[] athlete : athletes) {
                String[] row = {
                        (String) athlete[0], // 姓名
                        (String) athlete[1], // 性别
                        athlete[2].toString(), // 年龄
                        (String) athlete[3], // 身份证号
                        athlete[4] != null ? (String) athlete[4] : "", // 手机号
                        (String) athlete[5], // RFID卡号
                        athlete[6].toString() // 注册时间
                };
                csvWriter.writeNext(row);
            }

        } catch (Exception e) {
            throw new IOException("运动员列表CSV导出失败: " + e.getMessage(), e);
        }
    }
}
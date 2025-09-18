package marathon.service;

import marathon.model.Athlete;
import marathon.repository.AthleteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AthleteService {

    private final AthleteRepository athleteRepository;

    @Autowired
    public AthleteService(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    /**
     * 注册新运动员
     */
    public Athlete registerAthlete(Athlete athlete) {
        // 检查卡号是否已存在
        Optional<Athlete> existingAthlete = athleteRepository.findByCardId(athlete.getCardId());
        if (existingAthlete.isPresent()) {
            throw new RuntimeException("该RFID卡号已被注册: " + athlete.getCardId());
        }

        // 检查身份证号是否已存在
        existingAthlete = athleteRepository.findByIdCard(athlete.getIdCard());
        if (existingAthlete.isPresent()) {
            throw new RuntimeException("该身份证号已被注册: " + athlete.getIdCard());
        }

        return athleteRepository.save(athlete);
    }

    /**
     * 获取所有运动员
     */
    public List<Athlete> getAllAthletes() {
        return athleteRepository.findAll();
    }

    /**
     * 根据ID获取运动员
     */
    public Optional<Athlete> getAthleteById(Long id) {
        return athleteRepository.findById(id);
    }

    /**
     * 根据卡号获取运动员
     */
    public Optional<Athlete> getAthleteByCardId(String cardId) {
        return athleteRepository.findByCardId(cardId);
    }

    /**
     * 根据身份证号获取运动员
     */
    public Optional<Athlete> getAthleteByIdCard(String idCard) {
        return athleteRepository.findByIdCard(idCard);
    }

    /**
     * 更新运动员信息
     */
    public Athlete updateAthlete(Long id, Athlete athleteDetails) {
        Optional<Athlete> athleteOptional = athleteRepository.findById(id);

        if (athleteOptional.isPresent()) {
            Athlete athlete = athleteOptional.get();

            // 检查新卡号是否与其他运动员冲突
            if (!athlete.getCardId().equals(athleteDetails.getCardId())) {
                Optional<Athlete> existingWithCardId = athleteRepository.findByCardId(athleteDetails.getCardId());
                if (existingWithCardId.isPresent()) {
                    throw new RuntimeException("该RFID卡号已被其他运动员使用: " + athleteDetails.getCardId());
                }
            }

            // 检查新身份证号是否与其他运动员冲突
            if (!athlete.getIdCard().equals(athleteDetails.getIdCard())) {
                Optional<Athlete> existingWithIdCard = athleteRepository.findByIdCard(athleteDetails.getIdCard());
                if (existingWithIdCard.isPresent()) {
                    throw new RuntimeException("该身份证号已被其他运动员使用: " + athleteDetails.getIdCard());
                }
            }

            athlete.setName(athleteDetails.getName());
            athlete.setGender(athleteDetails.getGender());
            athlete.setAge(athleteDetails.getAge());
            athlete.setCardId(athleteDetails.getCardId());
            athlete.setIdCard(athleteDetails.getIdCard());
            athlete.setPhone(athleteDetails.getPhone());

            return athleteRepository.save(athlete);
        } else {
            throw new RuntimeException("未找到ID为 " + id + " 的运动员");
        }
    }

    /**
     * 删除运动员
     */
    public void deleteAthlete(Long id) {
        Optional<Athlete> athlete = athleteRepository.findById(id);

        if (athlete.isPresent()) {
            athleteRepository.delete(athlete.get());
        } else {
            throw new RuntimeException("未找到ID为 " + id + " 的运动员");
        }
    }

    /**
     * 根据姓名搜索运动员
     */
    public List<Athlete> searchAthletesByName(String name) {
        return athleteRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * 根据性别筛选运动员
     */
    public List<Athlete> getAthletesByGender(String gender) {
        return athleteRepository.findByGender(gender);
    }

    /**
     * 根据年龄范围筛选运动员
     */
    public List<Athlete> getAthletesByAgeRange(Integer minAge, Integer maxAge) {
        return athleteRepository.findByAgeBetween(minAge, maxAge);
    }
}
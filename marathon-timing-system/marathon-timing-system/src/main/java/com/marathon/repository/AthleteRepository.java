package com.marathon.repository;

import com.marathon.model.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AthleteRepository extends JpaRepository<Athlete, Long> {

    Optional<Athlete> findByCardId(String cardId);

    Optional<Athlete> findByIdCard(String idCard);

    List<Athlete> findByGender(String gender);

    List<Athlete> findByAgeBetween(Integer minAge, Integer maxAge);

    @Query("SELECT a FROM Athlete a WHERE a.gender = :gender AND a.age BETWEEN :minAge AND :maxAge")
    List<Athlete> findByGenderAndAgeBetween(
            @Param("gender") String gender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge);

    boolean existsByCardId(String cardId);

    boolean existsByIdCard(String idCard);
}
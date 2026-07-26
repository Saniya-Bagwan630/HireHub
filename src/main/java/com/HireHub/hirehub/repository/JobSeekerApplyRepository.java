package com.HireHub.hirehub.repository;

import com.HireHub.hirehub.entity.JobPostActivity;
import com.HireHub.hirehub.entity.JobSeekerApply;
import com.HireHub.hirehub.entity.JobSeekerProfile;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSeekerApplyRepository extends JpaRepository<JobSeekerApply, Integer> {

    List<JobSeekerApply> findByUserId(JobSeekerProfile userId);

    List<JobSeekerApply> findByJob(JobPostActivity job);
}

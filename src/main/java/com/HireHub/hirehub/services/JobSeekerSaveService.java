package com.HireHub.hirehub.services;

import com.HireHub.hirehub.entity.JobPostActivity;
import com.HireHub.hirehub.entity.JobSeekerProfile;
import com.HireHub.hirehub.entity.JobSeekerSave;
import com.HireHub.hirehub.repository.JobSeekerSaveRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobSeekerSaveService {

    private final JobSeekerSaveRepository jobSeekerSaveRepository;

    public JobSeekerSaveService(JobSeekerSaveRepository jobSeekerSaveRepository) {
        this.jobSeekerSaveRepository = jobSeekerSaveRepository;
    }

    public List<JobSeekerSave> getCandidatesJob(JobSeekerProfile userAccountId) {
        return jobSeekerSaveRepository.findByUserId(userAccountId);
    }

    public List<JobSeekerSave> getJobCandidates(JobPostActivity job) {
        return jobSeekerSaveRepository.findByJob(job);
    }

    public void addNew(JobSeekerSave jobSeekerSave) {
        jobSeekerSaveRepository.save(jobSeekerSave);
    }

    public boolean alreadySaved(JobSeekerProfile userId, JobPostActivity job) {
        return jobSeekerSaveRepository.existsByUserIdAndJob(userId, job);
    }
}
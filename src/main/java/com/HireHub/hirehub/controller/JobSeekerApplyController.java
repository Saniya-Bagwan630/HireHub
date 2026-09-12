package com.HireHub.hirehub.controller;

import com.HireHub.hirehub.entity.*;
import com.HireHub.hirehub.services.*;
import com.HireHub.hirehub.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
public class JobSeekerApplyController {

    private final JobPostActivityService jobPostActivityService;
    private final UsersService usersService;
    private final JobSeekerApplyService jobSeekerApplyService;
    private final JobSeekerSaveService jobSeekerSaveService;
    private final RecruiterProfileService recruiterProfileService;
    private final JobSeekerProfileService jobSeekerProfileService;

    @Autowired
    public JobSeekerApplyController(JobPostActivityService jobPostActivityService, UsersService usersService, JobSeekerApplyService jobSeekerApplyService, JobSeekerSaveService jobSeekerSaveService, RecruiterProfileService recruiterProfileService, JobSeekerProfileService jobSeekerProfileService) {
        this.jobPostActivityService = jobPostActivityService;
        this.usersService = usersService;
        this.jobSeekerApplyService = jobSeekerApplyService;
        this.jobSeekerSaveService = jobSeekerSaveService;
        this.recruiterProfileService = recruiterProfileService;
        this.jobSeekerProfileService = jobSeekerProfileService;
    }

    @GetMapping("job-details-apply/{id}")
    public String display(@PathVariable("id") int id, Model model) {
        JobPostActivity jobDetails = jobPostActivityService.getOne(id);
        List<JobSeekerApply> jobSeekerApplyList = jobSeekerApplyService.getJobCandidates(jobDetails);
        List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getJobCandidates(jobDetails);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))) {
                RecruiterProfile user = recruiterProfileService.getCurrentRecruiterProfile();
                if (user != null) {
                    model.addAttribute("applyList", jobSeekerApplyList);
                }
            } else {
                JobSeekerProfile user = jobSeekerProfileService.getCurrentSeekerProfile();
                if (user != null) {
                    boolean exists = false;
                    boolean saved = false;
                    for (JobSeekerApply jobSeekerApply : jobSeekerApplyList) {
                        if (jobSeekerApply.getUserId() != null && jobSeekerApply.getUserId().getUserAccountId().equals(user.getUserAccountId())) {
                            exists = true;
                            break;
                        }
                    }
                    for (JobSeekerSave jobSeekerSave : jobSeekerSaveList) {
                        if (jobSeekerSave.getUserId() != null && jobSeekerSave.getUserId().getUserAccountId().equals(user.getUserAccountId())) {
                            saved = true;
                            break;
                        }
                    }
                    model.addAttribute("alreadyApplied", exists);
                    model.addAttribute("alreadySaved", saved);
                }
            }
        }

        JobSeekerApply jobSeekerApply = new JobSeekerApply();
        model.addAttribute("applyJob", jobSeekerApply);

        model.addAttribute("jobDetails", jobDetails);
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "job-details";
    }

    @PostMapping("job-details/apply/{id}")
    public String apply(@PathVariable("id") int id,
                        @RequestParam(value = "resumeOption", required = false, defaultValue = "existing") String resumeOption,
                        @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
                        @RequestParam(value = "coverLetter", required = false) String coverLetter) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String currentUsername = authentication.getName();
        Optional<Users> userOpt = usersService.getUserByEmail(currentUsername);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        Users user = userOpt.get();
        Optional<JobSeekerProfile> seekerProfileOpt = jobSeekerProfileService.getOne(user.getUserId());
        if (seekerProfileOpt.isEmpty()) {
            return "redirect:/dashboard/";
        }

        JobSeekerProfile seekerProfile = seekerProfileOpt.get();

        JobPostActivity jobPostActivity = null;
        try {
            jobPostActivity = jobPostActivityService.getOne(id);
        } catch (Exception e) {
            return "redirect:/dashboard/";
        }

        if (jobPostActivity == null) {
            return "redirect:/dashboard/";
        }

        // Prevent duplicate applications
        if (jobSeekerApplyService.alreadyApplied(seekerProfile, jobPostActivity)) {
            return "redirect:/job-details-apply/" + id;
        }

        String selectedResume = null;
        boolean hasExistingResume = StringUtils.hasText(seekerProfile.getResume());

        if ("upload".equalsIgnoreCase(resumeOption) || !hasExistingResume) {
            if (resumeFile == null || resumeFile.isEmpty()) {
                return "redirect:/job-details-apply/" + id;
            }

            String originalFilename = resumeFile.getOriginalFilename();
            if (originalFilename == null || !isValidResumeExtension(originalFilename) || resumeFile.getSize() > 10 * 1024 * 1024) {
                return "redirect:/job-details-apply/" + id;
            }

            String fileName = StringUtils.cleanPath(Objects.requireNonNull(originalFilename));
            String uploadDir = "photos/candidate/" + seekerProfile.getUserAccountId();

            try {
                FileUploadUtil.saveFile(uploadDir, fileName, resumeFile);
                selectedResume = fileName;
            } catch (IOException ex) {
                return "redirect:/job-details-apply/" + id;
            }
        } else {
            selectedResume = seekerProfile.getResume();
        }

        JobSeekerApply jobSeekerApply = new JobSeekerApply();
        jobSeekerApply.setUserId(seekerProfile);
        jobSeekerApply.setJob(jobPostActivity);
        jobSeekerApply.setApplyDate(new Date());
        jobSeekerApply.setCoverLetter(coverLetter);
        jobSeekerApply.setResume(selectedResume);

        jobSeekerApplyService.addNew(jobSeekerApply);

        return "redirect:/job-details-apply/" + id;
    }

    private boolean isValidResumeExtension(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx");
    }
}
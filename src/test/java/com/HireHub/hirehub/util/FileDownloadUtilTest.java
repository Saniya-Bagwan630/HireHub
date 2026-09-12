package com.HireHub.hirehub.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FileDownloadUtilTest {

    @Test
    void shouldRejectNullOrBlankArguments() throws IOException {
        FileDownloadUtil util = new FileDownloadUtil();
        assertNull(util.getFileAsResourse(null, "sample.pdf"));
        assertNull(util.getFileAsResourse("photos/candidate/1", null));
        assertNull(util.getFileAsResourse("", "sample.pdf"));
        assertNull(util.getFileAsResourse("photos/candidate/1", "   "));
    }

    @Test
    void shouldRejectPathTraversalInFileName() throws IOException {
        FileDownloadUtil util = new FileDownloadUtil();
        assertNull(util.getFileAsResourse("photos/candidate/1", "../application.properties"));
        assertNull(util.getFileAsResourse("photos/candidate/1", "..\\application.properties"));
        assertNull(util.getFileAsResourse("photos/candidate/1", "/etc/passwd"));
    }

    @Test
    void shouldRejectPathTraversalInDirectory() throws IOException {
        FileDownloadUtil util = new FileDownloadUtil();
        assertNull(util.getFileAsResourse("photos/candidate/../../", "sample.pdf"));
        assertNull(util.getFileAsResourse("photos/candidate/../1", "sample.pdf"));
    }

    @Test
    void shouldSuccessfullyRetrieveValidCandidateFile() throws IOException {
        Path baseCandidateDir = Paths.get("photos", "candidate", "test-user-999").toAbsolutePath().normalize();
        Files.createDirectories(baseCandidateDir);
        Path sampleFile = baseCandidateDir.resolve("resume.pdf");
        Files.writeString(sampleFile, "Sample Resume Content");

        try {
            FileDownloadUtil util = new FileDownloadUtil();
            Resource resource = util.getFileAsResourse("photos/candidate/test-user-999", "resume.pdf");

            assertNotNull(resource);
            assertTrue(resource.exists());
            assertEquals("resume.pdf", resource.getFilename());
        } finally {
            Files.deleteIfExists(sampleFile);
            Files.deleteIfExists(baseCandidateDir);
        }
    }
}

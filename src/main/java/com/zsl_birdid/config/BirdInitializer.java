package com.zsl_birdid.config;

import com.zsl_birdid.domain.Bird;
import com.zsl_birdid.Repo.BirdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This component initializes bird data when the application starts up.
 * It scans a specified directory for bird information and updates or inserts
 * records into the database accordingly.
 */
@Component
public class BirdInitializer implements ApplicationRunner {

    private final String baseDirectory = "C:\\Birds";  // Base directory for bird files

    @Autowired
    private BirdRepository birdRepository;

    /**
     * This method is called when the Spring Boot application starts.
     * It scans the base directory for bird data and updates the database.
     */
    @Override
    public void run(ApplicationArguments args) {
        Path baseDirPath = Paths.get(baseDirectory);
        File[] birdDirs = baseDirPath.toFile().listFiles(File::isDirectory);

        if (birdDirs != null) {
            for (File birdDir : birdDirs) {

                String birdName = birdDir.getName();

                // Check if the bird already exists in the database
                Bird existingBird = birdRepository.findByBirdName(birdName);

                if (existingBird == null) {
                    // Create a new Bird entity and save it to the database
                    Bird bird = createBirdEntity(birdDir, birdName);
                    birdRepository.save(bird);
                } else {
                    // Update existing bird with new options
                    updateBirdOptions(existingBird, birdDir);
                }
            }
        }
    }

    /**
     * Creates a Bird entity from the given directory and bird name.
     *
     * @param birdDir   Directory containing bird files
     * @param birdName  Name of the bird
     * @return          A new Bird entity
     */
    private Bird createBirdEntity(File birdDir, String birdName) {

        // Read main audio file
        File[] files = birdDir.listFiles();
        File mainAudioFile = findFileByExtension(files, ".wav");  // Try .wav for audio
        if (mainAudioFile == null) {
            mainAudioFile = findFileByExtension(files, ".mp3");  // Fall back to .mp3 if .wav is not found
        }
        File imageFile = findFileByExtension(files, ".jpg");  // Look for image file (.jpg)

        File mainVideoFile = findFileByExtension(files, ".mp4");  // Look for video file (.mp4)

        Bird bird = new Bird();
        bird.setBirdName(birdName);
        if (mainAudioFile != null) {
            bird.setMediaUrl(relativePath(mainAudioFile.toPath()));
        }
        if (imageFile != null) {
            bird.setImageUrl(relativePath(imageFile.toPath()));
        }
        if (mainVideoFile != null) {
            bird.setVideoUrl(relativePath(mainVideoFile.toPath()));
        }
        bird.setMain(true);

        // Add options if available
        addOptionsToBird(bird, birdDir);

        return bird;
    }

    /**
     * Updates an existing Bird entity with new options from the directory.
     *
     * @param birdDir   Directory containing new options
     * @param bird      Existing Bird entity to be updated
     */
    private void updateBirdOptions(Bird bird, File birdDir) {
        // Add new options to the existing bird
        addOptionsToBird(bird, birdDir);

        // Save the updated bird entity
        birdRepository.save(bird);
    }

    /**
     * Adds option files from the directory to the Bird entity.
     *
     * @param bird      Bird entity to which options will be added
     * @param birdDir   Directory containing option files
     */
    private void addOptionsToBird(Bird bird, File birdDir) {
        File optionsDir = new File(birdDir, "options");
        if (optionsDir.exists() && optionsDir.isDirectory()) {
            File[] optionFiles = optionsDir.listFiles();
            if (optionFiles != null) {
                for (File optionFile : optionFiles) {

                    // Check if this option already exists in the bird's options
                    boolean optionExists = bird.getOptions().stream()
                            .anyMatch(opt -> opt.getMediaUrl().equals(relativePath(optionFile.toPath())));

                    if (!optionExists) {
                        // Create a new Bird entity for this option
                        Bird optionBird = new Bird();
                        optionBird.setBirdName(bird.getBirdName());  // Same name as parent
                        optionBird.setMediaUrl(relativePath(optionFile.toPath()));
                        optionBird.setImageUrl(bird.getImageUrl());  // Use the same image as parent
                        optionBird.setMain(false);

                        // Save the option bird
                        birdRepository.save(optionBird);

                        // Add the option bird to the parent bird's options
                        bird.getOptions().add(optionBird);
                    }
                }
            }
        }
    }

    /**
     * Finds a file with a specific extension in an array of files.
     *
     * @param files         Array of files to search
     * @param extension     Extension to look for
     * @return              File with the specified extension, or null if not found
     */
    private File findFileByExtension(File[] files, String extension) {
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(extension)) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * Converts a full file path to a relative path based on the base directory.
     *
     * @param fullPath  Full file path
     * @return          Relative path as a string
     */
    private String relativePath(Path fullPath) {
        Path basePath = Paths.get(baseDirectory);
        return basePath.relativize(fullPath).toString().replace(File.separatorChar, '/');
    }
}

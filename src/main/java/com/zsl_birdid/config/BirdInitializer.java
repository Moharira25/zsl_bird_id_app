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

@Component
public class BirdInitializer implements ApplicationRunner {

    private final String baseDirectory = "C:\\Birds";  // Base directory for bird files

    @Autowired
    private BirdRepository birdRepository;

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
                    // Create Bird entity and save to DB
                    Bird bird = createBirdEntity(birdDir, birdName);
                    birdRepository.save(bird);
                } else {
                    // Update existing bird with new options
                    updateBirdOptions(existingBird, birdDir);
                }
            }
        }
    }

    private Bird createBirdEntity(File birdDir, String birdName) {


        // Read main audio file
        File[] files = birdDir.listFiles();
        File mainAudioFile = findFileByExtension(files, ".wav");  // Assuming .wav for audio
        File imageFile = findFileByExtension(files, ".jpg");  // Assuming .jpg for image

        Bird bird = new Bird();
        bird.setBirdName(birdName);
        if (mainAudioFile != null) {
            bird.setMediaUrl(relativePath(mainAudioFile.toPath()));
        }
        if (imageFile != null) {
            bird.setImageUrl(relativePath(imageFile.toPath()));
        }
        bird.setMain(true);

        // Add options if available
        addOptionsToBird(bird, birdDir);

        return bird;
    }

    private void updateBirdOptions(Bird bird, File birdDir) {
        // Add new options to the existing bird
        addOptionsToBird(bird, birdDir);

        // Save the updated bird entity
        birdRepository.save(bird);
    }

    private void addOptionsToBird(Bird bird, File birdDir){
        File optionsDir = new File(birdDir, "options");
        if (optionsDir.exists() && optionsDir.isDirectory()) {
            File[] optionFiles = optionsDir.listFiles();
            if (optionFiles != null) {
                for (File optionFile : optionFiles) {
                    //String optionFileName = optionFile.getName();

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

    private String relativePath(Path fullPath) {
        Path basePath = Paths.get(baseDirectory);
        return basePath.relativize(fullPath).toString().replace(File.separatorChar, '/');
    }
}

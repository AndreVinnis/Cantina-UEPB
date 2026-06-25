package com.uepb.CoreService.services;

import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.dto.CafeteriaRequest;
import com.uepb.CoreService.enums.UserRole;
import com.uepb.CoreService.exceptions.EmailAlreadyExistException;
import com.uepb.CoreService.exceptions.ShortPasswordException;
import com.uepb.CoreService.repository.CafeteriaRepository;
import com.uepb.CoreService.utils.StorageImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class CafeteriaService {

    @Autowired
    private CafeteriaRepository cafeteriaRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private StorageImageService imageService;

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Value("${app.diretorio.imagens}")
    private String imageDirectory;

    @Transactional
    public Cafeteria createCafeteria(CafeteriaRequest newCafeteria){
        if(cafeteriaRepository.findByEmail(newCafeteria.email()) != null){
            throw new EmailAlreadyExistException(newCafeteria.email());
        }
        if (!isValidEmail(newCafeteria.email())) {
            throw new IllegalArgumentException("Formato de e-mail inválido.");
        }
        if(newCafeteria.password().length() < 8){
            throw new ShortPasswordException();
        }

        Cafeteria cafeteria = Cafeteria.builder()
                .name(newCafeteria.name())
                .email(newCafeteria.email())
                .hashPassword(encoder.encode(newCafeteria.password()))
                .role(UserRole.USER)
                .build();

        return cafeteriaRepository.save(cafeteria);
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public String saveImage(Cafeteria cafeteria, MultipartFile file) {
        String subfolder = "cafeterias/";
        String imagePath = imageService.saveImage(file, subfolder, cafeteria.getId(), cafeteria.getName());
        cafeteria.setImageUrl(imagePath);
        cafeteriaRepository.save(cafeteria);
        return imagePath;
    }
}

package com.uepb.CoreService.services;

import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.dto.request.CafeteriaRequest;
import com.uepb.CoreService.dto.response.CafeteriaResponse;
import com.uepb.CoreService.dto.response.MenuItemResponse;
import com.uepb.CoreService.enums.Campus;
import com.uepb.CoreService.enums.UserRole;
import com.uepb.CoreService.exceptions.*;
import com.uepb.CoreService.repository.CafeteriaRepository;
import com.uepb.CoreService.utils.StorageImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CafeteriaService {

    @Autowired
    private CafeteriaRepository cafeteriaRepository;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private StorageImageService imageService;

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

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
        List<Cafeteria> cafeterias = cafeteriaRepository.findByCampus(newCafeteria.campus());
        for(Cafeteria cafeteria : cafeterias){
            if(cafeteria.getName().equals(newCafeteria.name())){
                throw new NameAlreadyExist(newCafeteria.name());
            }
        }

        Cafeteria cafeteria = Cafeteria.builder()
                .name(newCafeteria.name())
                .email(newCafeteria.email())
                .hashPassword(encoder.encode(newCafeteria.password()))
                .campus(newCafeteria.campus())
                .role(UserRole.USER)
                .build();

        return cafeteriaRepository.save(cafeteria);
    }

    public CafeteriaResponse getMyCafeteria(String email){
        Cafeteria cafeteria = (Cafeteria) cafeteriaRepository.findByEmail(email);

        if(cafeteria == null){
            throw new CafeteriaNotFound(email);
        }

        return toResponse(cafeteria);
    }

    public CafeteriaResponse updateCafeteria(String email, CafeteriaRequest newCafeteria){
        Cafeteria cafeteria = (Cafeteria) cafeteriaRepository.findByEmail(email);

        if(cafeteria == null){
            throw new CafeteriaNotFound(email);
        }

        if(newCafeteria.name() != null){
            cafeteria.setName(newCafeteria.name());
        }
        if(newCafeteria.email() != null){
            cafeteria.setEmail(newCafeteria.email());
        }
        if(newCafeteria.password() != null){
            cafeteria.setHashPassword(encoder.encode(newCafeteria.password()));
        }
        cafeteria = cafeteriaRepository.save(cafeteria);
        return toResponse(cafeteria);
    }

    public void delete(String email){
        Cafeteria cafeteria = (Cafeteria) cafeteriaRepository.findByEmail(email);

        if(cafeteria == null){
            throw new CafeteriaNotFound(email);
        }

        cafeteriaRepository.delete(cafeteria);
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

    public List<CafeteriaResponse> getCafeteriaByCampus(Campus campus){
        List<Cafeteria> cafeterias = cafeteriaRepository.findByCampus(campus);
        if(cafeterias.isEmpty()){
            throw new NoCafeteriaFound();
        }

        List<CafeteriaResponse> cafeteriasResponses = new ArrayList<>();
        for(Cafeteria cafeteria: cafeterias){
            cafeteriasResponses.add(toResponse(cafeteria));
        }
        return cafeteriasResponses;
    }

    public List<MenuItemResponse> getItemsForCafeteria(String name, Campus campus){
        Cafeteria cafeteria = cafeteriaRepository.findByNameAndCampus(name, campus).orElseThrow(
                () -> new CafeteriaNotFound(name)
        );

        return menuItemService.getMenuItemsForCafeteria(cafeteria.getId());
    }

    private CafeteriaResponse toResponse(Cafeteria cafeteria){
        return new CafeteriaResponse(
                cafeteria.getName(),
                cafeteria.getEmail(),
                cafeteria.isActive(),
                cafeteria.getImageUrl()
        );
    }
}

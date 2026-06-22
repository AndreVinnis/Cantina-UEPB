package com.uepb.CoreService.services;

import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.dto.CafeteriaRequest;
import com.uepb.CoreService.enums.UserRole;
import com.uepb.CoreService.exceptions.EmailAlreadyExistException;
import com.uepb.CoreService.repository.CafeteriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CafeteriaService {

    @Autowired
    CafeteriaRepository cafeteriaRepository;

    @Autowired
    PasswordEncoder encoder;

    @Transactional
    public Cafeteria createCafeteria(CafeteriaRequest newCafeteria){
        if(cafeteriaRepository.findByEmail(newCafeteria.email()) != null){
            throw new EmailAlreadyExistException(newCafeteria.email());
        }

        Cafeteria cafeteria = Cafeteria.builder()
                .name(newCafeteria.name())
                .email(newCafeteria.email())
                .hashPassword(encoder.encode(newCafeteria.password()))
                .role(UserRole.USER)
                .build();

        System.out.println(cafeteria.getEmail());
        return cafeteriaRepository.save(cafeteria);
    }
}

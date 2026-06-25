package com.uepb.CoreService.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class StorageImageService {

    @Value("${app.diretorio.raiz-imagens}")
    private String rootDirectory;

    public String saveImage(MultipartFile file, String subFolder, String referenceId, String name) {
        if (file.isEmpty()) throw new RuntimeException("Arquivo vazio.");

        try {
            // Concatena a raiz com a subpasta (ex: /dados/imagens/ + cafeterias/)
            Path directoryPath = Paths.get(rootDirectory, subFolder);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String fileName = referenceId + extension;

            Path filePath = directoryPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Retorna o caminho relativo para salvar no banco
            return "/imagens/" + subFolder + name + "-" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar a imagem", e);
        }
    }
}

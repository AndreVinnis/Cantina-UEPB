package com.uepb.CoreService.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageImageServiceTest {

    @InjectMocks
    private StorageImageService storageImageService;

    @Mock
    private MultipartFile multipartFile;

    // Cria um diretório temporário real para cada teste
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageImageService, "rootDirectory", tempDir.toString());
    }

    @Test
    void deveLancarExcecaoQuandoArquivoEstiverVazio() {
        // Arrange
        when(multipartFile.isEmpty()).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                storageImageService.saveImage(multipartFile, "cafeterias/", "123", "cafe-central")
        );

        assertEquals("Arquivo vazio.", exception.getMessage());
        verify(multipartFile, never()).getOriginalFilename();
    }

    @Test
    void deveSalvarImagemComSucessoCriandoSubpasta() throws IOException {
        // Arrange
        String subFolder = "cafeterias/";
        String referenceId = "ref123";
        String name = "cafe";
        String originalFilename = "imagem_teste.jpg";

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);

        byte[] fileContent = "conteudo da imagem fake".getBytes();
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));

        // Act
        String resultPath = storageImageService.saveImage(multipartFile, subFolder, referenceId, name);

        // Assert
        String expectedRelativePath = "/imagens/cafeterias/cafe-ref123.jpg";
        assertEquals(expectedRelativePath, resultPath);
        Path expectedFilePath = tempDir.resolve("cafeterias").resolve("ref123.jpg");
        assertTrue(Files.exists(expectedFilePath), "O arquivo deveria ter sido salvo no disco");
        assertArrayEquals(fileContent, Files.readAllBytes(expectedFilePath));
    }

    @Test
    void deveLancarExcecaoQuandoOcorrerErroDeIO() throws IOException {
        // Arrange
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("imagem.png");

        when(multipartFile.getInputStream()).thenThrow(new IOException("Erro forçado no disco"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                storageImageService.saveImage(multipartFile, "produtos/", "456", "bolo")
        );

        assertEquals("Erro ao salvar a imagem", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals(IOException.class, exception.getCause().getClass());
    }
}
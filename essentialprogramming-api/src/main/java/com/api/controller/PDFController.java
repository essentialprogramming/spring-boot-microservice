package com.api.controller;

import com.api.config.Anonymous;
import com.api.entities.User;
import com.api.template.Templates;
import com.google.common.collect.ImmutableMap;
import com.template.service.TemplateService;
import com.util.enums.Language;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(description = "Pdf API", name = "Download PDF")
@RequestMapping("/pdf")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PDFController {

    private final ApplicationContext applicationContext;

    private final TemplateService templateService;

    @PostMapping(consumes =  {"application/json"}, produces = {"application/octet-stream"})
    @Operation(summary = "Generate PDF",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Demo PDF.",
                            content = @Content(mediaType = "application/octet-stream",
                                    schema = @Schema(implementation = String.class)))
            })
    @Anonymous
    public ResponseEntity<byte[]> generatePDF() {
        final String fileName = String.format("pdf-example-%s.%s",
                LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("MM-dd-yyyy-HH-mm")),
                "pdf");

        byte[] payload = templateService.generatePDF(Templates.PDF_EXAMPLE, generateTemplateVariables(), applicationContext.getBean(Language.class).getLocale());

        return ResponseEntity.ok()
                .header("content-disposition", "attachment; filename=" + fileName + "; filename*=UTF-8''" + fileName)
                .body(payload);
    }

    private Map<String, Object> generateTemplateVariables() {
        final User firstUser = new User();
        firstUser.setFirstName("Razvan");
        firstUser.setLastName("Prichici");
        firstUser.setEmail("razvanpaulp@gmail.com");
        firstUser.setCreatedDate(LocalDateTime.now());
        firstUser.setLanguage(generateLanguage());

        final User secondUser = new User();
        secondUser.setFirstName("Roger");
        secondUser.setLastName("Federer");
        secondUser.setEmail("roger@atp.com");
        secondUser.setCreatedDate(LocalDateTime.now());

        final List<User> users = new ArrayList<>();
        users.add(firstUser);
        users.add(secondUser);

        return ImmutableMap.<String, Object>builder()
                .put("users", users)
                .build();
    }

    private static com.api.entities.Language generateLanguage() {
        com.api.entities.Language language = new com.api.entities.Language();
        language.setId(1);
        language.setName("english");
        language.setDisplayOrder(2);
        language.setSymbol("EN");
        return language;
    }
}

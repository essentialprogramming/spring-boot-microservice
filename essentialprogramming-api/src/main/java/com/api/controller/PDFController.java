package com.api.controller;

import com.api.config.Anonymous;
import com.api.entities.Language;
import com.api.entities.User;
import com.api.service.UserService;
import com.api.template.Templates;
import com.google.inject.internal.util.ImmutableMap;
import com.template.service.TemplateService;
import com.util.web.SmartLocaleResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(description = "Pdf API", name = "Download PDF")
@RequestMapping("/pdf")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
public class PDFController {

    private final SmartLocaleResolver smartLocaleResolver;

    private final TemplateService templateService;
    private final UserService userService;

    @PostMapping(produces = {"application/octet-stream"})
    @Operation(summary = "Generate PDF",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Demo PDF.",
                            content = @Content(mediaType = "application/octet-stream",
                                    schema = @Schema(implementation = String.class)))
            })
    @Anonymous
    public ResponseEntity<Resource> generatePDF(HttpServletRequest request) {
        final String fileName = String.format("pdf-example-%s.%s",
                LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("MM-dd-yyyy-HH-mm")),
                "pdf");

        byte[] payload = templateService.generatePDF(Templates.PDF_EXAMPLE, generateTemplateVariables(), smartLocaleResolver.resolveLocale(request));
        ByteArrayResource resource = new ByteArrayResource(payload);

        return ResponseEntity.ok()
                .header("content-disposition", "attachment; filename=" + fileName + "; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .contentLength(payload.length)
                .body(resource);
    }

    private Map<String, Object> generateTemplateVariables() {

        final List<User> users = userService.loadAll();

        return ImmutableMap.<String, Object>builder()
                .put("users", users)
                .build();
    }
}

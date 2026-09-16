package com.shrija.asset.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "shrija.ai")
public record AssetAiProperties(
    @NotBlank String geminiApiKey,
    @NotBlank String geminiModel,
    @NotBlank String mcpServerUrl) {}

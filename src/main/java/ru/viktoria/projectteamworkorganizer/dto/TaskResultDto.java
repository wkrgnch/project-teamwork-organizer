package ru.viktoria.projectteamworkorganizer.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskResultDto {

    @NotBlank(message = "Описание результата обязательно")
    private String resultDescription;

    private String resultUrl;

    public String getResultDescription() {
        return resultDescription;
    }

    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
    }

    public String getResultUrl() {
        return resultUrl;
    }

    public void setResultUrl(String resultUrl) {
        this.resultUrl = resultUrl;
    }
}

package com.healthcare.claims.dto;

public class ClaimReviewDto {

    private String officerRemarks;

    public ClaimReviewDto() {
    }

    public ClaimReviewDto(String officerRemarks) {
        this.officerRemarks = officerRemarks;
    }

    public String getOfficerRemarks() {
        return officerRemarks;
    }

    public void setOfficerRemarks(String officerRemarks) {
        this.officerRemarks = officerRemarks;
    }
}

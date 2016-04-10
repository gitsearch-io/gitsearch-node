package io.gitsearch.elasticsearch.dto;

public class FileBranchDTO {
    private String branchName;
    private String filePath;

    public FileBranchDTO(String branchName, String filePath) {
        this.branchName = branchName;
        this.filePath = filePath;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "FileBranchDTO{" +
                "branchName='" + branchName + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}

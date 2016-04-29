package io.gitsearch.search.dto;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileBranchDTO that = (FileBranchDTO) o;

        if (branchName != null ? !branchName.equals(that.branchName) : that.branchName != null) return false;
        return filePath != null ? filePath.equals(that.filePath) : that.filePath == null;

    }

    @Override
    public int hashCode() {
        int result = branchName != null ? branchName.hashCode() : 0;
        result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
        return result;
    }
}

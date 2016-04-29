package io.gitsearch.search.dto;

import java.util.ArrayList;
import java.util.List;

public class SourceFileDTO {
    private String content;
    private String url;

    public String getUrl() {
        return url;
    }

    public SourceFileDTO setUrl(String url) {
        this.url = url;
        return this;
    }

    private List<FileBranchDTO> fileBranches = new ArrayList<>();

    public String getContent() {
        return content;
    }

    public SourceFileDTO setContent(String content) {
        this.content = content;
        return this;
    }

    public List<FileBranchDTO> getFileBranches() {
        return fileBranches;
    }

    public SourceFileDTO addFileBranch(FileBranchDTO fileBranchDTO) {
        fileBranches.add(fileBranchDTO);
        return this;
    }

    @Override
    public String toString() {
        return "SourceFileDTO{" +
                "content='" + content + '\'' +
                ", url='" + url + '\'' +
                ", fileBranches=" + fileBranches +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceFileDTO that = (SourceFileDTO) o;

        if (content != null ? !content.equals(that.content) : that.content != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        return fileBranches != null ? fileBranches.equals(that.fileBranches) : that.fileBranches == null;

    }

    @Override
    public int hashCode() {
        int result = content != null ? content.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (fileBranches != null ? fileBranches.hashCode() : 0);
        return result;
    }
}

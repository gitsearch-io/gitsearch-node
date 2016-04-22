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
}

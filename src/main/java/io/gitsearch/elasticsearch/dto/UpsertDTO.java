package io.gitsearch.elasticsearch.dto;

import java.util.ArrayList;
import java.util.List;

public class UpsertDTO {
    private String content;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private List<FileBranchDTO> fileBranches = new ArrayList<>();

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<FileBranchDTO> getFileBranches() {
        return fileBranches;
    }

    public void addFileBranch(FileBranchDTO fileBranchDTO) {
        fileBranches.add(fileBranchDTO);
    }

    @Override
    public String toString() {
        return "UpsertDTO{" +
                "content='" + content + '\'' +
                ", url='" + url + '\'' +
                ", fileBranches=" + fileBranches +
                '}';
    }
}

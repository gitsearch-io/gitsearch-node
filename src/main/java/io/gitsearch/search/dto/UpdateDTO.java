package io.gitsearch.search.dto;

import io.searchbox.core.Update;

import java.util.HashMap;
import java.util.Map;

public class UpdateDTO {
    private String script;
    private Map<String, Object> params = new HashMap<>();;
    private SourceFileDTO sourceFileDTO;

    public String getScript() {
        return script;
    }

    public UpdateDTO setScript(String script) {
        this.script = script;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public UpdateDTO addParam(String paramName, Object paramValue) {
        params.put(paramName, paramValue);
        return this;
    }

    public SourceFileDTO setSourceFileDTO() {
        return sourceFileDTO;
    }

    public UpdateDTO setSourceFileDTO(SourceFileDTO sourceFileDTO) {
        this.sourceFileDTO = sourceFileDTO;
        return this;
    }

    @Override
    public String toString() {
        return "UpdateDTO{" +
                "script='" + script + '\'' +
                ", params=" + params +
                ", upsert=" + sourceFileDTO +
                '}';
    }
}

package io.gitsearch.search.dto;

import java.util.HashMap;
import java.util.Map;

public class ESUpdateDTO {
    private String script;
    private Map<String, Object> params = new HashMap<>();;
    private SourceFileDTO upsert;

    public String getScript() {
        return script;
    }

    public ESUpdateDTO setScript(String script) {
        this.script = script;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public ESUpdateDTO addParam(String paramName, Object paramValue) {
        params.put(paramName, paramValue);
        return this;
    }

    public SourceFileDTO setSourceFileDTO() {
        return upsert;
    }

    public ESUpdateDTO setSourceFileDTO(SourceFileDTO sourceFileDTO) {
        this.upsert = sourceFileDTO;
        return this;
    }

    @Override
    public String toString() {
        return "ESUpdateDTO{" +
                "script='" + script + '\'' +
                ", params=" + params +
                ", upsert=" + upsert +
                '}';
    }
}

package io.gitsearch.elasticsearch.dto;

import java.util.HashMap;
import java.util.Map;

public class UpdateDTO {
    private String script;
    private Map<String, Object> params = new HashMap<>();;
    private UpsertDTO upsert;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void addParam(String paramName, Object paramValue) {
        params.put(paramName, paramValue);
    }

    public UpsertDTO getUpsert() {
        return upsert;
    }

    public void setUpsert(UpsertDTO upsert) {
        this.upsert = upsert;
    }
}

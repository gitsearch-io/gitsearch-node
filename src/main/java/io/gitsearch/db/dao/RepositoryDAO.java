package io.gitsearch.db.dao;

import java.util.List;

public interface RepositoryDAO {
    void insertRepository(String url, List<String> branches);

    void updateRepository(String url, List<String> branches);
}

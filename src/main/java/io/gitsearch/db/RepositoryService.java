package io.gitsearch.db;

import io.gitsearch.db.dao.RepositoryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryService {
    @Autowired
    private RepositoryDAO repositoryDAO;

    public void insert(String url, List<String> branches) {
        repositoryDAO.insertRepository(url, branches);
    }

    public void update(String url, List<String> branches) {
        repositoryDAO.updateRepository(url, branches);
    }
}

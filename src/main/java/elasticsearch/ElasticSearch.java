package elasticsearch;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.core.Update;
import org.apache.commons.lang3.StringEscapeUtils;

public class ElasticSearch {
    private JestClient client;

    public ElasticSearch() {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder("http://localhost:9200")
                .multiThreaded(true)
                .build());
        client = factory.getObject();
    }

    public void addDocuments() {
        Bulk bulk = new Bulk.Builder()
                .defaultIndex("")
                .defaultType("")
                .addAction(new Index.Builder("").build())

                .build();
    }





    public void addDocument(String id, String path, String content) throws Exception{
        String source = "{\"path\":\"" + path + "\", \"content\": \"" + StringEscapeUtils.escapeJson(content) + "\"}";
        Index index = new Index.Builder(source).index("gitsearch").type("codefile").id(id).build();
        System.out.println("es id: " + client.execute(index).getErrorMessage());
    }

    public void upsert(String id, String branch, String path, String content) throws Exception {
        String script = "{\n" +
                " \"script\" : \"ctx._source.branches += branch\",\n" +

                " \"params\" : {\n" +
//                    " \"branch\" : \"" + branch + "\"\n" +
                    " \"branch\" : {\"branch\":\""+ branch + "\"}\n" +
                " },\n" +
                " \"upsert\" : { "+
//                      " \"branches\" : [\"" + branch + "\"]," +
                      " \"branches\" : [{\"name\":\"" + branch + "\", \"filepath\":\"" + path + "\"}]," +
                    " \"content\" : \"" + StringEscapeUtils.escapeJson(content) + "\"" +
                " }\n" +
                "}";
        JestResult result = client.execute(new Update.Builder(script).index("gitsearch").type("codefile").id(id).build());
        System.out.println(result.getErrorMessage());
    }
}

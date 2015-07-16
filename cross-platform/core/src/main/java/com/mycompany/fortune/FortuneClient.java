package com.mycompany.fortune;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.model.Document;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;

import java.io.IOException;

public class FortuneClient {
    private static final String API_URL = "http://www.iheartquotes.com/api/v1/";

    public Document root;

    private class Fortune {
        public String quote;
    }

    private interface FortuneService {
        @GET("/random?format=json")
        void getFortune(Callback<Fortune> callback);
    }

    public static class OnFortuneListener {
        public void onFortune(String fortune) {
        }
    }

    private FortuneService service;

    public FortuneClient() {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .build();
        service = adapter.create(FortuneService.class);
    }

    public void getFortune(final OnFortuneListener listener) {
        service.getFortune(new Callback<Fortune>() {
            @Override
            public void success(Fortune fortune, Response response) {
                listener.onFortune(fortune.quote);
            }

            @Override
            public void failure(RetrofitError error) {
                HttpAutomationClient client = new HttpAutomationClient("http://localhost:8080/nuxeo/site/automation");
                Session session = null;
                try {
                    session = client.getSession("Administrator",
                                "Administrator");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Document root = null;
                try {
                    root = (Document) session.newRequest("Repository" +
                            ".GetDocument").set("value", "/").execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                listener.onFortune(root.getTitle());
            }
        });
    }
}

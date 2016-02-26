package ca.ualberta.cs.lonelytwitter;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.searchly.jestdroid.DroidClientConfig;
import com.searchly.jestdroid.JestClientFactory;
import com.searchly.jestdroid.JestDroidClient;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.Console;
import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.util.ArrayList;
import java.util.List;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.mapping.PutMapping;
import io.searchbox.params.SearchType;

/**
 * Created by esports on 2/16/16.
 */
public class ElasticsearchTweetController {
    private static JestDroidClient client;
    private static Gson gson;
    private static HttpClient http;


    //TODO: A function that gets tweets
    public static class GetTweetsTask extends AsyncTask<String, Void, ArrayList<Tweet>> {
        // TODO: Get tweets
        @Override
        protected ArrayList<Tweet> doInBackground(String... search_strings) {
            verifyClient();

            // Base arraylist to hold tweets
            ArrayList<Tweet> tweets = new ArrayList<Tweet>();

            String query = "{\n" +
                    "\"query\": {\n" +
                    "\"term\": { \"tweet\" : \"sad\" }\n" +
                    "}\n" +
                    "}\n";
            String search_string = "";

            Search search = new Search.Builder(search_string)
                    .addIndex("testing")
                    .addType("tweet")
                    .build();

            try {
                SearchResult result = client.execute(search);
                if(result.isSucceeded()) {
                    List<NormalTweet> fun = result.getSourceAsObjectList(NormalTweet.class);
                    tweets.addAll(fun);
                }
            } catch (IOException e) {
                // TODO: Something more useful
                throw new RuntimeException();
            }

            return tweets;
        }


    }


    public static class AddTweetTask extends AsyncTask<NormalTweet,Void,Void>{
        @Override
        protected Void doInBackground(NormalTweet... tweets) {
            verifyClient();

            // Since AsyncTasks work on arrays, we need to work with arrays as well (>= 1 tweet)
            for(int i = 0; i < tweets.length; i++) {
                NormalTweet tweet = tweets[i];

                Index index = new Index.Builder(tweet).index("testing").type("tweet").build();
                try {
                    DocumentResult result = client.execute(index);
                    if(result.isSucceeded()) {
                        // Set the ID to tweet that elasticsearch told me it was
                        tweet.setId(result.getId());
                    } else {
                        // TODO: Add an error message, because this was puzzling.
                        // TODO: Right here it will trigger if the insert fails
                        Log.i("TODO", "We actually failed here, adding a tweet");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }


    public static void verifyClient() {
        if(client == null) {
            // 2. If it doesn't, make it.
            // TODO: Put this URL somewhere it makes sense (e.g. class variable?)
            DroidClientConfig.Builder builder = new DroidClientConfig.Builder("http://cmput301.softwareprocess.es:8080");
            DroidClientConfig config = builder.build();

            JestClientFactory factory = new JestClientFactory();
            factory.setDroidClientConfig(config);
            client = (JestDroidClient) factory.getObject();
        }
    }
}
package com.aishwarypramanik.newzy;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    Map<Integer,JSONObject> map;
    JSONArray jsonArray;
    SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sqLiteDatabase = this.openOrCreateDatabase("articles",MODE_PRIVATE,null);

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS articles(id INTEGER PRIMARY KEY, articleId INTEGER, url VARCHAR, title VARCHAR, author VARCHAR)");
        DownloadTask downloadTask = new DownloadTask();
        try {
            String result = downloadTask.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();
            jsonArray = new JSONArray(result);
            map = new HashMap<>();
            for (int i = 0; i < 20; i++) {
                int id = Integer.parseInt(jsonArray.getString(i));
                DownloadTask aTask = new DownloadTask();
                String articleInfo = aTask.execute("https://hacker-news.firebaseio.com/v0/item/"+jsonArray.getString(i)+".json?print=pretty").get();
                JSONObject jsonObject = new JSONObject(articleInfo);
                map.put(id, jsonObject);
            }

            for(Integer i : map.keySet()){
                int articleId = i;
                String articleUrl = map.get(i).getString("url");
                String articleTitle = map.get(i).getString("title");
                String articleAuthor = map.get(i).getString("by");

                sqLiteDatabase.execSQL("INSERT INTO articles (articleId, url, title, author) VALUES("+
                        articleId+",'"+
                        articleUrl+"','"+
                        articleTitle+"','"+
                        articleAuthor+"')");

                Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM articles",null);
                cursor.moveToFirst();
                while (cursor!=null) {
                    int id = cursor.getColumnIndex("articleId");
                    Log.i("DETAILS", cursor.getInt(id) + "");
                    cursor.moveToNext();
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = new String();
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data!=-1){
                    char current = (char) data;
                    result +=current;
                    data = reader.read();

                }
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    }
}

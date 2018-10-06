package com.example.zzj.newrepresnet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.zzj.newrepresnet.Congressional.Name;

public class Detailed extends AppCompatActivity {

    //api key for propublic
    private final String proPubApiKey = "B6Ffl7yIU4ydKyUKLR0eQcDaAKMdlQGqlJ4DO03R";

    //arraylist for committes
    private ArrayList<String> committes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        committes = new ArrayList<>();
        //get info from congressional view
        Intent intent = getIntent();
        String name = intent.getStringExtra(Name);
        String img_url = intent.getStringExtra(Congressional.Img_url);
        String mem_id = intent.getStringExtra(Congressional.Mem_id);
        String party = intent.getStringExtra(Congressional.Party);
        String website = intent.getStringExtra(Congressional.Website);
        String contact_form = intent.getStringExtra(Congressional.Contact_form);

        //get textView
        TextView nameView = findViewById(R.id.d_name);
        TextView prtyView = findViewById(R.id.d_party);
        TextView webView = findViewById(R.id.d_website);
        TextView contactView = findViewById(R.id.d_contact_form);
        ImageView imgView = findViewById(R.id.d_staff_img);
        findCommittes(mem_id);


        //set textView and imgView
        Picasso.get().load(img_url).into(imgView);
        nameView.setText(name);
        prtyView.setText(party);
        webView.setText(website);
        if (contact_form.equals("null")) {
            contactView.setText("No Contact Form :(");
        } else {
            contactView.setText(contact_form);
        }
    }

    public void findCommittes(String mem_id) {
        String url = "https://api.propublica.org/congress/v1/members/" + mem_id + ".json";
        String prefix = "";




        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String toSet = "";
                            JSONArray resultArr = response.getJSONArray("results");
                            JSONObject firstObj = resultArr.getJSONObject(0);
                            JSONArray rolesArr = firstObj.getJSONArray("roles");
                            JSONObject firstRole = rolesArr.getJSONObject(0);
                            JSONArray commArr = firstRole.getJSONArray("committees");
                            for (int i = 0; i < commArr.length(); i++) {
                                committes.add(commArr.getJSONObject(i).getString("name"));
                                toSet += commArr.getJSONObject(i).getString("name") + " ";
                            }
                            ListView listView = findViewById(R.id.list_view2);
                            MyAdaptor myAddaptor = new MyAdaptor();
                            listView.setAdapter(myAddaptor);

                        }catch (JSONException e) {
                            Log.d("JSON error", "wtf!");
                            e.printStackTrace();
                        }
                    }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Rest Response", error.toString());
                }
            })
        {
            /** Passing some request headers* */
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("Content-Type", "application/json");
                headers.put("X-API-Key", proPubApiKey);
                return headers;
            }
        };

        queue.add(jor);
    }

    class MyAdaptor extends BaseAdapter {

        @Override
        public int getCount() {
            return committes.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.detailed_listview, null);
            //get the staff at the position

            //get all views
            TextView textView = convertView.findViewById(R.id.committ);

            //set the url to get the image
            String commit = committes.get(position);

            //set up everything in the list view
            textView.setText(commit);

            return convertView;

        }
    }
}

package com.example.zzj.newrepresnet;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Congressional extends AppCompatActivity {

    private FusedLocationProviderClient client;

    private Address currentAddress = null;

    public static final String data = "the staff data";

    private String status;

    //intent get from main
    private Intent intent;

    //intent messages for detailed view
    public static final String Name = "name";
    public static final String Img_url = "img_url";
    public static final String Party = "party";
    public static final String Mem_id = "member_id";
    public static final String Website = "web_site";
    public static final String Contact_form = "contact_form";

    //api key for geocodia
    private final String geocodiaApiKey = "e52b9bf97a723a2bb3bf25592f52a2b1227a227";

    //arraylist to store congressStaff
    public ArrayList<CongressStaff> congress_staff;

    public String zipCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congressional);

        zipCode = null;
        intent = getIntent();
        status = intent.getStringExtra(MainActivity.Status);
        congress_staff = new ArrayList<>();


        if (status.equals(MainActivity.Current)) {
            requestPermission();
            client = LocationServices.getFusedLocationProviderClient(this);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            client.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        getAddress(location);
                        findCongressionalAndDisplay();
                    }

                }
            });
        } else if (status.equals(MainActivity.ZipFound) || (status.equals(MainActivity.Random))) {
            findCongressionalAndDisplay();
        }

    }

    /**
     *Request for permission
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    /**
     *function that will convert the current coordinate to address
     */
    public void getAddress(Location myLoc) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try{
            List<Address> addresses = geocoder.getFromLocation(myLoc.getLatitude(), myLoc.getLongitude(), 1);
            currentAddress = addresses.get(0);
            Log.d("I got it!!!", "I got it!!!");
        } catch (IOException e) {
            Log.e("Not able to get address", "Not able to get address");
            e.printStackTrace();
        }
    }

    /**
     *function that will find the Congregational information from a address
     * and display in list view
     */
    public void findCongressionalAndDisplay() {
        String url = null;
        String prefix = "";
        if (status.equals(MainActivity.Current)) {
            if (currentAddress == null) {
                Log.d("current address not set", "null");
                return;
            }
            try {
                zipCode = currentAddress.getPostalCode();
                prefix = java.net.URLEncoder.encode(currentAddress.getThoroughfare(), "UTF-8");
                prefix += "%2c";
                prefix += java.net.URLEncoder.encode(currentAddress.getLocality(), "UTF-8");
                prefix += currentAddress.getAdminArea();
                prefix += "&fields=cd&api_key=" + geocodiaApiKey;

            } catch (UnsupportedEncodingException e) {
                Log.d("error before url", "omg!");
                e.printStackTrace();
            }
            url = "https://api.geocod.io/v1.3/geocode?q=" + prefix;
        } else if (status.equals(MainActivity.ZipFound) || status.equals(MainActivity.Random)) {
            zipCode = intent.getStringExtra(MainActivity.ZipCode);
            prefix = zipCode + "&fields=cd&api_key=" + geocodiaApiKey;
            url = "https://api.geocod.io/v1.3/geocode?q=" + prefix;

        } else {
            Log.e("Error", "something went wrong");
        }


        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray resultArr = response.getJSONArray("results");
                    JSONObject object = resultArr.getJSONObject(0);
                    JSONObject field = object.getJSONObject("fields");
                    JSONArray congrArr = field.getJSONArray("congressional_districts");
                    for (int i = 0; i < congrArr.length(); i++) {
                        JSONObject info = congrArr.getJSONObject(i);
                        JSONArray cur_leg0 =  info.getJSONArray("current_legislators");
                        //set the data and ready to pass to next activity
                        setStaff(cur_leg0, i);
                    }
                    //set the message on the top of the list view
                    TextView message = findViewById(R.id.message);
                    message.setText("Here is the result for Zip Code: " + zipCode + "!");

                    //set list view:
                    ListView listView = findViewById(R.id.list_view);
                    MyAdaptor myAddaptor = new MyAdaptor();
                    listView.setAdapter(myAddaptor);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            CongressStaff curStaff = congress_staff.get(position);
                            Intent intent = new Intent(view.getContext(), Detailed.class);
                            intent.putExtra(Name, curStaff.first_name + " " + curStaff.last_name);
                            intent.putExtra(Img_url, curStaff.img_url);
                            intent.putExtra(Party, curStaff.party);
                            intent.putExtra(Mem_id, curStaff.memberId);
                            intent.putExtra(Website, curStaff.website);
                            intent.putExtra(Contact_form, curStaff.contact_form);
                            startActivity(intent);
                        }
                    });

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
        });
        queue.add(jor);
    }

    public void setStaff(JSONArray cur_leg, int num) {
        /** find all information i need inset in to my data structure */
        try {
            int end;
            if(num == 0) {
                end = cur_leg.length();
            } else {
                end = 1;
            }
            for (int i = 0; i < end; i++) {
                JSONObject cur = cur_leg.getJSONObject(i);
                JSONObject bio = cur.getJSONObject("bio");
                JSONObject contact = cur.getJSONObject("contact");
                JSONObject ref = cur.getJSONObject("references");
                String type = cur.getString("type");
                String first = bio.getString("first_name");
                String last = bio.getString("last_name");
                String party = bio.getString("party");
                String phone = contact.getString("phone");
                String web = contact.getString("url");
                String contact_form = contact.getString("contact_form");
                String memId = ref.getString("bioguide_id");
                CongressStaff cur_staff = new CongressStaff(type, first, last, party, phone, web,
                        contact_form, memId);
                congress_staff.add(cur_staff);
                Log.e("size of the array", "" + congress_staff.size());
                Log.e("size of the jsonarray", "" + cur_leg.length());
            }
            Log.e("size after loop", "" + congress_staff.size());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *custom class
     */

    public class CongressStaff {
        public String type;
        public String first_name;
        public String last_name;
        public String party;
        public String phone;
        public String website;
        public String memberId;
        public String contact_form;
        public String img_url;

        public CongressStaff (String type, String first_name, String last_name,
                              String party, String phone, String website, String contact_form,
                              String memberId) {
            this.type = type;
            this.first_name = first_name;
            this.last_name = last_name;
            this.party = party;
            this.phone = phone;
            this.website = website;
            this.contact_form = contact_form;
            this.memberId = memberId;
            this.img_url = null;
        }

        public void setUrl (String url) {
            img_url = url;
        }

    }

    /**
     *custom adaptor
     */

    class MyAdaptor extends BaseAdapter {

        @Override
        public int getCount() {
            return congress_staff.size();
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
            convertView = getLayoutInflater().inflate(R.layout.list_view, null);
            //get the staff at the position
            CongressStaff curStaff = congress_staff.get(position);

            //get all views
            TextView type = (TextView) convertView.findViewById(R.id.type);
            TextView name = (TextView) convertView.findViewById(R.id.name);
            TextView party = (TextView) convertView.findViewById(R.id.party);
            ImageView image = (ImageView) convertView.findViewById(R.id.staff_img);

            //set the url to get the image
            String postFix = Character.toString(curStaff.memberId.charAt(0)) + "/";
            postFix += curStaff.memberId + ".jpg";
            String url = "http://bioguide.congress.gov/bioguide/photo/" + postFix;
            curStaff.setUrl(url);

            //set up everything in the list view
            Picasso.get().load(url).into(image);
            String type_str = curStaff.type;
            type.setText(type_str.substring(0, 1).toUpperCase() + type_str.substring(1));
            name.setText(curStaff.first_name + " " +  congress_staff.get(position).last_name);
            party.setText(curStaff.party);

            return convertView;

        }
    }

}

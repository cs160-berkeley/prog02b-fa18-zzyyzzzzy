package com.example.zzj.newrepresnet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final String ZipCode = "zipcode";
    public static final String Status = "status";
    public static final String ZipFound = "zipfound";
    public static final String Current = "current";
    public static final String Random = "random";
    private final String[] RandomZip = {"44691", "44012", "38632", "01844", "08902", "44024", "08610", "90403",
                                        "45066", "11776", "85203", "13501", "28079", "14534", "27405", "30039",
                                        "60452", "60060", "02127", "99654", "02148", "98144", "55912"};
    public Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText zip_input = findViewById(R.id.zip_input);
        Button find_button = findViewById(R.id.find_button);
        Button cur_button = findViewById(R.id.getlocation);
        Button rand_button = findViewById(R.id.getrandlocation);

        intent = new Intent(MainActivity.this, Congressional.class);


        //set onclick event for each button
        cur_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(Status, Current);
                startActivity(intent);
            }
        });

        find_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String zip = zip_input.getText().toString();
                if (zip == null || zip.equals("")) {
                    return;
                } else if (checkZip(zip)) {
                    intent.putExtra(Status, ZipFound);
                    intent.putExtra(ZipCode, zip);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Invalid Zip Code", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //random place
        rand_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String zip = generateRand();
                intent.putExtra(Status, Random);
                intent.putExtra(ZipCode, zip);
                Log.d("zip", zip);
                startActivity(intent);
            }
        });

    }

    private Boolean checkZip(String zipCode) {
        String regex = "^[0-9]{5}(?:-[0-9]{4})?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(zipCode);
        return matcher.matches();
    }

    private String generateRand () {
        Random rand = new Random();
        int zip = rand.nextInt(RandomZip.length);
        String zipCode = RandomZip[zip];
        return zipCode;
    }

}

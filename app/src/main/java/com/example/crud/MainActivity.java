package com.example.crud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //Variable for background video
    VideoView video2;

    //Variables which represent all app widgets on activity_main.xml
    private EditText mTitle, mDesc;
    private Button mSaveBtn, mShowBtn;
    //Instance of Firestore
    private FirebaseFirestore db;
    //Variables which represent updated variables
    private String uTitle, uDesc, uId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Hook up the VideoView to UI
        video2 = (VideoView) findViewById(R.id.video2);
        //Path for video Uri
        String path = "android.resource://" + getPackageName() + "/" + R.raw.clouds_loop;
        //Build video Uri
        Uri u = Uri.parse(path);
        video2.setVideoURI(u);
        video2.start();

        //Loops video
        video2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });


        //collecting widget id and sync them with variables which represent app widgets
        mTitle = findViewById(R.id.edit_title);
        mDesc = findViewById(R.id.edit_desc);
        mSaveBtn = findViewById(R.id.save_btn);
        mShowBtn = findViewById(R.id.showall_btn);

        //Opens a socket and instantiates the Firestore client
        db = FirebaseFirestore.getInstance();

        //Collecting temporary stored data from bundle
        Bundle bundle = getIntent().getExtras();
        //If temporary stored data is not empty
        if (bundle != null){
            //Sets Save Button text to "Update"
            mSaveBtn.setText("Update");
            //Splits temporary stored data from bundle to title, desc and id group
            uTitle = bundle.getString("uTitle");
            uId = bundle.getString("uId");
            uDesc = bundle.getString("uDesc");
            //Sets updated title data to title in MainActivity
            mTitle.setText(uTitle);
            //Sets updated description data to description in MainActivity
            mDesc.setText(uDesc);
        }else{
            //Sets Save Button text to "Save"
            mSaveBtn.setText("Save");
        }
        //When Show Button is clicked
        mShowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Starts ShowActivity and switches to activity_show.xml
                startActivity(new Intent(MainActivity.this, ShowActivity.class));
            }
        });
        //When Save Button is clicked
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            //Method saves Text from textFields into String variables when Save Button is clicked
            public void onClick(View view) {
                String title = mTitle.getText().toString();
                String desc = mDesc.getText().toString();


                Bundle bundle1 = getIntent().getExtras();
                //Checks if user want to update the data or just add new data
                if (bundle1 != null){
                    String id = uId;
                    updateToFireStore(id, title, desc);
                }else{
                    String id = UUID.randomUUID().toString();
                    saveToFireStore(id, title, desc);
                }
            }
        });
    }
    @Override
    // Function to resume the video
    protected void onResume(){
        video2.resume();
        super.onResume();
    }
    @Override
    // Function to suspend the video
    protected void onPause(){
        video2.suspend();
        super.onPause();
    }
    @Override
    // Function to stop playback of the video
    protected void onDestroy(){
        video2.stopPlayback();
        super.onDestroy();
    }
    //Method update data and then transfers it to the Firestore db by using unique ID
    private void updateToFireStore(String id, String title, String desc){
        db.collection("Documents").document(id).update("title", title, "desc", desc)
                //Handle success and failure of the button listener
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Data Updated!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //Method saves data and then transfers it to the Firestore db
    private void saveToFireStore(String id, String title, String desc){
        //Checks if title and description are empty
        if(!title.isEmpty() && !desc.isEmpty()){
            //Creates HashMap which will store id, title and description of saved data
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("title", title);
            map.put("desc", desc);
            //Saves data in collection called "Documents" on Firestore
            db.collection("Documents").document(id).set(map)
                    //Handle success and failure of the button listener
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Data saved!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(this,"Empty Fields not allowed", Toast.LENGTH_SHORT).show();
        }
    }
}
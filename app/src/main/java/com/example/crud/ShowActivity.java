package com.example.crud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShowActivity extends AppCompatActivity {
    //Variable for background video
    VideoView video1;

    //Variable which represent recyclerView on activity_show.xml
    private RecyclerView recyclerView;
    //Instance of Firestore
    private FirebaseFirestore db;
    //Instance of MyAdapter class
    private MyAdapter adapter;
    //List which stores Model data: id, title, desc
    private List<Model> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        //Hook up the VideoView to UI
        video1 = (VideoView) findViewById(R.id.video1);
        //Path for video Uri
        String path = "android.resource://" + getPackageName() + "/" + R.raw.clouds_loop_2;
        //Build video Uri
        Uri u = Uri.parse(path);
        video1.setVideoURI(u);
        video1.start();

        //Loops video
        video1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });

        //Collects recyclerView id and sync it with variable which represent recyclerView widget
        recyclerView = findViewById(R.id.recyclerview);
        //Method which makes sure that change of size of RecyclerView is constant when user makes action with it: deletes, creates, updates it
        recyclerView.setHasFixedSize(true);
        //Method setLayoutManager sets the layout of the contents in recyclerView
        //Method LinearLayoutManager creates a vertical LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Opens a socket and instantiates the Firestore client
        db = FirebaseFirestore.getInstance();
        //Set list to resizable array
        list = new ArrayList<>();
        adapter = new MyAdapter(this,list);
        recyclerView.setAdapter(adapter);

        //Add swipe to dismiss and drag & drop support to RecyclerView.
        ItemTouchHelper touchHelper = new ItemTouchHelper(new TouchHelper(adapter));
        touchHelper.attachToRecyclerView(recyclerView);

        showData();
    }
    @Override
    // Function to resume the video
    protected void onResume(){
        video1.resume();
        super.onResume();
    }
    @Override
    // Function to suspend the video
    protected void onPause(){
        video1.suspend();
        super.onPause();
    }
    @Override
    // Function to stop playback of the video
    protected void onDestroy(){
        video1.stopPlayback();
        super.onDestroy();
    }
    // Method which retrieves data from FireStore db and adds them to the list
    public void showData(){
        //Gets data from collection called "Documents" on Firestore
        db.collection("Documents").get()
                //Handle success and failure of the button listener
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //Clears previous list to avoid duplicate data
                        list.clear();
                        // DocumentSnapshot contains data read from a document in Firestore db
                        for (DocumentSnapshot snapshot: task.getResult()){
                            //Model gets data which are contained in sections: id, title and desc
                            Model model = new Model(snapshot.getString("id"), snapshot.getString("title"), snapshot.getString("desc"));
                            //Model with data from Firestore db is added to the list
                            list.add(model);
                        }
                        //Notifies the attached observers that the underlying data has been changed and View reflecting the data set should refresh itself
                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShowActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
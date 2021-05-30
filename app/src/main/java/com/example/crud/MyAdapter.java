package com.example.crud;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
//MyAdapter class extends RecyclerView.Adapter which provides a binding from an app-specific data set to views that are displayed within a RecyclerView
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    //Object of ShowActivity class
    private ShowActivity activity;
    //List which stores Model data: id, title, desc
    private List<Model> mList;
    //Instance of Firestore which opens a socket and instantiates the Firestore client
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public MyAdapter(ShowActivity activity, List<Model> mList){
        this.activity = activity;
        this.mList = mList;
    }
    //Method which updates data and is invoked in TouchHelper class
    public void updateData(int position){
        Model item = mList.get(position);
        //Bundle is required for temporary storage of data during execution by creating a mapping: String keys.
        Bundle bundle = new Bundle();
        bundle.putString("uId", item.getId());
        bundle.putString("uTitle", item.getTitle());
        bundle.putString("uDesc", item.getDesc());
        //Intent transfers temporary storage of bundle to the MainActivity and starts it
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }
    //Method which deletes data from data set by using unique ID
    public void deleteData(int position){
        Model item = mList.get(position);
        db.collection("Documents").document(item.getId()).delete()
                //Handle success of the button listener
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Checks if task has been completed successfully
                        if (task.isSuccessful()){
                            notifyRemoved(position);
                            Toast.makeText(activity, "Data Deleted!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(activity, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    //Method which notifies if data from list were removed
    private void notifyRemoved(int position){
        mList.remove(position);
        //Notify any registered observers that the item previously located at position has been removed from the data set.
        notifyItemRemoved(position);
        //Refreshes item set after removed item
        activity.showData();
    }
    @NonNull
    @Override
    //Method which is called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //LayoutInflater instantiates a layout XML file into its corresponding View objects.
        View v = LayoutInflater.from(activity).inflate(R.layout.item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    //Method is called by RecyclerView to display the data at the specified position.
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.title.setText(mList.get(position).getTitle());
        holder.desc.setText(mList.get(position).getDesc());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        //Initializes textViews
        TextView title, desc;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            //collect textViews and sync them with variables which will represent textViews
            title = itemView.findViewById(R.id.title_text);
            desc = itemView.findViewById(R.id.desc_text);
        }
    }
}

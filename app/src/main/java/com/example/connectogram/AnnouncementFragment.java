package com.example.connectogram;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
 import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectogram.adapters.AdapterAnnouncement;
import com.example.connectogram.models.ModelAnnounce;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnnouncementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnnouncementFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RecyclerView recyclerView;
    AdapterAnnouncement adapterAnnouncement;

    List<ModelAnnounce> modelAnnounceList;
    public AnnouncementFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AnnouncementFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AnnouncementFragment newInstance(String param1, String param2) {
        AnnouncementFragment fragment = new AnnouncementFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_announcement, container, false);

setHasOptionsMenu(true);
        recyclerView = view.findViewById(R.id.recyclerView);
        modelAnnounceList=new ArrayList<>();
        // Replace recyclerView with your RecyclerView ID
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Call a method to retrieve announcements from Firebase or any other source
        // For example:
        retrieveAnnouncementsFromFirebase();



        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        super.onCreateOptionsMenu(menu, inflater);

   menu.findItem(R.id.action_search).setVisible(false);
      menu.findItem(R.id.action_settings).setVisible(false);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_add_post)
        {
            //add anouncement

            //MOVE TO ADDDANOUCEMNT
            startActivity(new Intent(getContext(),AddAnnouncementActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
    private void retrieveAnnouncementsFromFirebase() {
        // Assuming you have a DatabaseReference initialized to "Announcements" node in Firebase
        DatabaseReference announcementsRef = FirebaseDatabase.getInstance().getReference("Announcements");
        announcementsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelAnnounceList.clear(); // Clear the list before adding new data
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ModelAnnounce announcement = dataSnapshot.getValue(ModelAnnounce.class);
                    modelAnnounceList.add(announcement);
                }
                // Create an instance of your AdapterAnnouncement class
                Collections.reverse(modelAnnounceList);
                adapterAnnouncement = new AdapterAnnouncement(getContext(), modelAnnounceList);
                // Set the adapter to your RecyclerView
                adapterAnnouncement.notifyDataSetChanged();
                recyclerView.setAdapter(adapterAnnouncement);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled event
            }
        });
    }
}
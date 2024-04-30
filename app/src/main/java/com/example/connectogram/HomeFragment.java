package com.example.connectogram;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import com.example.connectogram.adapters.AdapterPost;
import com.example.connectogram.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {


    FirebaseAuth fauth;
    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPost adapterPost;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fauth=FirebaseAuth.getInstance();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

    View view=inflater.inflate(R.layout.fragment_home, container, false);


        // Inflate the layout for this fragment
        setHasOptionsMenu(true);

        fauth=FirebaseAuth.getInstance();
        recyclerView=view.findViewById(R.id.postsRecycleView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        postList=new ArrayList<>();

        loadPost();
        return view;
    }

    private void loadPost() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();;
                for(DataSnapshot ds:snapshot.getChildren())
                {
                        ModelPost modelPost=ds.getValue(ModelPost.class);
                        postList.add(modelPost);




                }
                adapterPost=new AdapterPost(getActivity(),postList);

                recyclerView.setAdapter(adapterPost);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getActivity(),""+error,Toast.LENGTH_SHORT).show();


            }
        });
    }
    private  void searchPosts(String searchQuery)
    {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();;
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    if(modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())||modelPost.getpDesc().toLowerCase().contains(searchQuery.toLowerCase()))
                    { postList.add(modelPost);}



                }
                adapterPost=new AdapterPost(getActivity(),postList);

                recyclerView.setAdapter(adapterPost);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getActivity(),""+error,Toast.LENGTH_SHORT).show();


            }
        });

    }

    public void checkUserStatus()
    {
        FirebaseUser user= fauth.getCurrentUser();
        if(user!=null)
        {
            //email.setText(user.getEmail());
        }
        else {
            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);

MenuItem item=menu.findItem(R.id.action_search);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);

searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
    @Override
    public boolean onQueryTextSubmit(String query) {
        if(!TextUtils.isEmpty(query))
            searchPosts(query);
                    else
        {
            loadPost();
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(!TextUtils.isEmpty(newText))
            searchPosts(newText);
        else
        {
            loadPost();
        }
        return false;
    }
});

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout)
        {
            fauth.signOut();
            checkUserStatus();
            return  true;
        }
        if(id==R.id.action_settings)
        {
            startActivity(new Intent(getActivity(),SettingsActivity.class));


        }
        if(id==R.id.action_add_post)
        {
            startActivity(new Intent(getActivity(),AddPostActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }
}
package com.example.connectogram;

import android.content.Intent;
import android.os.Bundle;
import  java.util.*;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.connectogram.adapters.AdapterChatlist;
import com.example.connectogram.models.ModelChat;
import com.example.connectogram.models.ModelChatlist;
import com.example.connectogram.models.Model_discover;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatlistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatlistFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
FirebaseAuth fauth;
RecyclerView  recyclerView;
List<ModelChatlist>chatlistList;
List<Model_discover> userlist;

DatabaseReference reference;
FirebaseUser currentuser;
AdapterChatlist adapterChatlist;

    public ChatlistFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatlistFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatlistFragment newInstance(String param1, String param2) {
        ChatlistFragment fragment = new ChatlistFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_chatlist, container, false);
        fauth=FirebaseAuth.getInstance();
        currentuser=FirebaseAuth.getInstance().getCurrentUser();;

        recyclerView=view.findViewById(R.id.recyclerView);
        chatlistList=new ArrayList<>();
        reference= FirebaseDatabase.getInstance().getReference("Chatlist").child(currentuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();;

                for(DataSnapshot ds:snapshot.getChildren())
                {
                    ModelChatlist mdl=ds.getValue(ModelChatlist.class);
                    chatlistList.add(mdl);

                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return  view    ;
    }

    private void loadChats() {
        userlist=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            userlist.clear();;
            for(DataSnapshot ds:snapshot.getChildren())
            {
                Model_discover user=ds.getValue(Model_discover.class);
                for(ModelChatlist cl:chatlistList)
                {
                    if(user.getUid()!=null&& user.getUid().equals(cl.getId()))
                    {
                        userlist.add(user);
                        break;
                    }
                }
        adapterChatlist=new AdapterChatlist(getContext(),userlist);
                recyclerView.setAdapter(adapterChatlist);
                for(int i=0;i< userlist.size();i++)
                {
                    lastMessage(userlist.get(i).getUid());
                }
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String userId) {
DatabaseReference reference1=FirebaseDatabase.getInstance().getReference("Chats");
reference1.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        String theLastmessage="default";
        for(DataSnapshot ds:snapshot.getChildren())
        {
            ModelChat chat=ds.getValue(ModelChat.class);
            if(chat==null)
            {
                continue;
            }
            String sender=chat.getSender();
            String reciver=chat.getReceiver();
            if(sender==null||reciver==null)
            {
                continue;
            }
            if(chat.getReceiver().equals(currentuser.getUid())&& chat.getSender().equals(userId)||chat.getReceiver().equals(userId)&& chat.getSender().equals(currentuser.getUid()))
            {
                if(chat.getType().equals("image"))
                    theLastmessage="Send A photo";
                else
theLastmessage=chat.getMessage();
            }


        }
        adapterChatlist.setLastMessage(userId,theLastmessage);
        adapterChatlist.notifyDataSetChanged();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
});

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fauth= FirebaseAuth.getInstance();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
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


        menu.findItem(R.id.action_add_post).setVisible(false);

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

package com.example.justchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatWin extends AppCompatActivity {


   String recieverimg,recieverUid,recieverName,SenderUID;
   CircleImageView profile;
   TextView reciverNName;
   CardView sendbtn;
   EditText textmsg;
   FirebaseAuth firebaseAuth;
   FirebaseDatabase database;

   public static String senderImg;
   public static String recieverIImg;
   String senderRoom,recieverRoom;
   RecyclerView messageAdpter;
   ArrayList<msgModelclass> messagesArrayList;
   messagesAdpter mmessagesAdpter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_win);


        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        recieverName = getIntent().getStringExtra("nameeee");
        recieverimg = getIntent().getStringExtra("recieverImg");
        recieverUid = getIntent().getStringExtra("uid");

        messagesArrayList = new ArrayList<>();

        sendbtn = findViewById(R.id.sendbtnn);
        textmsg = findViewById(R.id.textmsg);
        profile = findViewById(R.id.profile_image);
        reciverNName = findViewById(R.id.recievername);

        messageAdpter = findViewById(R.id.msgadpter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); // messages start displaying from bottom
        messageAdpter.setLayoutManager(linearLayoutManager);

        mmessagesAdpter = new messagesAdpter(chatWin.this,messagesArrayList);
        messageAdpter.setAdapter(mmessagesAdpter);

        Picasso.get().load(recieverimg).into(profile);
        reciverNName.setText(""+recieverName);

        SenderUID = firebaseAuth.getUid();
        senderRoom = SenderUID + recieverUid;
        recieverRoom = recieverUid + SenderUID;

        DatabaseReference reference = database.getReference().child("user").child(firebaseAuth.getUid());

        DatabaseReference chatreference = database.getReference().child("chats").child(senderRoom).child("messages");

        chatreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    msgModelclass messages = dataSnapshot.getValue(msgModelclass.class);
                    messagesArrayList.add(messages);
                }
                mmessagesAdpter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderImg = snapshot.child("profilepic").getValue().toString();
                recieverIImg = recieverimg;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = textmsg.getText().toString();
                if(message.isEmpty()){
                    Toast.makeText(chatWin.this, "enter your message", Toast.LENGTH_SHORT).show();
                    return;
                }
                textmsg.setText("");
                Date date = new Date();
                msgModelclass messagess = new msgModelclass(message,SenderUID,date.getTime());

                database = FirebaseDatabase.getInstance();
                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .push().setValue(messagess).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        database.getReference().child("chats")
                                .child(recieverRoom)
                                .child("messages")
                                .push().setValue(messagess).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }
                });
            }
        });
    }
}
package com.androidchatapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import android.util.Base64;

public class Chat extends AppCompatActivity {
    LinearLayout layout;
    ImageView sendButton,imageView;
    EditText messageArea;
    ScrollView scrollView;
    Firebase reference1, reference2;
    Button pic;
    ImageView pic_upload;
    private static final int SELECT_PICTURE = 100;
    // creating an instance of Firebase Storage

    //FirebaseStorage storage = FirebaseStorage.getInstance();
    //creating a storage reference. Replace the below URL with your Firebase storage URL.
    //StorageReference storageRef = storage.getReferenceFromUrl("https://final-chat-app-42c5b.firebaseio.com/images.json");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        layout = (LinearLayout)findViewById(R.id.layout1);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        pic=(Button)findViewById(R.id.select_pic);
        pic_upload=(ImageButton)findViewById(R.id.pic_upload);
        imageView=(ImageView)findViewById(R.id.imview);
        Firebase.setAndroidContext(this);
//        reference1 = new Firebase("https://fir-testrsgss.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
//        reference2 = new Firebase("https://fir-testrsgss.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);

        reference1 = new Firebase("https://final-chat-app-42c5b.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase("https://final-chat-app-42c5b.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("user", UserDetails.username);
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                    messageArea.setText("");
                }
            }

        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();

                if(userName.equals(UserDetails.username)){
                    if(message.contains("--image--")) addImageBox(getImageOfString(message), 1);
                    else addMessageBox("You:-\n" + message, 1);
                }
                else{
                    if(message.contains("--image--")) addImageBox(getImageOfString(message), 2);
                    else addMessageBox(UserDetails.chatWith + ":-\n" + message, 2);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Picture"),SELECT_PICTURE );
            }
        });
        pic_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creating a reference to the full path of the file. myfileRef now points
                // gs://fir-demo-d7354.appspot.com/myuploadedfile.jpg
               // StorageReference myfileRef = storageRef.child("myuploadedfile.jpg");
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Picture"),199 );
            }
        });

    }
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode==RESULT_OK){
            if(requestCode==SELECT_PICTURE){
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                    String path = getPathFromURI(selectedImageUri);
                    Log.i("IMAGE PATH TAG", "Image Path : " + path);
                    // Set the image in ImageView
                    imageView.setImageURI(selectedImageUri);


                }
            }
            if(requestCode==199){
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                    String path = getPathFromURI(selectedImageUri);
                    Log.i("IMAGE PATH TAG", "Image Path : " + path);
                    // Set the image in ImageView
                    String messageText = getStringOfImage(selectedImageUri);

                    if(!messageText.equals("")){
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("message", messageText);
                        map.put("user", UserDetails.username);
                        reference1.push().setValue(map);
                        reference2.push().setValue(map);
                        messageArea.setText("");
                    }


                    //addImageBox(getImageOfString(messageText), 1);
                }
            }
        }
    }
    private String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public void  addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);

        textView.setLayoutParams(lp);

        if(type == 1) {
            textView.setBackgroundResource(R.drawable.bubble_a);
            lp.setMargins(70,0,0,10);


        }
        else{
            textView.setBackgroundResource(R.drawable.bubble_b);
            lp.setMargins(0,0,70,10);
        }
        layout.addView(textView);
        scrollView.post(new Runnable(
                //layout.setGravity(type==1? Gravity.LEFT : Gravity.RIGHT);
        ) {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

    }

    public void  addImageBox(Bitmap bmp, int type){
        ImageView imgView = new ImageView(Chat.this);
        //imgView.setText(message);
        imgView.setImageBitmap(bmp);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);

        imgView.setLayoutParams(lp);
        imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if(type == 1) {
            imgView.setBackgroundResource(R.drawable.bubble_a);
            lp.setMargins(70,0,0,10);


        }
        else{
            imgView.setBackgroundResource(R.drawable.bubble_b);
            lp.setMargins(0,0,70,10);
        }

        final ImageView imgView2 = new ImageView(Chat.this);
        //imgView.setText(message);
        imgView2.setImageBitmap(bmp);
        imgView2.setLayoutParams(lp);
        //imgView2.setScaleType(ImageView.ScaleType.FIT_XY);

        final Dialog diag = new Dialog(Chat.this);
        diag.setContentView(imgView2);

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diag.show();
            }
        });

        layout.addView(imgView);
        scrollView.post(new Runnable(
                //layout.setGravity(type==1? Gravity.LEFT : Gravity.RIGHT);
        ) {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

    }

    public String getStringOfImage(Uri uri)
    {
        try {
            Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 5, baos);
            byte[] imagebytes = baos.toByteArray();
            String image = Base64.encodeToString(imagebytes, Base64.DEFAULT);
            return "--image--" + image;
        }catch(Exception e){

        }
        return "";
    }

    public Bitmap getImageOfString(String image)
    {
        image = image.replace("--image--", "");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
         byte[] imageBytes = baos.toByteArray();
        imageBytes = Base64.decode(image, Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return decodedImage;
    }
}
package com.mad2021june.dianacosmetics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad2021june.dianacosmetics.Model.Products;
import com.mad2021june.dianacosmetics.Prevalent.Prevalent;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProductDetailsActivity extends AppCompatActivity
{
    private ImageView productImage;
    private TextView productPrice,productDescription,productName;
    private String productID = "";
    public String cartID;

    private Button addToCartBtn;
    private ElegantNumberButton qtyBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        productID = getIntent().getStringExtra("pid");

        productImage = (ImageView) findViewById(R.id.product_image_details);
        productPrice = (TextView) findViewById(R.id.product_price_details);
        productDescription = (TextView) findViewById(R.id.product_description_details);
        productName = (TextView) findViewById(R.id.product_name_details);

        qtyBtn =(ElegantNumberButton) findViewById( R.id.number_btn );
        addToCartBtn =(Button) findViewById( R.id.pd_add_to_cart_btn) ;

        getProductDetails(productID);

        addToCartBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addingToCartList();
            }
        } );






    }


    private void addingToCartList() {
        String saveCurrentDate,saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM,dd,yyyy");
        saveCurrentDate = currentDate.format( calForDate.getTime() );

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentDate.format( calForDate.getTime() );

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child( "CartList" );

        cartID = cartListRef.push().getKey();

        final HashMap<String,Object> cartMap = new HashMap<>();
        cartMap.put( "pid",productID );
        cartMap.put( "cid",cartID );
        cartMap.put( "pname",productName.getText().toString() );
        cartMap.put( "price",productPrice.getText().toString()  );
        cartMap.put( "date",saveCurrentDate);
        cartMap.put( "time",saveCurrentTime );
        cartMap.put( "qty",qtyBtn.getNumber());
        cartMap.put( "discount","");

        cartListRef.child(cartID).child( "User View" ).child( Prevalent.currentOnlineUser.getPhone())
                .child( "Products" ).child( productID )
                .setValue( cartMap )
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            cartListRef.child(cartID).child( "Admin View" ).child( Prevalent.currentOnlineUser.getPhone())
                                    .child( "Products" ).child( productID )
                                    .setValue( cartMap )
                                    .addOnCompleteListener( new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                Toast.makeText( ProductDetailsActivity.this ,"Added to Cart List",Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent (ProductDetailsActivity.this,HomeActivity.class);
                                                startActivity( intent );
                                            }

                                        }
                                    } );

                        }
                    }
                } );


    }
    private void getProductDetails(String productID)
    {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("Products");

        productsRef.child(productID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    Products products = dataSnapshot.getValue(Products.class);

                    productName.setText(products.getPname());
                    productPrice.setText(products.getPrice());
                    productDescription.setText(products.getDescription());
                    Picasso.get().load(products.getImage()).into(productImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError error)
            {

            }
        });
    }
}
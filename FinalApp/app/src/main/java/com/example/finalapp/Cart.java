package com.example.finalapp;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalapp.Common.Common;
import com.example.finalapp.Database.Database;
import com.example.finalapp.Model.Order;
import com.example.finalapp.Model.Request;
import com.example.finalapp.ViewHolder.CartAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Cart extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView txtTotalPrice;
    Button btnPlace;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Init
        recyclerView = (RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = (TextView)findViewById(R.id.total);
        btnPlace = (Button)findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cart.size()> 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this,"Your cart is empty !!!",Toast.LENGTH_SHORT).show();

            }
        });

        loadListFood();
    }

    public void showAlertDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address");

        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);

        final EditText edtAddress = (EditText)order_address_comment.findViewById(R.id.edtAddress);
        final EditText edtComment = (EditText)order_address_comment.findViewById(R.id.edtComment);

        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Create new request
                Request request = new Request(Common.currentUser.getPhone(), Common.currentUser.getName(), edtAddress.getText().toString(), txtTotalPrice.getText().toString(),edtComment.getText().toString(), cart);

                //Submit to Firebase
                //We will using System.CurrentMillo to key
                String order_number = String.valueOf(System.currentTimeMillis());
                requests.child(String.valueOf(System.currentTimeMillis())).setValue(request);
                //Delete cart
                new Database(getBaseContext()).cleanCart();
                sendNotificationOrder(order_number);
//                Toast.makeText(Cart.this, "Thank you , Order Place", Toast.LENGTH_SHORT).show();
//                finish();
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void sendNotificationOrder(String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("isServerToken").equalTo(true);//get all node with isServertoken is true
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for()
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        })
    }

    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Calculate total price
        int total = 0;
        for(Order order:cart)
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.DELETE)){
            deleteCart(item.getOrder());
        }
        return true;

    }

    private void deleteCart(int position) {
        //We will remove item at List<Order> by position
        cart.remove(position);
        //After that, we will delete all old datat from SQLite
        new Database(this).cleanCart();
        //And final, we will update new data from List<Order> to SQLite
        for(Order item:cart)
            new Database(this).addToCart(item);
        //Refresh
        loadListFood();
    }
}

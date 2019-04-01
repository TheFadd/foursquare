package com.example.foursquare.view;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.foursquare.DetailInformation;
import com.example.foursquare.R;
import com.example.foursquare.networking.venueitem.MessageJSON;
import com.example.foursquare.networking.venueitem.jsononeitem.AttributesGroup;
import com.example.foursquare.networking.venueitem.jsononeitem.BestPhoto;
import com.example.foursquare.networking.venueitem.jsononeitem.ItemTips;
import com.example.foursquare.networking.VenueListAPI;
import com.example.foursquare.networking.venueitem.jsononeitem.Likes;
import com.example.foursquare.networking.venueitem.jsononeitem.User;
import com.example.foursquare.networking.venueitem.jsononeitem.UserPhoto;
import com.example.foursquare.detailAdapterView.Tiper;
import com.example.foursquare.detailAdapterView.Header;
import com.example.foursquare.detailAdapterView.RecycleViewItem;
import com.example.foursquare.detailAdapterView.DetailAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private String discription = "";
    private ArrayList<String> listIntent;
    private double latitude;
    private double longtitude;
    private VenueListAPI.ApiInterface api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        makeInternetRequest();
    }

    private void makeInternetRequest() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        listIntent = getIntent().getStringArrayListExtra("list");
        String title = listIntent.get(1);
        this.setTitle(title);
        String id = listIntent.get(0);
        latitude = Double.valueOf(listIntent.get(6));
        longtitude = Double.valueOf(listIntent.get(7));
        String client_id = VenueListAPI.CLIENT_ID;
        String client_secret = VenueListAPI.CLIENT_SECRET;
        String version = VenueListAPI.VERSION;
        api = VenueListAPI.getClientVenueItem(id).create(VenueListAPI.ApiInterface.class);
        Call<MessageJSON> callMessageJSON = api.getVenue(id, client_id, client_secret, version);
        callMessageJSON.enqueue(new Callback<MessageJSON>() {

            @Override
            public void onResponse(@NonNull Call<MessageJSON> call, @NonNull Response<MessageJSON> response) {
                Log.e("RESPONSE", "РЕСПОНС ОКК");
                List<AttributesGroup> list = response.body().getResponseItem().getVenueItem().getAttributes().getAttributesGroups();
                for (AttributesGroup ag : list) {
                    if (discription.equals("")) {
                        discription = ag.getName();
                    } else
                        discription = discription + ", " + ag.getName();
                }
                List<ItemTips> tipslist = response.body().getResponseItem().getVenueItem().getTips().getGroups().get(0).getItemTips();
                DetailInformation detailInformation = DetailInformation.from(listIntent, response.body().getResponseItem(),
                        discription, tipslist);
                initRecyclerView(detailInformation);
            }

            @Override
            public void onFailure(Call<MessageJSON> call, Throwable t) {
                Log.e("Response", "onFailure");
            }
        });
    }



    private void initRecyclerView(DetailInformation detailInformation) {
        RecyclerView recyclerView = findViewById(R.id.recyclerview_comment);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new DetailAdapter(createDummyList(detailInformation), DetailActivity.this,latitude,longtitude));
    }

    private List<RecycleViewItem> createDummyList(DetailInformation detailInformation) {
        List<RecycleViewItem> recyclerViewItems = new ArrayList<>();
        Header header = new Header(detailInformation);
        recyclerViewItems.add(header);

        for (int i = 0; i < detailInformation.getTips().size(); i++) {
            Tiper foodItem = new Tiper(detailInformation.getTips().get(i));
            recyclerViewItems.add(foodItem);
        }
        return recyclerViewItems;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        if(id ==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
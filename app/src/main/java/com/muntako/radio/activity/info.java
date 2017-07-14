package com.muntako.radio.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.muntako.radio.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ADMIN on 26-May-17.
 */

public class info extends AppCompatActivity implements View.OnClickListener{

    EditText editNama, editTel,editSource,editLast;
    RadioGroup group,like;
    String nama, noTelepon, jeKel,source,suka_musik;
    Button nextButton;
    Spinner spinner;
    boolean likeMusic;
    int waktu,x,y;
    ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_layout);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Data Responden");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dialog = new ProgressDialog(this);

        editNama = (EditText)findViewById(R.id.nama);
        editTel = (EditText)findViewById(R.id.notel);
        group = (RadioGroup)findViewById(R.id.jekel);
        like = (RadioGroup)findViewById(R.id.music_like);
        nextButton = (Button)findViewById(R.id.masuk);
        editSource = (EditText) findViewById(R.id.music_source);
        editLast = (EditText) findViewById(R.id.last_hear);
        spinner = (Spinner)findViewById(R.id.spinner);

        final List<String> time = new ArrayList<>();
        time.add("Hari");
        time.add("Minggu");
        time.add("Bulan");
        time.add("Tahun");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,time);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                String t = parent.getItemAtPosition(position).toString();
                if (t.equalsIgnoreCase("hari")){
                    y =1;
                }else if (t.equalsIgnoreCase("minggu")){
                    y =7;
                }else if (t.equalsIgnoreCase("bulan")){
                    y =30;
                }else if (t.equalsIgnoreCase("tahun")){
                    y =365;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        nextButton.setOnClickListener(this);
    }

    public boolean fieldNotEmpty(){

        switch (group.getCheckedRadioButtonId()){
            case R.id.pria:
                jeKel = "Laki-laki";
                break;
            case R.id.perempuan:
                jeKel = "Perempuan";
        }

        waktu = cekTerakhir() * y;

        if (cekNama() && cekTel() ){
                return cekSource();
        }
        return false;
    }

    public boolean isLikeMusic(){
        switch (like.getCheckedRadioButtonId()){
            case R.id.yes:
                likeMusic = true;
                suka_musik = "iya";
                break;
            case R.id.no:
                likeMusic = false;
                suka_musik = "tidak";
                break;
        }
        return likeMusic;
    }

    public boolean cekNama(){
        if (editNama.getText().length()==0){
            editNama.setError("Nama harus diisi");
            return false;
        }
        nama = editNama.getText().toString();
        return true;
    }

    public boolean cekTel(){
        if( editTel.getText().length()<10){
            editTel.setError("Nomor telepon minimal 11 angka");
            return false;
        }
        noTelepon = editTel.getText().toString();
        return true;
    }

    public int cekTerakhir(){
        if (editLast.getText().length()>0){
            x = Integer.parseInt(editLast.getText().toString());
            return x;
        }
        return 0;
    }

    public boolean cekSource(){
        if (isLikeMusic()) {
            if (editSource.getText().length() > 0) {
                source = editSource.getText().toString();
                return true;
            } else {
                editSource.setError("Mohon isi data");
                return false;
            }
        }else {
                source = editSource.getText().toString();
                return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.masuk:
                if(fieldNotEmpty()){
                    pushData();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    public void pushData(){
        dialog.setTitle("Loading......");
        dialog.show();
        String URL = "http://perhimak-ui.id/radio/addRespondent.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dialog.dismiss();
                        Toast.makeText(info.this,nama+noTelepon+jeKel+waktu+likeMusic,Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(info.this,speech_test.class).putExtra("nama",nama));
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Toast.makeText(info.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String,String> getParams(){

                Map<String,String> params = new HashMap<String, String>();
                params.put("nama",nama);
                params.put("telepon",noTelepon);
                params.put("gender", jeKel);
                params.put("suka_musik", suka_musik);
                params.put("sarana",source);
                params.put("terakhir", String.valueOf(waktu));
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

}

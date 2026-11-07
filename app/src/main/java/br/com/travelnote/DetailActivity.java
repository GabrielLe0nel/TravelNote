package br.com.travelnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.Locale;

public class DetailActivity extends Activity {
    private MemoryDatabase db; private Memory memory; private long id;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xFFF5F1E8);db=new MemoryDatabase(this);id=getIntent().getLongExtra("id",0);memory=db.find(id);if(memory==null){finish();return;}build();}
    private void build(){
        setContentView(R.layout.activity_detalhes);
        ((TextView)findViewById(R.id.detail_date)).setText(memory.createdAt);
        ((TextView)findViewById(R.id.detail_title)).setText(memory.title);
        ((TextView)findViewById(R.id.detail_story)).setText(memory.description);
        findViewById(R.id.back_detail).setOnClickListener(v->finish());
        TextView location=findViewById(R.id.detail_location);updateLocationText(location);
        TextView pressure=findViewById(R.id.detail_pressure);
        if(memory.pressureHpa!=null){pressure.setText(String.format(Locale.getDefault(),"Pressão atmosférica: %.1f hPa",memory.pressureHpa));pressure.setVisibility(View.VISIBLE);}
        ImageView photo=findViewById(R.id.detail_photo);
        if(memory.photoUri!=null){try{photo.setImageURI(Uri.parse(memory.photoUri));photo.setVisibility(View.VISIBLE);}catch(Exception ignored){}}
        if(memory.locationName==null&&memory.latitude!=null){PlaceResolver.resolve(this,memory.latitude,memory.longitude,name->{if(name!=null){memory.locationName=name;db.updateLocationName(memory.id,name);updateLocationText(location);}});}
        Button map=findViewById(R.id.button_open_map);
        if(memory.latitude!=null){map.setVisibility(View.VISIBLE);map.setOnClickListener(v->{Uri geo=Uri.parse(String.format(Locale.US,"geo:%f,%f?q=%f,%f",memory.latitude,memory.longitude,memory.latitude,memory.longitude));startActivity(new Intent(Intent.ACTION_VIEW,geo));});}
        findViewById(R.id.button_edit_memory).setOnClickListener(v->{Intent i=new Intent(this,EditorActivity.class);i.putExtra("id",id);startActivity(i);finish();});
        findViewById(R.id.button_delete_memory).setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Excluir memória?").setMessage("Esta lembrança será removida do caderno.").setNegativeButton("Cancelar",null).setPositiveButton("Excluir",(dialog,which)->{db.delete(id);finish();}).show());
    }
    private void updateLocationText(TextView location){
        if(memory.locationName!=null&&!memory.locationName.trim().isEmpty())location.setText("⌖  "+memory.locationName);
        else if(memory.latitude!=null&&memory.longitude!=null)location.setText(String.format(Locale.getDefault(),"⌖  %.5f, %.5f",memory.latitude,memory.longitude));
        else location.setText("Localização não registrada");
    }
}

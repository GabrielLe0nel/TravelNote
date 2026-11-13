package br.com.travelnote;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditorActivity extends Activity implements SensorEventListener {
    private static final int REQUEST_LOCATION=41, REQUEST_PHOTO=42;
    private EditText title,description; private TextView place,pressureLabel,photoLabel; private Double latitude,longitude; private Float pressureHpa; private String photoUri,locationName; private long editId; private MemoryDatabase db; private LocationManager locationManager; private LocationListener locationListener; private SensorManager sensors; private Sensor pressureSensor;
    /** true enquanto o usuário está nas configurações de localização do sistema */
    private boolean waitingForLocationSettings=false;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xFFF5F1E8);db=new MemoryDatabase(this);editId=getIntent().getLongExtra("id",0);sensors=(SensorManager)getSystemService(SENSOR_SERVICE);pressureSensor=sensors==null?null:sensors.getDefaultSensor(Sensor.TYPE_PRESSURE);build();if(editId!=0)load();else captureLocation();}
    private void build(){
        setContentView(R.layout.activity_editor);
        title=findViewById(R.id.input_title);description=findViewById(R.id.input_description);
        place=findViewById(R.id.location_text);pressureLabel=findViewById(R.id.pressure_text);photoLabel=findViewById(R.id.photo_label);
        TextView heading=findViewById(R.id.editor_heading);heading.setText(editId==0?"Nova memória":"Editar memória");
        Button save=findViewById(R.id.button_save_memory);save.setText(editId==0?"Salvar memória":"Salvar alterações");save.setOnClickListener(v->save());
        findViewById(R.id.back_editor).setOnClickListener(v->finish());
        findViewById(R.id.button_add_photo).setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,REQUEST_PHOTO);});
    }
    private void load(){Memory m=db.find(editId);if(m==null)return;title.setText(m.title);description.setText(m.description);latitude=m.latitude;longitude=m.longitude;locationName=m.locationName;pressureHpa=m.pressureHpa;photoUri=m.photoUri;updateMeta();}
    private void captureLocation(){if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_LOCATION);place.setText("Permita o acesso à localização para anexar o lugar.");return;}readLocation();}
    private void readLocation(){
        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        // Verifica se os serviços de localização do sistema estão ligados
        if(!isLocationEnabled()){
            place.setText("Localização desativada.");
            showEnableLocationDialog();
            return;
        }
        place.setText("Obtendo localização atual…");
        try {
            locationListener=new LocationListener(){
                @Override public void onLocationChanged(Location location){
                    latitude=location.getLatitude();longitude=location.getLongitude();locationName=null;updateMeta();resolvePlaceName(latitude,longitude);
                }
                @Override public void onProviderEnabled(String provider){}
                @Override public void onProviderDisabled(String provider){}
                @Override public void onStatusChanged(String provider,int status,android.os.Bundle extras){}
            };
            boolean requested=false;
            for(String provider:locationManager.getProviders(true)){
                if(LocationManager.PASSIVE_PROVIDER.equals(provider))continue;
                locationManager.requestLocationUpdates(provider,0,0,locationListener,getMainLooper());
                requested=true;
            }
            Location last=latestKnownLocation(locationManager);
            if(last!=null){latitude=last.getLatitude();longitude=last.getLongitude();updateMeta();resolvePlaceName(latitude,longitude);}
            if(!requested&&last==null)place.setText("Aguardando sinal de localização…");
        }catch(SecurityException e){place.setText("Sem permissão de localização.");}
    }
    /** Retorna true se pelo menos um provider ativo (GPS ou Network) estiver ligado. */
    private boolean isLocationEnabled(){
        if(locationManager==null)return false;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){return locationManager.isLocationEnabled();}
        try{return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception e){return false;}
    }
    /** Mostra diálogo pedindo para o usuário ativar a localização nas configurações do sistema. */
    private void showEnableLocationDialog(){
        new AlertDialog.Builder(this)
            .setTitle("Localização desativada")
            .setMessage("Para registrar o local desta memória, ative os serviços de localização do dispositivo.")
            .setPositiveButton("Ativar localização",(d,w)->{
                waitingForLocationSettings=true;
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            })
            .setNegativeButton("Continuar sem localização",(d,w)->place.setText("Localização não registrada nesta memória."))
            .setCancelable(false)
            .show();
    }
    private Location latestKnownLocation(LocationManager lm){
        try{Location best=null;for(String provider:lm.getProviders(true)){Location l=lm.getLastKnownLocation(provider);if(l!=null&&(best==null||l.getTime()>best.getTime()))best=l;}return best;}catch(SecurityException e){return null;}
    }
    private void stopLocationUpdates(){if(locationManager!=null&&locationListener!=null){try{locationManager.removeUpdates(locationListener);}catch(SecurityException ignored){}locationListener=null;}}
    private void resolvePlaceName(double lat,double lon){PlaceResolver.resolve(this,lat,lon,name->{if(name==null||latitude==null||longitude==null||Math.abs(latitude-lat)>0.00001||Math.abs(longitude-lon)>0.00001)return;locationName=name;updateMeta();if(editId!=0)db.updateLocationName(editId,name);});}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==REQUEST_LOCATION){if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED||checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)readLocation();else place.setText("Localização não autorizada.");}}
    @Override protected void onResume(){
        super.onResume();
        if(editId==0){
            if(pressureSensor!=null&&sensors!=null)sensors.registerListener(this,pressureSensor,SensorManager.SENSOR_DELAY_NORMAL);
            else updateMeta();
            // Volta das configurações do sistema: tenta obter localização se foi ativada
            if(waitingForLocationSettings){
                waitingForLocationSettings=false;
                if(isLocationEnabled()) readLocation();
                else place.setText("Localização não registrada nesta memória.");
            }
        }
    }
    @Override protected void onPause(){super.onPause();if(sensors!=null)sensors.unregisterListener(this);stopLocationUpdates();}
    @Override public void onSensorChanged(SensorEvent event){if(event.sensor.getType()!=Sensor.TYPE_PRESSURE)return;pressureHpa=event.values[0];updateMeta();}
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
    private void updateMeta(){if(place!=null){if(locationName!=null)place.setText("⌖  "+locationName);else if(latitude!=null)place.setText(String.format(Locale.getDefault(),"⌖  %.5f, %.5f",latitude,longitude));else place.setText("Localização indisponível no momento.");}if(pressureLabel!=null){if(pressureHpa!=null)pressureLabel.setText(String.format(Locale.getDefault(),"Pressão atmosférica: %.1f hPa",pressureHpa));else if(editId!=0)pressureLabel.setText("Pressão não registrada nesta memória.");else if(pressureSensor==null)pressureLabel.setText("Barômetro indisponível neste dispositivo.");else pressureLabel.setText("Aguardando leitura do barômetro…");}if(photoLabel!=null)photoLabel.setText(photoUri==null?"Nenhuma foto selecionada":"Foto adicionada à memória");}
    private void save(){String t=title.getText().toString().trim(),d=description.getText().toString().trim();if(t.isEmpty()){title.setError("Informe um título");title.requestFocus();return;}if(d.isEmpty()){description.setError("Conte um pouco sobre essa memória");description.requestFocus();return;}Memory m=editId==0?new Memory():db.find(editId);if(m==null)m=new Memory();m.title=t;m.description=d;m.latitude=latitude;m.longitude=longitude;m.locationName=locationName;m.pressureHpa=pressureHpa;m.photoUri=photoUri;if(m.createdAt==null)m.createdAt=new SimpleDateFormat("dd/MM/yyyy · HH:mm",Locale.getDefault()).format(new Date());if(db.save(m)>0){setResult(RESULT_OK);finish();}else Toast.makeText(this,"Não foi possível salvar a memória.",Toast.LENGTH_SHORT).show();}
    @Override protected void onActivityResult(int r,int c,Intent data){super.onActivityResult(r,c,data);if(r==REQUEST_PHOTO&&c==RESULT_OK&&data!=null&&data.getData()!=null){Uri uri=data.getData();try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}photoUri=uri.toString();updateMeta();}}
}

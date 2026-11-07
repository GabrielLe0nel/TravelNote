package br.com.travelnote;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

/** Mapa OpenStreetMap com marcadores para todas as notas que têm coordenadas. */
public class MapActivity extends Activity {
    @Override public void onCreate(Bundle state){
        super.onCreate(state);getWindow().setStatusBarColor(0xFFF5F1E8);
        setContentView(R.layout.activity_map);
        findViewById(R.id.back_from_map).setOnClickListener(v->finish());
        FrameLayout container=findViewById(R.id.map_container);
        TextView placeholder=findViewById(R.id.map_preview_placeholder);placeholder.setVisibility(View.VISIBLE);placeholder.setText("Carregando mapa…");
        WebView map=new WebView(this);map.getSettings().setJavaScriptEnabled(true);map.setWebViewClient(new WebViewClient(){
            @Override public void onPageFinished(WebView view,String url){view.setVisibility(View.VISIBLE);placeholder.setVisibility(View.GONE);}
            @Override public void onReceivedError(WebView view,int errorCode,String description,String failingUrl){view.setVisibility(View.GONE);placeholder.setText("Não foi possível carregar o mapa. Verifique a conexão com a internet.");}
        });
        map.setVisibility(View.INVISIBLE);container.addView(map,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
        map.loadDataWithBaseURL("https://travelnote.local/",html(points()),"text/html","UTF-8",null);
    }
    private String points(){
        JSONArray array=new JSONArray();List<Memory> memories=new MemoryDatabase(this).all();
        for(Memory memory:memories){if(memory.latitude==null||memory.longitude==null)continue;JSONObject item=new JSONObject();try{item.put("lat",memory.latitude);item.put("lon",memory.longitude);item.put("title",memory.title);item.put("place",memory.locationName==null?"":memory.locationName);array.put(item);}catch(Exception ignored){}}
        return array.toString();
    }
    private String html(String points){
        return "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1'>"
            +"<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
            +"<style>html,body,#map{height:100%;margin:0;background:#f5f1e8;font-family:sans-serif}.empty{padding:24px;color:#526b58;text-align:center}</style>"
            +"<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script></head><body><div id='map'></div><script>"
            +"const places="+points+";const map=L.map('map').setView([-15.78,-47.93],4);"
            +"L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'&copy; OpenStreetMap'}).addTo(map);"
            +"if(!places.length){const info=document.createElement('div');info.className='empty';info.textContent='Nenhuma memória possui localização registrada ainda.';info.style.cssText='position:absolute;z-index:1000;top:45%;left:8%;right:8%;background:#fff;border-radius:16px';document.body.appendChild(info);}"
            +"const bounds=[];places.forEach(p=>{const marker=L.marker([p.lat,p.lon]).addTo(map);const popup=document.createElement('div');const title=document.createElement('strong');title.textContent=p.title;popup.appendChild(title);if(p.place){const place=document.createElement('div');place.textContent=p.place;popup.appendChild(place);}marker.bindPopup(popup);bounds.push([p.lat,p.lon]);});"
            +"if(bounds.length===1)map.setView(bounds[0],13);else if(bounds.length>1)map.fitBounds(bounds,{padding:[28,28]});</script></body></html>";
    }
}

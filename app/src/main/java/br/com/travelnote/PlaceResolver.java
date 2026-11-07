package br.com.travelnote;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Converte coordenadas em cidade/estado usando o serviço de geocodificação do Android. */
public final class PlaceResolver {
    public interface Callback { void onResolved(String placeName); }
    private static final ExecutorService WORKER=Executors.newSingleThreadExecutor();
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private PlaceResolver() {}

    public static void resolve(Context context,double latitude,double longitude,Callback callback) {
        Context app=context.getApplicationContext();
        WORKER.execute(()->{
            String result=null;
            try {
                Geocoder geocoder=new Geocoder(app,new Locale("pt","BR"));
                if(Geocoder.isPresent()) {
                    List<Address> matches=geocoder.getFromLocation(latitude,longitude,1);
                    if(matches!=null&&!matches.isEmpty()) {
                        Address address=matches.get(0);
                        String city=first(address.getLocality(),address.getSubAdminArea(),address.getSubLocality());
                        String state=fullState(address.getAdminArea());
                        if(city!=null&&state!=null&&!city.equalsIgnoreCase(state))result=city+", "+state;
                        else if(city!=null)result=city;
                        else if(address.getFeatureName()!=null)result=address.getFeatureName();
                    }
                }
            } catch(IOException|IllegalArgumentException ignored) { }
            final String resolved=result;
            if(callback!=null)MAIN.post(()->callback.onResolved(resolved));
        });
    }
    private static String first(String... values){for(String value:values)if(value!=null&&!value.trim().isEmpty())return value.trim();return null;}
    private static String fullState(String state){
        if(state==null||state.trim().isEmpty())return null;
        String s=state.trim().replaceFirst("(?i)^(estado de|state of)\\s+","");
        if(s.length()!=2)return s;
        switch(s.toUpperCase(Locale.ROOT)){
            case "AC":return "Acre";case "AL":return "Alagoas";case "AP":return "Amapá";case "AM":return "Amazonas";
            case "BA":return "Bahia";case "CE":return "Ceará";case "DF":return "Distrito Federal";case "ES":return "Espírito Santo";
            case "GO":return "Goiás";case "MA":return "Maranhão";case "MT":return "Mato Grosso";case "MS":return "Mato Grosso do Sul";
            case "MG":return "Minas Gerais";case "PA":return "Pará";case "PB":return "Paraíba";case "PR":return "Paraná";
            case "PE":return "Pernambuco";case "PI":return "Piauí";case "RJ":return "Rio de Janeiro";case "RN":return "Rio Grande do Norte";
            case "RS":return "Rio Grande do Sul";case "RO":return "Rondônia";case "RR":return "Roraima";case "SC":return "Santa Catarina";
            case "SP":return "São Paulo";case "SE":return "Sergipe";case "TO":return "Tocantins";default:return s;
        }
    }
}

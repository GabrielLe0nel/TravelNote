package br.com.travelnote;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private MemoryDatabase db; private LinearLayout cards; private EditText search; private TextView empty; private List<Memory> memories=new ArrayList<>();
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.rgb(245,241,232));getWindow().setNavigationBarColor(Color.rgb(245,241,232));db=new MemoryDatabase(this);build();}
    private void build(){
        setContentView(R.layout.activity_main);
        cards=findViewById(R.id.memory_cards);search=findViewById(R.id.search_memories);empty=findViewById(R.id.empty_memories);
        findViewById(R.id.button_new_memory).setOnClickListener(v->startActivity(new Intent(this,EditorActivity.class)));
        findViewById(R.id.button_map).setOnClickListener(v->startActivity(new Intent(this,MapActivity.class)));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){render();}public void afterTextChanged(Editable e){}});
    }
    private void render(){memories=db.all();cards.removeAllViews();String q=search.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);int shown=0;for(Memory m:memories){String searchable=(m.title+" "+m.description+" "+(m.locationName==null?"":m.locationName)).toLowerCase(java.util.Locale.ROOT);if(!q.isEmpty()&&!searchable.contains(q))continue;MemoryCardView card=(MemoryCardView)getLayoutInflater().inflate(R.layout.item_memory_card,cards,false);card.setMemory(m);card.setContentDescription("Memória: "+m.title+". Toque para abrir.");cards.addView(card);card.setOnClickListener(v->{Intent i=new Intent(this,DetailActivity.class);i.putExtra("id",m.id);startActivity(i);});
            if(m.locationName==null&&m.latitude!=null){PlaceResolver.resolve(this,m.latitude,m.longitude,name->{if(name!=null){m.locationName=name;db.updateLocationName(m.id,name);card.setMemory(m);}});}
            shown++;}empty.setVisibility(shown==0?View.VISIBLE:View.GONE);if(shown==0)empty.setText(memories.isEmpty()?"Ainda não há memórias.\nRegistre seu primeiro momento de viagem.":"Nenhuma memória encontrada.");}
    @Override protected void onResume(){super.onResume();if(db!=null&&cards!=null)render();}
}

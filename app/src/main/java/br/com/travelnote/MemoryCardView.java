package br.com.travelnote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/** Cartão de memória desenhado em Canvas, com o nome do lugar visitado. */
public class MemoryCardView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Memory memory;
    private final float d;
    public MemoryCardView(Context c) { this(c, null); }
    public MemoryCardView(Context c, AttributeSet a) { super(c, a); d=getResources().getDisplayMetrics().density; setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    public void setMemory(Memory m) { memory=m; invalidate(); }
    private void text(Canvas c, String s, float x, float y, float size, int color, boolean bold) { p.setColor(color); p.setTextSize(size*d); p.setTypeface(bold?Typeface.create("sans-serif-medium",Typeface.NORMAL):Typeface.create("sans-serif",Typeface.NORMAL)); c.drawText(s,x,y,p); }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        Memory shown=memory;
        if(shown==null&&isInEditMode()){shown=new Memory();shown.title="TravelNote";shown.createdAt="Memória de viagem";shown.locationName="Brusque, Santa Catarina";}
        if(shown==null)return; float w=getWidth()/d, h=getHeight()/d;
        p.setColor(0xFFFFFFFF); p.setShadowLayer(8*d,0,3*d,0x18000000); c.drawRoundRect(new RectF(3*d,3*d,(w-3)*d,(h-3)*d),18*d,18*d,p); p.clearShadowLayer();
        p.setColor(0xFFE5EBE1); c.drawRoundRect(new RectF(16*d,16*d,54*d,54*d),12*d,12*d,p);
        drawMountains(c);
        String title=shown.title==null?"":shown.title; if(title.length()>30)title=title.substring(0,28)+"…";
        text(c,title,68*d,34*d,17,0xFF26342B,true);
        String date=shown.createdAt==null?"":shown.createdAt; text(c,date,68*d,52*d,11,0xFF6E756E,false);
        String place=shown.locationName;
        if(place==null||place.trim().isEmpty()) place=shown.latitude==null?"Localização não registrada":String.format(java.util.Locale.getDefault(),"%.5f, %.5f",shown.latitude,shown.longitude);
        p.setTextSize(12*d);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
        float maxWidth=(w-36)*d;while(place.length()>4&&p.measureText("⌖  "+place)>maxWidth)place=place.substring(0,place.length()-2)+"…";
        text(c,"⌖  "+place,18*d,86*d,12,0xFF526B58,false);
    }
    private void drawMountains(Canvas c){
        p.setStyle(Paint.Style.FILL);p.setColor(0xFF718675);Path left=new Path();left.moveTo(19*d,47*d);left.lineTo(31*d,25*d);left.lineTo(43*d,47*d);left.close();c.drawPath(left,p);
        p.setColor(0xFF526B58);Path right=new Path();right.moveTo(29*d,47*d);right.lineTo(41*d,27*d);right.lineTo(51*d,47*d);right.close();c.drawPath(right,p);
        p.setColor(0xFF6B5743);Path snowLeft=new Path();snowLeft.moveTo(28*d,31*d);snowLeft.lineTo(31*d,25*d);snowLeft.lineTo(35*d,32*d);snowLeft.lineTo(32*d,31*d);snowLeft.lineTo(30*d,33*d);snowLeft.close();c.drawPath(snowLeft,p);
        Path snowRight=new Path();snowRight.moveTo(38*d,32*d);snowRight.lineTo(41*d,27*d);snowRight.lineTo(45*d,34*d);snowRight.lineTo(42*d,33*d);snowRight.lineTo(40*d,35*d);snowRight.close();c.drawPath(snowRight,p);
        p.setColor(0xFFA7B5A4);c.drawLine(23*d,47*d,48*d,47*d,p);
    }
}

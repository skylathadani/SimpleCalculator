package com.example.calculator;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.OptionalDataException;

public class MainActivity extends AppCompatActivity {
    private enum Operator {none,add,minus,multiply,divide}
    private double data1 = 0, data2 = 0;
    private Operator optr = Operator.none;



    public void btn00Click(View view){
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        eText.setText(eText.getText()+"0");
    }


    public void btn01Click(View view){
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        eText.setText(eText.getText()+"1");
    }

    public void btn02Click(View view){
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        eText.setText(eText.getText()+"2");
    }
    public void btn03Click(View view){
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        eText.setText(eText.getText()+"3");
    }
    public void btn04Click(View view){
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        eText.setText(eText.getText()+"4");
    }
    public void btn05Click(View view){
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        eText.setText(eText.getText()+"5");
    }
    public void btn06Click(View view){
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        eText.setText(eText.getText()+"6");
    }
    public void btn07Click(View view){
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        eText.setText(eText.getText()+"7");
    }
    public void btn08Click(View view){
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        eText.setText(eText.getText()+"8");
    }
    public void btn09Click(View view){
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        eText.setText(eText.getText()+"9");
    }
    public void btnAddClick(View view){
        optr = Operator.add;
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        data1 = Double.parseDouble(eText.getText().toString());
        eText.setText("");
    }
    public void btnMinusClick(View view){
        optr = Operator.minus;
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        data1 = Double.parseDouble(eText.getText().toString());
        eText.setText("");
    }
    public void btnMultiplyClick(View view){
        optr = Operator.multiply;
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        data1 = Double.parseDouble(eText.getText().toString());
    }
    public void btnDivideClick(View view){
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        optr = Operator.divide;
        data1 = Double.parseDouble(eText.getText().toString());
        eText.setText("");
    }

    public void btnClearClick(View view){
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        eText.setText("");
    }

    public void btnDotClick(View view){
        TextView eText = (TextView)findViewById(R.id.resultEdit);
        eText.setText(eText.getText()+".");
    }
    public void btnResultClick(View view){
        if(optr != Operator.none){
            TextView eText = (TextView)findViewById(R.id.resultEdit);
            data2 = Double.parseDouble(eText.getText().toString());
            double result = 0;
            if(optr == Operator.add){
                result = data1+data2;
            }else if (optr == Operator.minus){
                result = data1 - data2;
            }else if (optr == Operator.multiply){
                result = data1*data2;
            }else if (optr == Operator.divide){
                result = data1/data2;
            }
            optr = Operator.none;
            data1 = result;
            if((result-(int)result) != 0)
                eText.setText(String.valueOf(result));
            else
                eText.setText(String.valueOf((int)result));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
